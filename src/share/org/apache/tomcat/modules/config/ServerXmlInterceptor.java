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
package org.apache.tomcat.modules.config;

import java.beans.*;
import java.io.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.helper.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.util.log.*;
import org.xml.sax.*;

/**
 * This is a configuration module that will read a server.xml file
 * and dynamically configure the server by adding modules and interceptors.
 *
 * Tomcat can be configured ( and auto-configured ) in many ways, and
 * a configuration module will have access to all server events, and will
 * be able to update it's state, etc.
 *
 * @author Costin Manolache
 */
public class ServerXmlInterceptor extends BaseInterceptor {
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    
    public ServerXmlInterceptor() {
    }

    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( this != module ) return;

	// this==module -> we have been added to the server.
	
	XmlMapper xh=new XmlMapper();
	xh.setDebug( debug );
	
	ServerXmlHelper sxml=new ServerXmlHelper();
	sxml.setHelper( xh );
	sxml.setConnectorHelper( xh );
	sxml.setLogHelper( xh );

	String tchome=sxml.getTomcatInstall();
	cm.setInstallDir( tchome);

	// load server.xml
	File f = null;
	if (configFile != null)
	    f=new File(configFile);
	else
	    f=new File(tchome, DEFAULT_CONFIG);

	loadConfigFile(xh,f,cm);

	// load server-*.xml
	Vector v = sxml.getUserConfigFiles(f);
	for (Enumeration e = v.elements();
	     e.hasMoreElements() ; ) {
	    f = (File)e.nextElement();
	    loadConfigFile(xh,f,cm);
	}
    }

    private void loadConfigFile(XmlMapper xh, File f, ContextManager cm)
	throws TomcatException
    {
	log(sm.getString("tomcat.loading") + " " + f);
	try {
	    xh.readXml(f,cm);
	} catch( Exception ex ) {
	    log( sm.getString("tomcat.fatalconfigerror"), ex );
	    throw new TomcatException(ex);
	}
	log(sm.getString("tomcat.loaded") + " " + f);
    }

    // -------------------- Command-line args processing --------------------
    // null means user didn't set one
    String configFile=null;
    // relative to TOMCAT_HOME 
    static final String DEFAULT_CONFIG="conf/server.xml";

    public void setConfig( String s ) {
	configFile=s;
    }

    public void setHome( String h ) {
	System.getProperties().put("tomcat.home", h);
    }
}
