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
 * JMSProviderLoader.java
 *
 * This is realy NOT a factory for pool's but simply a way of getting
 * an object instantiated and bound in jndi
 * Created: Wed Nov 29 14:07:07 2000
 *
 * @author 
 * @version
 */

public class JMSProviderLoader extends ServiceMBeanSupport
        implements JMSProviderLoaderMBean {
    JMSProviderAdapter providerAdapter;
    
    public JMSProviderLoader(String name, String jmsProviderAdapterClass) {
        try {
            Class cls = Class.forName(jmsProviderAdapterClass);
            providerAdapter = (JMSProviderAdapter)cls.newInstance();
        } catch(Exception e) {
            Logger.exception(e);
            throw new RuntimeException("Unable to initialize ProviderAdapter '"+name+"': "+e);
        }
	providerAdapter.setName(name);
	
    }

    public void setProviderUrl(String url) {
	providerAdapter.setProviderUrl(url);
    }

    public String getProviderUrl() {
	return providerAdapter.getProviderUrl();
    }
    public ObjectName getObjectName(MBeanServer parm1, ObjectName parm2) throws javax.management.MalformedObjectNameException {
        return (parm2 == null) ? new ObjectName(OBJECT_NAME+",name="+providerAdapter.getName()) : parm2;
    }

    public String getName() {
        return providerAdapter.getName();
    }
    public void startService() throws Exception {
    
        initializeAdapter();
    }

    public void stopService() {
        // Unbind from JNDI
        try {
            String name = providerAdapter.getName();
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
        bind(new InitialContext(), "java:/"+providerAdapter.getName(), providerAdapter);

        log.log("JMS provider Adapter "+providerAdapter.getName()+" bound to java:/"+providerAdapter.getName());

   
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



} // JMSProviderLoader





