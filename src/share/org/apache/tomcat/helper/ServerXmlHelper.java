/*
 * ====================================================================
 *
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
 * [Additional notices, if required by prior licensing conditions]
 *
 */
package org.apache.tomcat.helper;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.log.*;
import org.xml.sax.*;

/**
 *  Helper for reading server.xml
 *
 * @author Costin
 */
public class ServerXmlHelper {
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    public static final String[] MODULE_PKGS={
	"org.apache.tomcat.modules.session.",
	"org.apache.tomcat.modules.server.",
	"org.apache.tomcat.modules.config.",
	"org.apache.tomcat.modules.security.",
	"org.apache.tomcat.request.", // OLD
	"org.apache.tomcat.context." // OLD
    };
       
    
    public ServerXmlHelper() {
    }

    // Set the mappings
    public void setHelper( XmlMapper xh ) {
	xh.addRule( "ContextManager", xh.setProperties() );

	setInterceptorRules( xh );
	setContextRules( xh );
	setVHostRules( xh );
    }

    public static void setInterceptorRules( XmlMapper xh ) {
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.objectCreate(null, "className", MODULE_PKGS));
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.setParent("setContextManager") );
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.objectCreate(null, "className", MODULE_PKGS));
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.setParent("setContextManager") );
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));
    }
    
    public static void setContextRules( XmlMapper xh ) {
	// Default host
 	xh.addRule( "Context",
		    xh.objectCreate("org.apache.tomcat.core.Context"));
	xh.addRule( "Context",
		    xh.setProperties() );
	xh.addRule( "Context",
		    xh.setParent("setContextManager") );

	xh.addRule( "Context", new XmlAction() {
		public void end( SaxContext ctx) throws Exception {
		    Context tcCtx=(Context)ctx.currentObject();
		    String host=(String)ctx.getVariable("current_host");

		    if( host!=null && ! "DEFAULT".equals( host )) 
			tcCtx.setHost( host );
		}
	    });

	xh.addRule( "Context",
		    xh.addChild("addContext",
				"org.apache.tomcat.core.Context") );
	
	// Configure context interceptors
	xh.addRule( "Context/Interceptor",
		    xh.objectCreate(null, "className", MODULE_PKGS));
	xh.addRule( "Context/Interceptor",
		    xh.setProperties() );
	xh.addRule( "Context/Interceptor",
		    xh.setParent("setContext") );
	xh.addRule( "Context/Interceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));
	// Old style 
	xh.addRule( "Context/RequestInterceptor",
		    xh.objectCreate(null, "className", MODULE_PKGS));
	xh.addRule( "Context/RequestInterceptor",
		    xh.setProperties() );
	xh.addRule( "Context/RequestInterceptor",
		    xh.setParent("setContext") );
	xh.addRule( "Context/RequestInterceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));
    }

    // Virtual host support.
    public static void setVHostRules( XmlMapper xh ) {
 	xh.addRule( "Host", xh.setVariable( "current_host", "name"));
	xh.addRule( "Host", xh.setProperties());
    }

    public void setConnectorHelper( XmlMapper xh ) {
	xh.addRule( "ContextManager/Connector",
		    xh.objectCreate(null, "className", MODULE_PKGS));
	xh.addRule( "ContextManager/Connector",
		    xh.setParent( "setContextManager",
				  "org.apache.tomcat.core.ContextManager") );
	xh.addRule( "ContextManager/Connector",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	xh.addRule( "ContextManager/Connector/Parameter",
		    xh.methodSetter("setProperty",2) );
	xh.addRule( "ContextManager/Connector/Parameter",
		    xh.methodParam(0, "name") );
	xh.addRule( "ContextManager/Connector/Parameter",
		    xh.methodParam(1, "value") );
    }


    /** Setup loggers when reading the configuration file - this will be
     *  called only when starting tomcat as deamon, all other modes will
     * output to stderr
     * *** [I don't think that's true any more -Alex]
     */
    public void setLogHelper( XmlMapper xh ) {
	setLogRules( xh );
    }

    public static void setLogRules( XmlMapper xh ) {
	xh.addRule("Server/Logger",
		   xh.objectCreate("org.apache.tomcat.util.log.QueueLogger"));
	xh.addRule("Server/Logger", xh.setProperties());
	xh.addRule("Server/Logger", 
		   xh.addChild("addLogger",
			       "org.apache.tomcat.util.log.Logger") );

	xh.addRule("Context/Logger",
		   xh.objectCreate("org.apache.tomcat.util.log.QueueLogger"));
	xh.addRule("Context/Logger", xh.setProperties());
	xh.addRule("Context/Logger", 
		   xh.addChild("setLogger",
			       "org.apache.tomcat.util.log.Logger") );

	xh.addRule("Context/ServletLogger",
		   xh.objectCreate("org.apache.tomcat.util.log.QueueLogger"));
	xh.addRule("Context/ServletLogger", xh.setProperties());
	xh.addRule("Context/ServletLogger", 
		   xh.addChild("setServletLogger",
			       "org.apache.tomcat.util.log.Logger") );


    }

    /**
     * Return the configuration file we are processing.  If the
     * <code>-config filename</code> command line argument is not
     * used, the default configuration filename will be loaded from
     * the TOMCAT_HOME directory.
     *
     * If a relative config file is used, it will be relative to the current
     * working directory.
     *
     * @param cm The ContextManager we are configuring
     **/
    public String getTomcatInstall() {
	// Use the "tomcat.home" property to resolve the default filename
	String tchome = System.getProperty("tomcat.home");
	if (tchome == null) {
	    System.out.println(sm.getString("tomcat.nohome"));
	    tchome = ".";
	    // Assume current working directory
	}
	return tchome;
    }

    public Vector getUserConfigFiles(File master) {
	File dir = new File(master.getParent());
	String[] names = dir.list( new ConfigFilter(master) );
	Vector v = new Vector(names.length);
	for (int i=0; i<names.length; ++i) {
	    File found = new File(dir, names[i]);
	    v.addElement(found);
	}
	return v;
    }

    class ConfigFilter implements FilenameFilter {
	String start;
	String end;
	public ConfigFilter(File master) {
	    String name = master.getName();
	    int dot = name.indexOf(".");
	    if (dot==-1) return;
	    start = name.substring(0,dot) + "-";
	    end = name.substring(dot);
	}
	public boolean accept(File dir, String name) {
	    if (start == null || end == null) return false;
	    if (name.startsWith(start) &&
		name.endsWith(end))
	    {
		return true;
	    }
	    return false;
	}
    }

    // -------------------- Deprecated --------------------
    
}
