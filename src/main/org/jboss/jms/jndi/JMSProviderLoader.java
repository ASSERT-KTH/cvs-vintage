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
 * 6/22/01 - hchirino - The queue/topic jndi references are now configed via JMX
 *
 * @author 
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @version
 */

public class JMSProviderLoader 
   extends ServiceMBeanSupport
   implements JMSProviderLoaderMBean 
{
   JMSProviderAdapter providerAdapter;
   
   String url;
   String providerName;
   String providerAdapterClass;
   String queueFactoryRef;
   String topicFactoryRef;

   public void setProviderName(String name)
   {
	  this.providerName = name;
   }      
   
   public String getProviderName()
   {
	  return providerName;
   }      
   
   public void setProviderAdapterClass(String clazz)
   {
	  providerAdapterClass = clazz;
   }      
   
   public String getProviderAdapterClass()
   {
	  return providerAdapterClass;
   }      
   
   public void setProviderUrl(String url)
   {
	  this.url = url;
   }      

   public String getProviderUrl()
   {
	  return url;
   }      
   public ObjectName getObjectName(MBeanServer parm1, ObjectName parm2) 
	  throws javax.management.MalformedObjectNameException
   {
	  return (parm2 == null) ? new ObjectName(OBJECT_NAME) : parm2;
   }      

   public String getName()
   {
	  return providerName;
   }      
   
   public void initService() 
	  throws Exception
   {
	  Class cls = Class.forName(providerAdapterClass);
	  providerAdapter = (JMSProviderAdapter)cls.newInstance();
	  providerAdapter.setName(providerName);
	  providerAdapter.setProviderUrl(url);
	  providerAdapter.setQueueFactoryRef(queueFactoryRef);
	  providerAdapter.setTopicFactoryRef(topicFactoryRef);
   }            
   
   public void startService() 
	  throws Exception
   {
	  // Bind in JNDI
	  bind(new InitialContext(), "java:/"+providerAdapter.getName(), providerAdapter);

	  log.log("JMS provider Adapter "+providerAdapter.getName()+" bound to java:/"+providerAdapter.getName());
   }      

   public void stopService()
   {
	  // Unbind from JNDI
	  try {
		 String name = providerAdapter.getName();
		 new InitialContext().unbind("java:/"+name);
		 log.log("JMA Provider Adapter "+name+" removed from JNDI");
		 //source.close();
		 //log.log("XA Connection pool "+name+" shut down");
	  } catch (NamingException e)
	  {
		 // Ignore
	  }
   }      

   // Private -------------------------------------------------------
   private void bind(Context ctx, String name, Object val) throws NamingException
   {
	  // Bind val to name in ctx, and make sure that all intermediate contexts exist
	  Name n = ctx.getNameParser("").parse(name);
	  while (n.size() > 1)
	  {
		 String ctxName = n.get(0);
		 try
		 {
			ctx = (Context)ctx.lookup(ctxName);
		 } catch (NameNotFoundException e)
		 {
			ctx = ctx.createSubcontext(ctxName);
		 }
		 n = n.getSuffix(1);
	  }

	  ctx.bind(n.get(0), val);
   }      

/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 12:38:31 AM)
 * @param newQueueFactoryRef java.lang.String
 */
public void setQueueFactoryRef(java.lang.String newQueueFactoryRef) {
	queueFactoryRef = newQueueFactoryRef;
}

/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 12:38:31 AM)
 * @param newTopicFactoryRef java.lang.String
 */
public void setTopicFactoryRef(java.lang.String newTopicFactoryRef) {
	topicFactoryRef = newTopicFactoryRef;
}

/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 12:38:31 AM)
 * @return java.lang.String
 */
public java.lang.String getQueueFactoryRef() {
	return queueFactoryRef;
}

/**
 * Insert the method's description here.
 * Creation date: (6/22/2001 12:38:31 AM)
 * @return java.lang.String
 */
public java.lang.String getTopicFactoryRef() {
	return topicFactoryRef;
}
}