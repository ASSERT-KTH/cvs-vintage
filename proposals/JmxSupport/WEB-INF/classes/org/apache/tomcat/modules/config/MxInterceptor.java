/* $Id: MxInterceptor.java,v 1.3 2004/02/26 06:04:41 billbarker Exp $
 *
 *  Copyright 1999-2004 The Apache Software Foundation
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

import java.io.IOException;
import java.net.InetAddress;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import mx4j.adaptor.rmi.jrmp.JRMPAdaptorMBean;
import mx4j.tools.naming.NamingServiceMBean;
import mx4j.util.StandardMBeanProxy;
import org.apache.commons.modeler.Registry;

import org.apache.tomcat.core.BaseInterceptor;
import org.apache.tomcat.core.Context;
import org.apache.tomcat.core.ContextManager;
import org.apache.tomcat.core.Request;
import org.apache.tomcat.core.Response;
import org.apache.tomcat.core.TomcatException;
import org.apache.tomcat.modules.config.DynamicMBeanProxy;

/**
 *
 * @author Costin Manolache
 */
public class MxInterceptor  extends BaseInterceptor { 

    MBeanServer     mserver;
    private int    port=-1;
    private String host;
    private String auth;
    private String user;
    private String password;
    private String type = "http";
    private int protocolNote;
    
    // -------------------- Tomcat callbacks --------------------

    private void createMBean( String domain, Object proxy, String name ) {
        try {
            DynamicMBeanProxy mbean=new DynamicMBeanProxy();
            mbean.setReal( proxy );
            if( name!=null ) {
                mbean.setName( "name=" + name );
            }

            mbean.registerMBean( domain );
            
            // Set mserver once
            if (mserver == null)
                mserver = mbean.getMBeanServer();
                
        } catch( Throwable t ) {
            log( "Error creating mbean ", t );
        }
    }

    /* -------------------- Public methods -------------------- */

    /** Enable the MX4J internal adaptor
     */
    public void setPort( int i ) {
        port=i;
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host ) {
        this.host=host;
    }

    public String getHost() {
        return host;
    }

    public void setAuthentication( String auth ) {
        if ("none".equals(auth) || "basic".equals(auth) || "digest".equals(auth))
            this.auth=auth;
    }

    public String getAuthentication() {
        return auth;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isPasswordSet() {
        return password != null && password.length() > 0;
    }

	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
    /* ==================== Start/stop ==================== */
    ObjectName serverName=null;
    
	/** Initialize the JRMP Adaptor for JMX.
	 */
	private void loadJRMPAdaptor()
	{
		try
		{
			// Create the RMI Naming registry
			ObjectName naming = new ObjectName("Naming:type=registry");
			mserver.createMBean("mx4j.tools.naming.NamingService", naming, null, new Object[] {new Integer(port)}, new String[] {"int"});
			NamingServiceMBean nsmbean = (NamingServiceMBean)StandardMBeanProxy.create(NamingServiceMBean.class, mserver, naming);
			nsmbean.start();

			// Create the JRMP adaptor
			ObjectName adaptor = new ObjectName("Adaptor:protocol=JRMP");
			mserver.createMBean("mx4j.adaptor.rmi.jrmp.JRMPAdaptor", adaptor, null);
			JRMPAdaptorMBean jrmpmbean = (JRMPAdaptorMBean)StandardMBeanProxy.create(JRMPAdaptorMBean.class, mserver, adaptor);
	
			// Set the JNDI name with which will be registered
			String jndiName = "jrmp";
			jrmpmbean.setJNDIName(jndiName);

			String lHost = host;

			if (lHost == null)
				lHost = "localhost";
			else if (lHost.length() == 0)
					lHost = InetAddress.getLocalHost().getHostName();

			log( "Started mx4j jrmp adaptor" + ((host != null) ? " for host " + host : "") + " at port " + port);

			jrmpmbean.putJNDIProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
			jrmpmbean.putJNDIProperty(javax.naming.Context.PROVIDER_URL, "rmi://" + lHost + ":" + port);

			jrmpmbean.start();
			
		} 
		catch( Throwable t ) 
		{
			log("Can't load MX4J JRMP adaptor" + t.toString() );
		}
	}

	/** Initialize the HTTP Adaptor for JMX.
	 */
	private void loadHTTPAdaptor() {
		try {
			serverName = new ObjectName("Http:name=HttpAdaptor");
			mserver.createMBean("mx4j.adaptor.http.HttpAdaptor", serverName, null);
            
			if( host!=null ) 
				mserver.setAttribute(serverName, new Attribute("Host", host));
            
			mserver.setAttribute(serverName, new Attribute("Port", new Integer(port)));

			// use authentication if user/password set
			if( auth!=null && user!=null && password!=null) 
				mserver.setAttribute(serverName, new Attribute("AuthenticationMethod", auth));

			// add user names
			mserver.invoke(serverName, "addAuthorization", new Object[] {user, password}, 
						   new String[] {"java.lang.String", "java.lang.String"});

			ObjectName processorName = new ObjectName("Http:name=XSLTProcessor");
			mserver.createMBean("mx4j.adaptor.http.XSLTProcessor", processorName, null);
			mserver.setAttribute(serverName, new Attribute("ProcessorName", processorName));
                
			mserver.invoke(serverName, "start", null, null);
			log( "Started mx4j http adaptor" + ((host != null) ? " for host " + host : "") + " at port " + port);
			return;
		} catch( Throwable t ) {
			log( "Can't load the MX4J http adaptor " + t.toString()  );
		}

		try {
			Class c=Class.forName( "com.sun.jdmk.comm.HtmlAdaptorServer" );
			Object o=c.newInstance();
			serverName=new ObjectName("Adaptor:name=html,port=" + port);
			log("Registering the JMX_RI html adaptor " + serverName);
			mserver.registerMBean(o,  serverName);

			mserver.setAttribute(serverName,
								 new Attribute("Port", new Integer(port)));

			mserver.invoke(serverName, "start", null, null);
			log( "Start JMX_RI http adaptor at port " + port);

		} catch( Throwable t ) {
			log( "Can't load the JMX_RI http adaptor " + t.toString()  );
		}
	}

    /** Initialize the worker. After this call the worker will be
     *  ready to accept new requests.
     */
    public void loadAdaptor() throws IOException {
    	
    	if (type.equalsIgnoreCase("jrmp"))
    		loadJRMPAdaptor();
    	else
			loadHTTPAdaptor();
    }
    	

    public void engineInit(ContextManager cm) throws TomcatException {
	protocolNote = cm.getNoteId(ContextManager.MODULE_NOTE,
				    "coyote.protocol");
	BaseInterceptor[] modules = cm.getContainer().getInterceptors();
	for(int i=0; i < modules.length; i++) {
	    Object proto = modules[i].getNote(protocolNote);
	    if(proto != null) {
		try {
		    Registry.getRegistry(null,null).registerComponent
			(proto, "tomcat3:type=ProtocolHandler,className="+
			   proto.getClass().getName(),"ProtocolHandler");
		}catch(Exception ex) {
		    log("Error registering Protocol",ex);
		}
	    }
	}
    }

    public void destroy() {
        try {
            log("Stoping JMX ");

            if( serverName!=null ) {
                mserver.invoke(serverName, "stop", null, null);
            }
        } catch( Throwable t ) {
            log( "Destroy error", t );
        }
    }

    public void addContext( ContextManager cm,
                            Context ctx )
    throws TomcatException
    {
        String host=ctx.getHost();
        if( host==null ) host="DEFAULT";
        
        createMBean( "webapps", ctx, host + ctx.getPath() );
    }
    
    public void addInterceptor( ContextManager cm,
                Context ctx,
                BaseInterceptor bi )
    throws TomcatException
    {
        // If executing config generation, skip startup
        if ( cm.getProperty("jkconf") != null ) {
            return;
        }

        // if time to start up
        if ( cm.getState() == ContextManager.STATE_CONFIG ) {
            if( bi==this ) {
                // Adding myself and on-time things
                createMBean( "tomcat3", cm, "Tomcat3Container" );

                if( port > 0 ) {
                    try {
                        loadAdaptor();
                    }
                    catch (IOException ioe)
                    {
                        log("can't load adaptor");
                    }
                }

                // add previously added interceptors, including this interceptor
                Object[] modules = cm.getContainer().getInterceptors();
                for (int i=0; i < modules.length; i++) {
                    createMBean( "tomcat3", modules[i], null);
                }
            }
            else {
                createMBean( "tomcat3", bi, null);
            }
        }
    }

    public void initRequest( ContextManager cm, Request req, Response resp )
    {
        createMBean( "tomcat3.requests", req, null);
    }
}
