/**
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.ConfigurationMetaData;

import javax.jms.DeliveryMode;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSubscriber;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Connection;
import javax.jms.JMSException;

import org.w3c.dom.Element;
/**
 * The role of this interceptor is to receive seppuku events and remove ids from cache
 * This really doesn't need to be an interceptor, but couldn't find a better way to
 * have access to the container's cache.
 *
 * FIXME: make this an MBean instead.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.1 $
 */
public class EntitySeppukuInvalidatorInterceptor
   extends AbstractInterceptor
   implements MessageListener
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /**
    *  The container of this interceptor.
    */
   protected EntityContainer container;

   protected HashMap seppukuSynchs = new HashMap();
 
   protected TopicConnection  conn = null;
   protected TopicSession session = null;
   protected Topic topic = null;
   protected TopicSubscriber subscriber = null;
   protected String connectionFactoryName = null;
   protected String topicName = null;
   protected boolean transacted = true;
   protected int acknowledgeMode = TopicSession.AUTO_ACKNOWLEDGE;
   protected Element config = null;

   // Static --------------------------------------------------------
 
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
 

   /**
    * When receiving a seppuku message, remove all ids from cache
    */
   public void onMessage(Message msg)
   {
      try
      {
         log.info("Seppuku message received in Invalidator");
         ObjectMessage objmsg = (ObjectMessage)msg;
         HashSet ids = (HashSet)objmsg.getObject();
         Iterator it = ids.iterator();
         while (it.hasNext())
         {
            container.getInstanceCache().remove(it.next());
         }
      }
      catch (JMSException ex)
      {
         log.warn(ex.getMessage());
      }
   }


   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
   }

   public void setConfiguration(Element config)
   {
      this.config = config;
   }

   protected void readConfiguration()
   {
      connectionFactoryName = config.getAttribute("connectionFactory");
      if (connectionFactoryName == null || connectionFactoryName.trim().equals("")) connectionFactoryName = "java:/ConnectionFactory";
      connectionFactoryName = connectionFactoryName.trim();
      topicName = config.getAttribute("topic");
      if (topicName == null || topicName.trim().equals(""))
      {
         topicName = "topic/" + container.getBeanMetaData().getEjbName() + "_seppuku";
      }
      topicName = topicName.trim();
      String strTransacted = config.getAttribute("transacted");
      if (strTransacted == null || "true".equals(strTransacted.toLowerCase().trim()))
      {
         transacted = true;
      }
      else
      {
         transacted = false;
      }
      String strAcknowledgeMode = config.getAttribute("acknowledgeMode");
      if (strAcknowledgeMode != null)
      {
         if (strAcknowledgeMode.equals("AUTO_ACKNOWLEDGE"))
         {
            acknowledgeMode = TopicSession.AUTO_ACKNOWLEDGE;
         }
         else if (strAcknowledgeMode.equals("CLIENT_ACKNOWLEDGE"))
         {
            acknowledgeMode = TopicSession.CLIENT_ACKNOWLEDGE;
         }
         else if (strAcknowledgeMode.equals("DUPS_OK_ACKNOWLEDGE"))
         {
            acknowledgeMode = TopicSession.DUPS_OK_ACKNOWLEDGE;
         }
      }
      
   }
 
   public void create()
      throws Exception
   {
   }


   protected synchronized void initialize()
   {
      try
      {
         readConfiguration();
         InitialContext iniCtx = new InitialContext();
         Object tmp = iniCtx.lookup(connectionFactoryName);
         TopicConnectionFactory tcf = (TopicConnectionFactory) tmp;
         conn = tcf.createTopicConnection();
         topic = (Topic) iniCtx.lookup(topicName);
         session = conn.createTopicSession(transacted,
                                           acknowledgeMode);
         conn.start();
         subscriber = session.createSubscriber(topic);     
      }
      catch (Exception ex)
      {
         log.error("Failed to start seppuku interceptor", ex);
      }
   }

   public void start()
   {
      try
      {
         initialize();
      }
      catch (Exception ex)
      {
         log.error("Failed to start seppuku interceptor", ex);
      }
   }      

   public void stop()
   {
      try
      {
         if (subscriber != null)
         {
            subscriber.close();
            conn.stop();
            session.close();
            conn.close();
         }
      }
      catch (Exception ex)
      {
         log.error("Failed to stop EntitySeppukuInterceptor: ", ex);
      }
   }
 
   public Container getContainer()
   {
      return container;
   }
 
   // Interceptor implementation --------------------------------------
 
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      Object rtn =  getNext().invokeHome(mi);  
      return rtn;
   }


   public Object invoke(Invocation mi)
      throws Exception
   {
      //Invoke down the chain
      Object retVal = getNext().invoke(mi);  
      return retVal;
   }
}
