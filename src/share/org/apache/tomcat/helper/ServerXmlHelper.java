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
import org.apache.tomcat.logging.*;
import org.xml.sax.*;

/**
 *  Helper for reading server.xml
 *
 * @author Costin
 */
public class ServerXmlHelper {
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    public ServerXmlHelper() {
    }

    // Set the mappings
    public void setHelper( XmlMapper xh ) {
	xh.addRule( "ContextManager", xh.setProperties() );

	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.setParent("setContextManager") );
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.addChild( "addContextInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.setParent("setContextManager") );
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.addChild( "addRequestInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	// Default host
 	xh.addRule( "ContextManager/Context",
		    xh.objectCreate("org.apache.tomcat.core.Context"));
	xh.addRule( "ContextManager/Context",
		    xh.setParent( "setContextManager") );
	xh.addRule( "ContextManager/Context",
		    xh.setProperties() );
	xh.addRule( "ContextManager/Context",
		    xh.addChild( "addContext", null ) );
	xh.addRule( "ContextManager/Context/RequestInterceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/Context/RequestInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/Context/RequestInterceptor",
		    xh.setParent("setContext") );
	xh.addRule( "ContextManager/Context/RequestInterceptor",
		    xh.addChild( "addRequestInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	// Virtual host support.
	// Push a host object on the stack
 	xh.addRule( "ContextManager/Host", new XmlAction() {
		public void start( SaxContext ctx) throws Exception {
		    Stack st=ctx.getObjectStack();
		    // get attributes 
		    int top=ctx.getTagCount()-1;
		    AttributeList attributes = ctx.getAttributeList( top );

		    // get CM
		    ContextManager cm=(ContextManager)st.peek();

		    // construct virtual host config helper
		    HostConfig hc=new HostConfig(cm);

		    // set the host name
		    hc.setName( attributes.getValue("name")); 
		    st.push( hc );
		}
		public void cleanup( SaxContext ctx) {
		    Stack st=ctx.getObjectStack();
		    Object o=st.pop();
		}
	    });
	xh.addRule( "ContextManager/Host", xh.setProperties());
	
 	xh.addRule( "ContextManager/Host/Context",
		    xh.objectCreate("org.apache.tomcat.core.Context"));
	xh.addRule( "ContextManager/Host/Context",
		    xh.setProperties() );
	xh.addRule( "ContextManager/Host/Context", new XmlAction() {
		public void end( SaxContext ctx) throws Exception {
		    Stack st=ctx.getObjectStack();
		    
		    Context tcCtx=(Context)st.pop(); // get the Context
		    HostConfig hc=(HostConfig)st.peek();
		    st.push( tcCtx );
		    // put back the context, to be cleaned up corectly
		    
		    hc.addContext( tcCtx );
		}
	    });
    }

    public void setConnectorHelper( XmlMapper xh ) {
	xh.addRule( "ContextManager/Connector",
		    xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/Connector",
		    xh.setParent( "setContextManager",
				  "org.apache.tomcat.core.ContextManager") );
	xh.addRule( "ContextManager/Connector",
		    xh.addChild( "addContextInterceptor",
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
	xh.addRule("Server/Logger",
		   xh.objectCreate("org.apache.tomcat.logging.TomcatLogger"));
	xh.addRule("Server/Logger", xh.setProperties());
	xh.addRule("Server/Logger", 
		   xh.addChild("addLogger",
			       "org.apache.tomcat.logging.Logger") );
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
}
