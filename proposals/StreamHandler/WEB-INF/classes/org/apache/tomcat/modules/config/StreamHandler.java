/*
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.tomcat.modules.config;

import java.net.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.net.StreamHandlerFactory;

/** 
 * <code>StreamHandler</code> is a add-on module for the Tomcat 3.3
 * server. By installing this module, you will register a 
 * URLStreamHandlerFactory that:
 * <ul>
 * <li>Loads defined handlers from the <code>ContextClassLoader</code>.</li>
 * <li>Returns a <code>SteamHandler</code> that doesn't support connections
 *     for protocols that don't have a defined handler.  This allows for URL
 *     manipulation.</li>
 * </ul>
 * <p>
 * <strong>Installation</strong>
 * <p>
 *
 * Place the StreamHandler.war file in Tomcat 3.3's modules directory
 * prior to starting Tomcat.
 * 
 *
 * @author    Bill Barker
 * @version   $Revision: 1.2 $ $Date: 2002/01/12 07:08:40 $
 */
public class StreamHandler extends BaseInterceptor {

    // ----------------------------------------------------------------- Fields

    /** The name of the module */
    public static final String MOD_NAME = "StreamHandler";

    /** The current version number */
    public static final String MOD_VERSION = "1.0";

    // ------------------------------------------------------------ Constructor

    /** Default constructor. */
    public StreamHandler() {
    }

    // ------------------------------------------------------------- Attributes


    // ------------------------------------------------ Implementation (Public)

    /**
     * Returns the name of this module.
     *
     * @return   the module name
     */
    public static String getModName() {
        return MOD_NAME;
    }

    /**
     * Returns the current verion number of the module.
     *
     * @return   the current version number
     */
    public static String getModVersion() {
        return MOD_VERSION;
    }

    // -------------------------------------------------------------- Callbacks

    /**
     * This callback is automatically executed by the startup process 
     * after the modules have been configured, but before serving request.
     *
     * @param cm    the <code>ContextManager</code> 
     */
    public void engineStart( ContextManager cm ) throws TomcatException {
	if( debug > 0)
	    log("Setting URLStreamHandlerFactory");
	StreamHandlerFactory shf = new StreamHandlerFactory();
	try {
	    URL.setURLStreamHandlerFactory(shf);
	} catch(Throwable thr) {
	    // Most likely, because the factory is already set.
	    log("Unable to set Factory",thr);
	}
    }
    
    
}

