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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
/**
 * SpyderMQProvider.java
 *
 *
 * Created: Fri Dec 22 09:34:04 2000
 *
 * @author Peter Antman
 * @version
 */

public class SpyderMQProvider implements JMSProviderAdapter, java.io.Serializable{
    public static final String TOPIC_CONNECTION_FACTORY="TopicConnectionFactory";
    public static final String QUEUE_CONNECTION_FACTORY="QueueConnectionFactory";
    
    private String name;
    private String url;
    public SpyderMQProvider() {
	
    }
    
    public void setProviderUrl(String url) {this.url = url;}
    public String getProviderUrl() { return url;    }
    public void setName(String name) {this.name = name;}
    public String getName() {return name;}
    public Context getInitialContext() throws NamingException {
	return new InitialContext();//Only for Jboss embedded now

    }
    public  String getTopicFactoryName(){return TOPIC_CONNECTION_FACTORY;}
    public String getQueueFactoryName(){return QUEUE_CONNECTION_FACTORY;}
} // SpyderMQProvider
