/*   
 *  Copyright 1999-2004 The Apache Sofware Foundation.
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

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.xml.SaxContext;
import org.apache.tomcat.util.xml.XmlAction;
import org.apache.tomcat.util.xml.XmlMapper;
import org.xml.sax.AttributeList;

/**
 * This is a configuration module that will read context configuration files,
 * including server.xml, and configure the server by adding the contexts.
 *
 * @author Costin Manolache
 */
public class ContextXmlReader extends BaseInterceptor {
    private static StringManager sm =
	StringManager.getManager("org.apache.tomcat.resources");
    
    public ContextXmlReader() {
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

    public void engineInit(ContextManager cm)
	throws TomcatException
    {
        if( configFile==null )
	    configFile=(String)cm.getNote("configFile");

	XmlMapper xh=new XmlMapper();
	xh.setDebug( debug );

	// use the same tags for context-local modules
	addTagRules(cm, xh);
	setContextRules( xh );
        setPropertiesRules( cm, xh );
	setBackward( xh );

	// load the config file(s)
	File f  = null;
	if (configFile == null)
	    configFile=DEFAULT_CONFIG;

        if (File.separatorChar != '/')
            configFile=configFile.replace('/',File.separatorChar);

        f=new File(configFile);
	if( !f.isAbsolute())
	    f=new File( cm.getHome(), configFile);

	if( f.exists() )
	    ServerXmlReader.loadConfigFile(xh,f,cm);

	// load server-*.xml
	Vector v = ServerXmlReader.getUserConfigFiles(f);
	for (Enumeration e = v.elements();
	     e.hasMoreElements() ; ) {
	    f = (File)e.nextElement();
	    if( f.exists() ) {
		String s=f.getAbsolutePath();
		if( s.startsWith( cm.getHome()))
		    s="$TOMCAT_HOME" + s.substring( cm.getHome().length());
		log( "Context config=" + s);
	    }
	    ServerXmlReader.loadConfigFile(xh,f,cm);
	}
    }

    // -------------------- Xml reading details --------------------

    static class ContextPropertySource
        implements IntrospectionUtils.PropertySource
    {
        ContextManager cm;
        Context ctx=null;
	
        ContextPropertySource( ContextManager cm ) {
            this.cm=cm;
        }

        public void setContext(Context ctx) {
            this.ctx=ctx;
        }
	
        public String getProperty( String key ) {
            // XXX add other "predefined" properties
            String s=null;
            if( ctx != null )
                s=ctx.getProperty( key );              
            if( s == null )
                s=cm.getProperty( key );
            if( s == null )
        	s=System.getProperty( key );
            return s;
        }
    }

    public static void setPropertiesRules( ContextManager cm, XmlMapper xh )
	throws TomcatException
    {
	ContextPropertySource propS=new ContextPropertySource( cm );
	xh.setPropertySource( propS );
	
	xh.addRule( "Context/Property", new XmlAction() {
		public void start(SaxContext ctx ) throws Exception {
		    AttributeList attributes = ctx.getCurrentAttributes();
		    String name=attributes.getValue("name");
		    String value=attributes.getValue("value");
		    if( name==null || value==null ) return;
		    XmlMapper xm=ctx.getMapper();
		    
		    Context context=(Context)ctx.currentObject();
		    // replace ${foo} in value
		    value=xm.replaceProperties( value );
		    if( context.getDebug() > 0 )
			context.log("Setting " + name + "=" + value);
		    context.setProperty( name, value );
		}
	    });
    }

    // rules for reading the context config
    public static void setContextRules( XmlMapper xh ) {
	// Default host
	xh.addRule( "Context",
		    xh.objectCreate("org.apache.tomcat.core.Context"));
	xh.addRule( "Context",
		    xh.setProperties() );
	xh.addRule( "Context",
		    xh.setParent("setContextManager") );
	
	// Virtual host support - if Context is inside a <Host>
	xh.addRule( "Host", xh.setVar( "current_host", "name"));
	xh.addRule( "Host", xh.setVar( "current_address", "address"));
	xh.addRule( "Host", xh.setVar( "host_aliases", "")); // so host_aliases will get reset
	xh.addRule( "Host", xh.setProperties());
	xh.addRule( "Alias", new XmlAction() {
		public void start( SaxContext xctx) throws Exception {
		    Vector aliases=(Vector)xctx.getVariable( "host_aliases" );
		    if( aliases==null ) {
			aliases=new Vector();
			xctx.setVariable( "host_aliases", aliases );
		    }
		    String alias=(String)xctx.getCurrentAttributes().getValue("name");
		    if( alias!=null ) 
			aliases.addElement( alias );
		}
	    });

        xh.addRule( "Context", new XmlAction() {
                public void start( SaxContext xctx) throws Exception {
                    Context tcCtx=(Context)xctx.currentObject();
                    XmlMapper xm=xctx.getMapper();
                    ContextPropertySource propS = (ContextPropertySource)xm.getPropertySource();
                    if( propS != null )
                        propS.setContext(tcCtx);
                }
            });

	xh.addRule( "Context", new XmlAction() {
		public void end( SaxContext xctx) throws Exception {
		    Context tcCtx=(Context)xctx.currentObject();
                    XmlMapper xm=xctx.getMapper();
                    ContextPropertySource propS = (ContextPropertySource)xm.getPropertySource();
                    if( propS != null )
                        propS.setContext(null);
		    String host=(String)xctx.getVariable("current_host");
		    String address=(String)xctx.getVariable("current_address");
		    Vector aliases=(Vector)xctx.getVariable( "host_aliases" );
		    
		    if( host!=null && ! "DEFAULT".equals( host )) {
			    tcCtx.setHost( host );
			    tcCtx.setHostAddress( address );
			    if( aliases!=null ) {
				Enumeration alE=aliases.elements();
				while( alE.hasMoreElements() ) {
				    String alias=(String)alE.nextElement();
				    tcCtx.addHostAlias( alias );
				    if( tcCtx.getDebug() > 0 )
					tcCtx.log( "Alias " + host  + " " + alias );
				}
			    }
		    }
		}
	    });

	xh.addRule( "Context",
		    xh.addChild("addContext",
				"org.apache.tomcat.core.Context") );
    }

    // -------------------- Backward compatibility -------------------- 

    // Read old configuration formats
    private static void setBackward( XmlMapper xh ) {
	// Configure context interceptors
	xh.addRule( "Context/Interceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "Context/Interceptor",
		    xh.setProperties() );
	xh.addRule( "Context/Interceptor",
		    xh.setParent("setContext") );
	xh.addRule( "Context/Interceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));
	// Old style 
	xh.addRule( "Context/RequestInterceptor",
		    xh.objectCreate(null, "className"));
	xh.addRule( "Context/RequestInterceptor",
		    xh.setProperties() );
	xh.addRule( "Context/RequestInterceptor",
		    xh.setParent("setContext") );
	xh.addRule( "Context/RequestInterceptor",
		    xh.addChild( "addInterceptor",
				 "org.apache.tomcat.core.BaseInterceptor"));
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

    // --------------------

    public void addTagRules( ContextManager cm, XmlMapper xh )
	throws TomcatException
    {
	Hashtable modules=(Hashtable)cm.getNote("modules");
	if( modules==null) return;
	Enumeration keys=modules.keys();
	while( keys.hasMoreElements() ) {
	    String name=(String)keys.nextElement();
	    String classN=(String)modules.get( name );

	    String tag="Context" + "/" + name;
	    xh.addRule(  tag ,
			 xh.objectCreate( classN, null ));
	    xh.addRule( tag ,
			xh.setProperties());
	    xh.addRule( tag,
			xh.addChild( "addInterceptor",
				     "org.apache.tomcat.core.BaseInterceptor"));
	}
    }
}

