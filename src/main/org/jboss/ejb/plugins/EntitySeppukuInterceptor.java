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
import org.jboss.invocation.InvocationResponse;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.tm.TxUtils;

import javax.jms.DeliveryMode;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Connection;
import javax.jms.JMSException;

import org.w3c.dom.Element;

/**
 * The role of this interceptor is to register synchronizations with the
 * transaction manager when an Entity as changed.  The Synchronization will
 * broadcast a seppuku message through a JMS topic if the change successfully
 * commits
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.7 $
 */
public class EntitySeppukuInterceptor extends AbstractInterceptor
{
   protected HashMap seppukuSynchs = new HashMap();

   protected TopicConnection  conn = null;
   protected TopicSession session = null;
   protected Topic topic = null;
   protected TopicPublisher pub = null;
   protected String connectionFactoryName = null;
   protected String topicName = null;
   protected boolean transacted = true;
   protected int acknowledgeMode = TopicSession.AUTO_ACKNOWLEDGE;

   public void readConfiguration()
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

   protected void initialize()
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
         pub = session.createPublisher(topic);
      }
      catch (Exception ex)
      {
         log.error("Failed to start seppuku interceptor", ex);
      }
   }

   protected synchronized TopicSession getSession()
   {
      return session;
   }

   protected synchronized TopicPublisher getPublisher()
   {
      return pub;
   }

   public void start()
   {
      try
      {
         initialize();
      }
      catch (Exception ex)
      {
         // DAIN: initialize can't throw an exception...
         log.error("Failed to start seppuku interceptor", ex);
      }
   }

   public void stop()
   {
      try
      {
         if(pub != null)
         {
            pub.close();
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

   public InvocationResponse invoke(Invocation mi) throws Exception
   {
      if(mi.getType().isHome())
      {
         return getNext().invoke(mi);
      }

      EntityContainer container = (EntityContainer)getContainer();
      EntityEnterpriseContext ctx = (
            EntityEnterpriseContext)mi.getEnterpriseContext();
      Object id = ctx.getId();

      // The Tx coming as part of the Method Invocation
      Transaction tx = mi.getTransaction();

      if(log.isTraceEnabled())
      {
         log.trace("invoke called for ctx " + ctx + ", tx=" + tx);
      }

      // Invocation with a running Transaction
      if(TxUtils.isActive(tx))
      {
         InvocationResponse returnValue = getNext().invoke(mi);

         // readonly does not synchronize, lock or belong with transaction.
         // nor does it modify data.
         if(!container.isReadOnly())
         {
            Method method = mi.getMethod();
            if(method == null ||
               !container.getBeanMetaData().isMethodReadOnly(method.getName()))
            {
               register(ctx,tx);
            }
         }

         return returnValue;
      }
      else
      {
         // No tx
         InvocationResponse returnValue = getNext().invoke(mi);

         if(ctx.getId() != null)
         {
            if(!container.isReadOnly())
            {
               Method method = mi.getMethod();
               if(method == null ||
                  !container.getBeanMetaData().isMethodReadOnly(method.getName()))
               {
                  if(container.isModified(ctx))
                  {
                     sendSeppukuEvent(ctx.getId());
                  }
               }
            }
         }
         else
         {
            // a remove happened so broadcast seppuku msg
            sendSeppukuEvent(ctx.getId());
         }
         return returnValue;
      }
   }

   protected void register(EntityEnterpriseContext ctx, Transaction tx)
      throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      if(ctx.getId() == null || container.isModified(ctx))
      {
         SeppukuSynchronization synch = null;
         synchronized(seppukuSynchs)
         {
            synch = (SeppukuSynchronization)seppukuSynchs.get(tx);
            if(synch == null)
            {
               synch = new SeppukuSynchronization(tx);
               seppukuSynchs.put(tx, synch);
            }
         }
         synch.addSeppukuEvent(ctx.getId());
         tx.registerSynchronization(synch);
      }
   }

   protected void sendSeppukuSet(HashSet ids) throws Exception
   {
      ObjectMessage msg = getSession().createObjectMessage();
      msg.setJMSType("SEPPUKU_SET");
      msg.setObject(ids);
      getPublisher().publish(msg);
   }

   protected void sendSeppukuEvent(Object id) throws Exception
   {
      HashSet ids = new HashSet();
      ids.add(id);
      sendSeppukuSet(ids);
   }

   protected class SeppukuSynchronization implements Synchronization
   {
      /**
       *  The transaction we follow.
       */
      protected final Transaction tx;

      /**
       *  The context we manage.
       */
      protected HashSet ids = new HashSet();

      /**
       *  Create a new isynchronization instance.
       */
      SeppukuSynchronization(Transaction tx)
      {
         this.tx = tx;
      }

      public void addSeppukuEvent(Object key)
      {
         ids.add(key);
      }

      public void beforeCompletion()
      {
         // complete
      }

      public void afterCompletion(int status)
      {
         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(
               getContainer().getClassLoader());
         try
         {
            if (status != Status.STATUS_ROLLEDBACK)
            {
               try
               {
                  sendSeppukuSet(ids);
               }
               catch (Exception ex)
               {
                  log.error("Failed send seppuku message", ex);
               }
            }
            synchronized (seppukuSynchs)
            {
               seppukuSynchs.remove(tx);
            }
         }
         finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }
   }
}
