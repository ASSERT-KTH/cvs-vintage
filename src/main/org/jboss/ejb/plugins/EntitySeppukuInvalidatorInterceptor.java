/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.InitialContext;


import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.JMSException;


/**
 * The role of this interceptor is to receive seppuku events and remove ids
 * from cache
 * This really doesn't need to be an interceptor, but couldn't find a better
 * way to have access to the container's cache.
 *
 * FIXME: make this an MBean instead.
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.5 $
 */
public class EntitySeppukuInvalidatorInterceptor extends AbstractInterceptor
      implements MessageListener
{
   protected HashMap seppukuSynchs = new HashMap();

   protected TopicConnection conn;
   protected TopicSession session;
   protected Topic topic;
   protected TopicSubscriber subscriber;
   protected String connectionFactoryName;
   protected String topicName;
   protected boolean transacted;
   protected int acknowledgeMode = TopicSession.AUTO_ACKNOWLEDGE;

   /**
    * When receiving a seppuku message, remove all ids from cache
    */
   public void onMessage(Message message)
   {
      try
      {
         // DAIN:  why is this an info message?
         log.info("Seppuku message received in Invalidator");

         ObjectMessage objectMessage = (ObjectMessage)message;
         HashSet ids = (HashSet)objectMessage.getObject();
         for(Iterator iterator = ids.iterator(); iterator.hasNext(); )
         {
            getContainer().getInstanceCache().remove(iterator.next());
         }
      }
      catch (JMSException ex)
      {
         log.warn(ex.getMessage());
      }
   }

   protected void readConfiguration()
   {
      connectionFactoryName = config.getAttribute("connectionFactory");
      if(connectionFactoryName == null ||
            connectionFactoryName.trim().equals(""))
      {
         connectionFactoryName = "java:/ConnectionFactory";
      }
      connectionFactoryName = connectionFactoryName.trim();

      topicName = config.getAttribute("topic");
      if(topicName == null || topicName.trim().equals(""))
      {
         topicName = "topic/" +
               getContainer().getBeanMetaData().getEjbName() +
               "_seppuku";
      }
      topicName = topicName.trim();

      String strTransacted = config.getAttribute("transacted");
      if(strTransacted == null ||
            "true".equals(strTransacted.toLowerCase().trim()))
      {
         transacted = true;
      }
      else
      {
         transacted = false;
      }

      String strAcknowledgeMode = config.getAttribute("acknowledgeMode");
      if(strAcknowledgeMode != null)
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
         session = conn.createTopicSession(transacted, acknowledgeMode);
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
         // DAIN: initialze can't throw an exception...
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
}
