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
package org.jboss.jms.asf;

import java.util.Hashtable;

import javax.jms.ServerSessionPool;
import javax.jms.MessageListener;
import javax.jms.Connection;
import javax.jms.JMSException;
/**
 * StdServerSessionPoolFactory.java
 *
 *
 * Created: Fri Dec 22 09:47:41 2000
 *
 * @author Peter Antman
 * @version
 */

public class StdServerSessionPoolFactory implements ServerSessionPoolFactory, java.io.Serializable {
    private Hashtable pools = new Hashtable();
    
    private String name;
    public StdServerSessionPoolFactory() {
	
    }
    public void setName(String name){this.name = name;}
    public String getName(){return name;}
    
    public ServerSessionPool getServerSessionPool(Connection con, int maxSession, boolean isTransacted, int ack, MessageListener listener) throws JMSException {
	/* 
	   This is probably basically fucked up. The ServerSessionPool in
	   OpenJMS is a Singleton. Every one that uses it will end up in
	   the same Connection and against the same messagelistener.
	*/

	// We need a pool, but what should we key on, a guess the adress
	// of the listener is the only really uniqe here
	String key = listener.toString();// Or hash?
	
	if (pools.containsKey(key)) {
	    return (ServerSessionPool)pools.get(key);
	} else {

	    ServerSessionPool pool =  (ServerSessionPool)new StdServerSessionPool(con,  isTransacted, ack, listener,maxSession);
	    pools.put(key, pool);
	    return pool;
	}
    }
} // StdServerSessionPoolFactory
