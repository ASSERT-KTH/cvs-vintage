/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper34.runtime11;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.servlet.jsp.JspFactory;

/**
 * This class provides a basic implementation for TagPoolManager.
 * It simply manages a collection of named pools, including their
 * retrieval and cleanup.
 *
 * @author Casey Lucas <clucas@armassolutions.com
 * @see TagPoolManagerInterceptor
 */
public class TagPoolManagerImpl implements TagPoolManager {

    /**
     * the collection of tag pools
     */
    Hashtable myPools = new Hashtable();

    JspFactoryImpl factory;

    /**
     * This constant is the log named that can be used in configuration
     * files to enable logging.
     */
    public static final String LOG_NAME = "tag_pool_log";

    static int debug=0;

    /**
     * This constructor just sets up logging.
     */
    public TagPoolManagerImpl() {
	factory=(JspFactoryImpl)JspFactory.getDefaultFactory();
    }

    /**
     * Obtain a pool by the given name that provides handlers for
     * the given class.  If no pool is available for the given name
     * then allocate a new one and return it.
     *
     * @param poolName name of the requested pool
     * @param handlerClass
     *                 class of the tag handlers
     * @return named pool
     */
    public TagHandlerPool getPool(String poolName, Class handlerClass) {
        TagHandlerPool returnValue = null;
        synchronized (myPools) {
            returnValue = (TagHandlerPool) myPools.get(poolName);
            if (returnValue == null) {
                returnValue = new TagHandlerPoolImpl(handlerClass, poolName);
                myPools.put(poolName, returnValue);
            }

            if (debug > 0 ) {
                factory.debug("Getting pool named '" +
			    poolName + "' for tag handler class '" +
			    handlerClass.getName() + "'");
                factory.debug("Number of pools is now " + myPools.size());
            }
        }

        return returnValue;
    }


    /**
     * This method is called when the tag pools should be shutdown.  It
     * calls shutdown for each of the tag pools.
     */
    public void shutdown() {
        synchronized (myPools) {
            if (debug > 0 ) {
                factory.debug("Shutting down " + myPools.size() + " pools");
            }

            Enumeration pools = myPools.elements();
            while (pools.hasMoreElements()) {
                ((TagHandlerPool) pools.nextElement()).shutdown();
            }
        }
    }

}

