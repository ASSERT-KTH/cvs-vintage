/*
 * Copyright (c) 2000 Peter Antman Tim <peter.antman@tim.se>
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
import org.jboss.logging.Logger;
/**
 * OpenJMSProvider.java
 *
 *
 * Created: Wed Nov 29 14:19:42 2000
 *
 * @author 
 * @version
 */

public class OpenJMSProvider implements JMSProviderAdapter, java.io.Serializable{
    public static final String TOPIC_CONNECTION_FACTORY="JmsTopicConnectionFactory";
    public static final String QUEUE_CONNECTION_FACTORY="JmsQueueConnectionFactory";
    public static final String INITIAL_CONTEXT_FACTORY = "org.exolab.jms.jndi.rmi.RmiJndiInitialContextFactory";
    
    private static final String HOST_PROP_NAME="org.exolab.jms.jndi.Host";
    private static final String PORT_PROP_NAME="org.exolab.jms.jndi.PortNumber";

    private static final String SECURITY_MANAGER="java.naming.rmi.security.manager";

    private String hasJndiSecurityManager = "yes";
    private String name;
    private String url;
    public OpenJMSProvider() {
	
    }

    public void setProviderUrl(String url) {this.url = url;}
    public String getProviderUrl() { return url;    }
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public Context getInitialContext() throws NamingException {
	// Have to parse url!!
	Context context = null;
	
	Hashtable props = new Hashtable();
	props.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
	props.put(HOST_PROP_NAME, "localhost");
	Logger.debug("OpenJMSProvider: " + HOST_PROP_NAME + ":localhost");
	props.put(PORT_PROP_NAME, new Integer(1199));
	Logger.debug("OpenJMSProvider: " + PORT_PROP_NAME + 1199);
	//props.put(Context.PROVIDER_URL, jndiProviderUrl);
	props.put(SECURITY_MANAGER, hasJndiSecurityManager);
	//   props.put(Context.URL_PKG_PREFIXES, jndiPkgPrefixes);
	return context= new InitialContext(props);
	

    }
    public  String getTopicFactoryName(){return TOPIC_CONNECTION_FACTORY;}
    public String getQueueFactoryName(){return QUEUE_CONNECTION_FACTORY;}
    public static void main(String[] args) {
	
    }
    
} // OpenJMSProvider
