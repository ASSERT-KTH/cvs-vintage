/*
 * Copyright (c) 2000 Peter Antman DN <peter.antman@dn.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.jndi;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
/**
 * JBossMQProvider.java
 *
 *
 * Created: Fri Dec 22 09:34:04 2000
 *
 * @author Peter Antman
 * @version
 */

public class JBossMQProvider implements JMSProviderAdapter, java.io.Serializable{
    public static final String TOPIC_CONNECTION_FACTORY="XATopicConnectionFactory";
    public static final String QUEUE_CONNECTION_FACTORY="XAQueueConnectionFactory";
    public static final String INITIAL_CONTEXT_FACTORY = "org.jnp.interfaces.NamingContextFactory";
    public static final String URL_PKG_PREFIXES = "org.jboss.naming";
    private static final String SECURITY_MANAGER="java.naming.rmi.security.manager";

    private String hasJndiSecurityManager = "yes";
    private String name;
    private String url;
    public JBossMQProvider() {
	
    }
    
    public void setProviderUrl(String url) {
	this.url = url;

    }
    public String getProviderUrl() { return url;    }
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public Context getInitialContext() throws NamingException {
	Context ctx = null;
	if (url == null) {
	    // Use default
	    ctx = new InitialContext();//Only for Jboss embedded now
	} else {
	    //Try another location
	    Hashtable props = new Hashtable();
	    props.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
	    props.put(Context.PROVIDER_URL, url);
	    props.put(SECURITY_MANAGER, hasJndiSecurityManager);
	    props.put(Context.URL_PKG_PREFIXES, URL_PKG_PREFIXES);
	    ctx = new InitialContext(props);
	}
	return ctx;
    }
    public  String getTopicFactoryName(){return TOPIC_CONNECTION_FACTORY;}
    public String getQueueFactoryName(){return QUEUE_CONNECTION_FACTORY;}
} // JBossMQProvider
