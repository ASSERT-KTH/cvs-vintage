/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.tomcat.modules.config;

import java.net.URL;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
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
 * @version   $Revision: 1.4 $ $Date: 2004/02/26 06:37:28 $
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

