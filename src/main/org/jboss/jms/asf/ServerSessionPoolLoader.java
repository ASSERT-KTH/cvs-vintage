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
package org.jboss.jms.asf;

import javax.management.ObjectName;
import javax.management.MBeanServer;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import org.jboss.util.ServiceMBeanSupport;
import org.jboss.logging.Logger;

/**
 * ServerSessionPoolLoader.java
 *
 *
 * Created: Wed Nov 29 16:14:46 2000
 *
 * @author 
 * @version
 */

public class ServerSessionPoolLoader extends ServiceMBeanSupport
        implements ServerSessionPoolLoaderMBean{
    private ServerSessionPoolFactory poolFactory;
    public ServerSessionPoolLoader(String name, String poolFactoryClass) {
        try {
            Class cls = Class.forName(poolFactoryClass);
            poolFactory = (ServerSessionPoolFactory)cls.newInstance();
        } catch(Exception e) {
            Logger.exception(e);
            throw new RuntimeException("Unable to initialize ServerSessionPoolFactory '"+name+"': "+e);
        }
	poolFactory.setName(name);
	
	
    }
     public ObjectName getObjectName(MBeanServer parm1, ObjectName parm2) throws javax.management.MalformedObjectNameException {
        return (parm2 == null) ? new ObjectName(OBJECT_NAME+",name="+poolFactory.getName()) : parm2;
    }

    public String getName() {
        return poolFactory.getName();
    }
    public void startService() throws Exception {
    
        initializeAdapter();
    }

    public void stopService() {
        // Unbind from JNDI
        try {
            String name = poolFactory.getName();
            new InitialContext().unbind("java:/"+name);
            log.log("JMA Provider Adapter "+name+" removed from JNDI");
            //source.close();
            //log.log("XA Connection pool "+name+" shut down");
        } catch (NamingException e) {
            // Ignore
        }
    }

	// Private -------------------------------------------------------

    private void initializeAdapter() throws NamingException {
        Context ctx = null;
        Object mgr = null;

	/*
        source.setTransactionManagerJNDIName("java:/TransactionManager");
        try {
            ctx = new InitialContext();
            mgr = ctx.lookup("java:/TransactionManager");
        } catch(NamingException e) {
            throw new IllegalStateException("Cannot start XA Connection Pool; there is no TransactionManager in JNDI!");
        }
        source.initialize();
	*/

        // Bind in JNDI
        bind(new InitialContext(), "java:/"+poolFactory.getName(),poolFactory);

        log.log("JMS provider Adapter "+poolFactory.getName()+" bound to java:/"+poolFactory.getName());

   
    }

    private void bind(Context ctx, String name, Object val) throws NamingException {
        // Bind val to name in ctx, and make sure that all intermediate contexts exist
        Name n = ctx.getNameParser("").parse(name);
        while (n.size() > 1) {
            String ctxName = n.get(0);
            try {
                ctx = (Context)ctx.lookup(ctxName);
            } catch (NameNotFoundException e) {
                ctx = ctx.createSubcontext(ctxName);
            }
            n = n.getSuffix(1);
        }

        ctx.bind(n.get(0), val);
    }
    public static void main(String[] args) {
	
    }
    
} // ServerSessionPoolLoader
