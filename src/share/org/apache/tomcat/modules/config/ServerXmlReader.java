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
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.*;
import java.net.*;
import org.apache.tomcat.util.*;
import org.apache.tomcat.util.xml.*;
import org.apache.tomcat.core.*;
import org.apache.tomcat.modules.server.*;
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
public class ServerXmlReader extends BaseInterceptor {
    int configFileNote;
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");

    
    public ServerXmlReader() {
    }

    // -------------------- Properties --------------------
    String configFile=null;
    static final String DEFAULT_CONFIG="conf/server.xml";

    public void setConfig( String s ) {
	configFile=s;
    }

    public void setHome( String h ) {
	System.getProperties().put("tomcat.home", h);
    }

    // -------------------- Hooks --------------------

    /** When this module is added, it'll automatically load
     *  a configuration file and add all global modules.
     */
    public void addInterceptor(ContextManager cm, Context ctx,
			       BaseInterceptor module)
	throws TomcatException
    {
	if( this != module ) return;
	XmlMapper xh=new XmlMapper();
	xh.setDebug( debug );
	xh.addRule( "ContextManager", xh.setProperties() );
	setTagRules( xh );
	addDefaultTags(cm, xh);
	setBackward( xh );

	// load the config file(s)
	File f  = null;
	if (configFile == null)
	    configFile=DEFAULT_CONFIG;

        f=new File(configFile);
        if ( !f.isAbsolute())
	    f=new File( cm.getHome(), File.separator + configFile);

	if( f.exists() ){
            cm.setNote( "configFile", f.getAbsolutePath());
	    loadConfigFile(xh,f,cm);
	    String s=f.getAbsolutePath();
	    if( s.startsWith( cm.getHome()))
		s="$TOMCAT_HOME" + s.substring( cm.getHome().length());
	    log( "Config=" + s);
            // load server-*.xml
/*            Vector v = getUserConfigFiles(f);
            for (Enumeration e = v.elements();
                 e.hasMoreElements() ; ) {
                f = (File)e.nextElement();
                loadConfigFile(xh,f,cm);
		cm.log(sm.getString("tomcat.loading") + " " + f);
            }
*/
        }
    }

    // -------------------- Xml reading details --------------------

    public static void loadConfigFile(XmlMapper xh, File f, ContextManager cm)
	throws TomcatException
    {
	try {
	    xh.readXml(f,cm);
	} catch( Exception ex ) {
	    cm.log( sm.getString("tomcat.fatalconfigerror"), ex );
	    throw new TomcatException(ex);
	}
    }

    public static void setTagRules( XmlMapper xh ) {
	xh.addRule( "module",  new XmlAction() {
		public void end(SaxContext ctx ) throws Exception {
		    Object elem=ctx.currentObject();
		    AttributeList attributes = ctx.getCurrentAttributes();
		    String name=attributes.getValue("name");
		    String classN=attributes.getValue("javaClass");
		    if( name==null || classN==null ) return;
		    XmlMapper mapper=ctx.getMapper();
		    ServerXmlReader.addTag( mapper, name, classN );
		}
	    });
    }

    // read modules.xml, if any, and load taskdefs
    public static  void addDefaultTags( ContextManager cm, XmlMapper xh)
	throws TomcatException
    {
	File f=new File( cm.getHome(), "/conf/modules.xml");
	if( f.exists() ) {
            cm.setNote( "configFile", f.getAbsoluteFile());
	    loadConfigFile( xh, f, cm );
            // load module-*.xml
            Vector v = getUserConfigFiles(f);
            for (Enumeration e = v.elements();
                 e.hasMoreElements() ; ) {
                f = (File)e.nextElement();
                loadConfigFile(xh,f,cm);
            }
	}
    }

    // similar with ant's taskdef
    public static void addTag( XmlMapper xh, String tag, String classN) {
	xh.addRule( tag ,
		    xh.objectCreate( null, classN ));
	xh.addRule( tag ,
		    xh.setProperties());
	xh.addRule( tag,
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));
    }


    // -------------------- File utils --------------------

    // get additional files
    public static Vector getUserConfigFiles(File master) {
	File dir = new File(master.getParent());
	String[] names = dir.list();

	String masterName=master.getAbsolutePath();

	String base=FileUtil.getBase(masterName) + "-";
	String ext=FileUtil.getExtension( masterName );
	
	Vector v = new Vector();
	for (int i=0; i<names.length; ++i) {
	    if( names[i].startsWith( base )
		&& ( ext==null || names[i].endsWith( ext )) ) {

		File found = new File(dir, names[i]);
		v.addElement(found);
	    }
	}
	return v;
    }

    // -------------------- Backward compatibility --------------------

    // Read old configuration formats
    private void setBackward( XmlMapper xh ) {
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/ContextInterceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.setProperties() );
	xh.addRule( "ContextManager/RequestInterceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));

	// old <connector>
	xh.addRule( "ContextManager/Connector",
		    xh.objectCreate(null, "className"));
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

	// old <Logger>
	xh.addRule("Server/Logger",
		   xh.objectCreate("org.apache.tomcat.util.log.QueueLogger"));
	xh.addRule("Server/Logger", xh.setProperties());
	xh.addRule("Server/Logger", 
		   xh.addChild("addLogger",
			       "org.apache.tomcat.util.log.Logger") );

    }


}
