/* $Id: MxInterceptor.java,v 1.5 2002/09/19 09:03:15 hgomez Exp $
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

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.io.FileUtil;
import java.io.*;
import java.util.*;

import javax.management.*;

import org.apache.tomcat.util.mx.*;

/**
 *
 * @author Costin Manolache
 */
public class MxInterceptor  extends BaseInterceptor { 

	MBeanServer 	mserver;
    private int 	port=-1;
    private String host;
    private String	auth;
    private String user;
    private String password;
	
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

    /** Enable the MX4J internal adapter
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

    public void setAuthentification( String auth ) {
    	if ("none".equals(auth) || "basic".equals(auth) || "digest".equals(auth))
        	this.auth=auth;
    }

    public String getAuthentification() {
        return auth;
    }

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

    /* ==================== Start/stop ==================== */
    ObjectName serverName=null;
    
    /** Initialize the worker. After this call the worker will be
     *  ready to accept new requests.
     */
    public void loadAdapter() throws IOException {
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
            log( "Can't load the MX4J http adapter " + t.toString()  );
        }

        try {
            Class c=Class.forName( "com.sun.jdmk.comm.HtmlAdaptorServer" );
            Object o=c.newInstance();
            serverName=new ObjectName("Adaptor:name=html,port=" + port);
            log("Registering the JMX_RI html adapter " + serverName);
            mserver.registerMBean(o,  serverName);

            mserver.setAttribute(serverName,
                                 new Attribute("Port", new Integer(port)));

            mserver.invoke(serverName, "start", null, null);
			log( "Start JMX_RI http adaptor at port " + port);

        } catch( Throwable t ) {
            log( "Can't load the JMX_RI http adapter " + t.toString()  );
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
        if( bi==this ) {
            // Adding myself and on-time things
            createMBean( "tomcat3", cm, "Tomcat3Container" );

			if( port > 0 ) {
				try {
	        		loadAdapter();
				}
				catch (IOException ioe)
				{
					log("can't load adaptor");
				}
			}
        }
        createMBean( "tomcat3", bi, null);
    }

    public void initRequest( ContextManager cm, Request req, Response resp )
    {
        createMBean( "tomcat3.requests", req, null);
    }
}
