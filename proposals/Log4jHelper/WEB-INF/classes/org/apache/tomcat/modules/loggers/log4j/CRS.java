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
package org.apache.tomcat.modules.loggers.log4j;

import java.util.*;
import org.apache.tomcat.core.*;
import org.apache.log4j.*;
import org.apache.log4j.spi.*;

/**
 * Implimentation to add context-seperation support for log4j logging.
 * Code taken from <a href="http://www.qos.ch/logging/sc.html">
 * Support for log4j in Servlet Containers</a> by Ceki Gülcü.
 * @author Ceki Gülcü.
 */
public class CRS implements RepositorySelector {
    private Hashtable ht = new Hashtable();
    private Level defLevel=Level.DEBUG;

    public void setDefaultLevel(Level lv) {
	if(lv != null)
	    defLevel = lv;
    }
    public Level getDefaultLevel() {
	return defLevel;
    }

    void addRepository(ClassLoader cl) {
	if( ht.get(cl) != null ) {
	    throw new IllegalStateException("ClassLoader " + cl + " already registered");
	}
	ht.put(cl, new Hierarchy(new RootCategory( defLevel)));
    }
				 
    // the returned value is guaranteed to be non-null
    public LoggerRepository getLoggerRepository() {
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	Hierarchy hierarchy = (Hierarchy) ht.get(cl);
	
	if(hierarchy == null) {
	    hierarchy = new Hierarchy(new RootCategory(defLevel));
	    ht.put(cl, hierarchy);
	} 
	return hierarchy;
    }

    /** 
     * The Container should remove the entry when the web-application
     * is removed or restarted.
     * */
    public void remove(ClassLoader cl) {
	ht.remove(cl); 
    } 
}


