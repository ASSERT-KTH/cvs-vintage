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
import javax.management.MalformedObjectNameException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import org.apache.log4j.Category;

import org.jboss.configuration.ConfigurationException;
import org.jboss.system.ServiceMBeanSupport;

/**
 * A JMX service to load a JMSProviderAdapter and register it.
 *
 * <p>Created: Wed Nov 29 14:07:07 2000
 * 
 * <p>6/22/01 - hchirino - The queue/topic jndi references are now configed via JMX
 *
 * @author  <a href="mailto:cojonudo14@hotmail.com">Hiram Chirino</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.7 $
 */
public class JMSProviderLoader 
   extends ServiceMBeanSupport
   implements JMSProviderLoaderMBean 
{
   /** Instance logger. */
   private final Category log = Category.getInstance(this.getClass());

   /** The provider adapter which we are loading. */
   protected JMSProviderAdapter providerAdapter;

   /** The provider url. */
   protected String url;

   /** The provider name. */
   protected String providerName;

   /** The provider adapter classname. */
   protected String providerAdapterClass;

   /** The queue factory jndi name. */
   protected String queueFactoryRef;

   /** The topic factory jndi name. */   
   protected String topicFactoryRef;

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
   
   public void setProviderUrl(final String url)
   {
      this.url = url;
   }      

   public String getProviderUrl()
   {
      return url;
   }
   
   public ObjectName getObjectName(MBeanServer parm1, ObjectName parm2) 
      throws MalformedObjectNameException
   {
      return (parm2 == null) ? new ObjectName(OBJECT_NAME) : parm2;
   }      

   public String getName()
   {
      return providerName;
   }      
   
   
   public void startService() throws Exception
   {
      // validate the configuration
      if (queueFactoryRef == null)
         throw new ConfigurationException
            ("missing required attribute: QueueFactoryRef");

      if (topicFactoryRef == null)
         throw new ConfigurationException
            ("missing required attribute: TopicFactoryRef");
       
      Class cls = Class.forName(providerAdapterClass);
      providerAdapter = (JMSProviderAdapter)cls.newInstance();
      providerAdapter.setName(providerName);
      providerAdapter.setProviderUrl(url);
      providerAdapter.setQueueFactoryRef(queueFactoryRef);
      providerAdapter.setTopicFactoryRef(topicFactoryRef);
      InitialContext context = new InitialContext();
      try {
         // Bind in JNDI
         String name = providerAdapter.getName();
         String jndiname = "java:/" + name;
         bind(context, jndiname, providerAdapter);
         log.info("bound adapter " + name + " to " + jndiname);
      }
      finally {
         context.close();
      }
   }      

   public void stopService()
   {
      InitialContext context = null;
      
      try {
         context = new InitialContext();

         // Unbind from JNDI
         String name = providerAdapter.getName();
         String jndiname = "java:/" + name;
         context.unbind(jndiname);
         log.info("unbound adapter " + name + " from " + jndiname);
         
         //source.close();
         //log.log("XA Connection pool "+name+" shut down");
      }
      catch (Exception e) {
         log.warn("failed to unbind; ignoring", e);
      }
      finally {
         if (context != null) {
            try {
               context.close();
            }
            catch (NamingException ignore) {}
         }
      }
   }      

   // Private -------------------------------------------------------
   
   private void bind(Context ctx, String name, Object val)
      throws NamingException
   {
      log.debug("attempting to bind " + val + " to " + name);
      
      // Bind val to name in ctx, and make sure that all
      // intermediate contexts exist
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
    * @param newQueueFactoryRef String
    */
   public void setQueueFactoryRef(final String newQueueFactoryRef) {
      queueFactoryRef = newQueueFactoryRef;
   }

   /**
    * Insert the method's description here.
    * Creation date: (6/22/2001 12:38:31 AM)
    * @param newTopicFactoryRef String
    */
   public void setTopicFactoryRef(final String newTopicFactoryRef) {
      topicFactoryRef = newTopicFactoryRef;
   }

   /**
    * Insert the method's description here.
    * Creation date: (6/22/2001 12:38:31 AM)
    * @return String
    */
   public String getQueueFactoryRef() {
      return queueFactoryRef;
   }

   /**
    * Insert the method's description here.
    * Creation date: (6/22/2001 12:38:31 AM)
    * @return String
    */
   public String getTopicFactoryRef() {
      return topicFactoryRef;
   }
}
