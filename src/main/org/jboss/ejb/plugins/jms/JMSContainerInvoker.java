/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jms;

import java.util.Collection;
import java.util.Hashtable;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.jms.*;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;

import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.w3c.dom.Element;

import org.apache.log4j.Category;

import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.DeploymentException;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.MessageDrivenMetaData;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.jms.asf.ServerSessionPoolFactory;

import org.jboss.jms.asf.StdServerSessionPool;

/**
 * ContainerInvoker for JMS MessageDrivenBeans, based on JRMPContainerInvoker.
 *   
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.17 $
 */
public class JMSContainerInvoker
   implements ContainerInvoker, XmlLoadable
{
   // Constants -----------------------------------------------------

   /** {@link MessageListener#onMessage} reference. */
   protected static /* final */ Method ON_MESSAGE;

   /**
    * Initialize the ON_MESSAGE reference.
    */
   static {
      try {
         final Class type = MessageListener.class;
         final Class arg = Message.class;
         ON_MESSAGE = type.getMethod("onMessage", new Class[] { arg });
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }

   /** Instance logger. */
   private final Category log = Category.getInstance(this.getClass());
    
   // Attributes ----------------------------------------------------
    
   protected boolean optimize; // = false;
   protected int maxMessagesNr = 1;
   protected int maxPoolSize = 15;
   protected String providerAdapterJNDI;
   protected String serverSessionPoolFactoryJNDI;
   protected int acknowledgeMode;
   protected Container container;
   protected Connection connection;
   protected ConnectionConsumer connectionConsumer;
   protected TransactionManager tm;
   protected ServerSessionPool pool;
   protected ExceptionListenerImpl exListener;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
    
   public void setOptimized(boolean optimize)
   {
      log.debug("Container Invoker optimize set to " + optimize);
      this.optimize = optimize;
   }

   public boolean isOptimized()
   {
      log.debug("Optimize in action: " + optimize);
      return optimize;
   }

   public EJBMetaData getEJBMetaData()
   {
      throw new Error("Not valid for MessageDriven beans");
   }

   // ContainerInvoker implementation
    
   public EJBHome getEJBHome() {
      throw new Error("Not valid for MessageDriven beans");
   }

   public EJBObject getStatelessSessionEJBObject() {
      throw new Error("Not valid for MessageDriven beans");
   }

   public EJBObject getStatefulSessionEJBObject(Object id) {
      throw new Error("Not valid for MessageDriven beans");
   }

   public EJBObject getEntityEJBObject(Object id) {
      throw new Error("Not valid for MessageDriven beans");
   }

   public Collection getEntityCollection(Collection ids) {
      throw new Error("Not valid for MessageDriven beans");
   }

   public Object invoke(Object id,
                        Method m,
                        Object[] args,
                        Transaction tx,
                        Principal identity,
                        Object credential)
      throws Exception
   {
      MethodInvocation mi =
         new MethodInvocation(id, m, args, tx, identity, credential);
       
      // Set the right context classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
       
      try {
         return container.invoke(mi);
      }
      finally {	   
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   // ContainerService implementation -------------------------------
    
   public void setContainer(final Container container)
   {
      this.container = container;
      //jndiName = container.getBeanMetaData().getJndiName();
   }

   /**
    * Return the JMSProviderAdapter that should be used.
    */
   protected JMSProviderAdapter getJMSProviderAdapter()
      throws NamingException
   {
      Context context = new InitialContext();
      try {
         log.debug("looking up provider adapter: " + providerAdapterJNDI);
         return (JMSProviderAdapter)context.lookup(providerAdapterJNDI);
      }
      finally {
         context.close();
      }
   }

   /**
    * Return the ServerSessionPoolFactory that should be used.
    */
   protected ServerSessionPoolFactory getServerSessionPoolFactory()
      throws NamingException
   {
      Context context = new InitialContext();
      try {
         log.debug("looking up session pool factory: " +
                   serverSessionPoolFactoryJNDI);
         return (ServerSessionPoolFactory)
            context.lookup(serverSessionPoolFactoryJNDI);
      }
      finally {
         context.close();
      }
   }

   public void init() throws Exception
   {
      log.debug("initializing");
      
      // Store TM reference locally - should we test for CMT Required
      tm = container.getTransactionManager();

      // Get configuration information - from EJB-xml
      MessageDrivenMetaData config =
         ((MessageDrivenMetaData)container.getBeanMetaData());
       
      // Selector
      String messageSelector = config.getMessageSelector();
      
      // Queue or Topic
      String destinationType = config.getDestinationType();
      
      // Is containermanages TX
      boolean isContainerManagedTx = config.isContainerManagedTx();

      acknowledgeMode = config.getAcknowledgeMode();

      // Get configuration data from jboss.xml
      String destinationJNDI = config.getDestinationJndiName();
      String user = config.getUser();

      // Set upp JNDI
      // connect to the JNDI server and get a reference to root context
      Context context = null;

      JMSProviderAdapter adapter = getJMSProviderAdapter();
      log.debug("provider adapter: " + adapter);
      
      context = adapter.getInitialContext();
      log.debug("context: " + context);
      
      // if we can't get the root context then exit with an exception
      if (context == null)
      {
         throw new RuntimeException("Failed to get the root context");
      }
       
      // Set up pool
      ServerSessionPoolFactory poolFactory = getServerSessionPoolFactory();

      // jndiSuffix is merely the name that the user has given the MDB.
      // since the jndi name contains the message type I have to split 
      // at the "/" if there is no slash then I use the entire jndi name...
      String jndiSuffix = "";
      if (destinationJNDI != null) {
         int indexOfSlash = destinationJNDI.indexOf("/");
         if (indexOfSlash != -1) {
            jndiSuffix = destinationJNDI.substring(indexOfSlash+1);
         } else {
            jndiSuffix = destinationJNDI;
         }
         // if the jndi name from jboss.xml is null then lets use the ejbName
      } else {
         jndiSuffix = config.getEjbName();
      }
      log.debug("jndiSuffix: " + jndiSuffix);
      
      MBeanServer server = (MBeanServer)
         MBeanServerFactory.findMBeanServer(null).iterator().next();
       
      if (destinationType.equals("javax.jms.Topic")) 
      {
         log.debug("Got destination type Topic for " + config.getEjbName());
	       
         // All classes are different between topics and queues!!
         TopicConnectionFactory topicFactory = 
            (TopicConnectionFactory)context.
            lookup(adapter.getTopicFactoryRef());
            
         // Do we have a user - this is messy code (should be done for queues to)
         TopicConnection topicConnection;
         if(user != null) 
         {
            log.debug("Creating topic connection with user: " + 
                      user + " passwd: " + config.getPasswd());
            topicConnection = topicFactory.
               createTopicConnection(user, config.getPasswd());
         }
         else 
         {
            topicConnection = topicFactory.createTopicConnection();
         }
	       
         // Lookup destination
         // First Try a lookup.
         // If that lookup fails then try to contact the MBeanServer and inoke a new...
         // Then do lookup again..
         String topicJndi = "topic/"+jndiSuffix;
         Topic topic;
         try {
            topic = (Topic)context.lookup(topicJndi);
         } catch(NamingException ne) {
            log.error("JndiName not found:"+topicJndi +
                      "...attempting to recover", ne);
            server.invoke(new ObjectName("JMS","service","JMSServer"),
                          "newTopic", new Object[]{jndiSuffix},
                          new String[] {"java.lang.String"});
            topic = (Topic)context.lookup(topicJndi);
         }
            
         pool = poolFactory.getServerSessionPool
            (topicConnection,
             maxPoolSize,
             //Transacted
             true, 
             acknowledgeMode, 
             new MessageListenerImpl(this));

         // To be no-durable or durable
         if (config.getSubscriptionDurability() != 
             MessageDrivenMetaData.DURABLE_SUBSCRIPTION) 
         {
            // Create non durable
            connectionConsumer = topicConnection.
               createConnectionConsumer(topic, 
                                        messageSelector, 
                                        pool, 
                                        maxMessagesNr); 
         } 
         else 
         {
            //Durable subscription
            String clientId = config.getClientId();
            String durableName = clientId != null ? clientId:
                    
               config.getEjbName();
            connectionConsumer = topicConnection.
               createDurableConnectionConsumer(topic, 
                                               durableName,
                                               messageSelector, 
                                               pool, 
                                               maxMessagesNr);
         }
            
         // set global connection, so we have something to
         // start() and close()
         connection = topicConnection;
         log.debug("Topic connectionConsumer set up");

      }
      else if (destinationType.equals("javax.jms.Queue")) 
      {
         log.debug("Got destination type Queue");
         QueueConnectionFactory queueFactory = 
            (QueueConnectionFactory)context.lookup(adapter.getQueueFactoryRef());
	       
         // Do we have a user
         QueueConnection queueConnection;
         if (user != null) 
         {
            queueConnection = queueFactory.
               createQueueConnection(user, config.getPasswd());
         } 
         else 
         {
            queueConnection = queueFactory.createQueueConnection();
         }

         // Lookup destination
         // First Try a lookup.
         // If that lookup fails then try to contact the MBeanServer and inoke a new...
         // Then do lookup again..
         String queueJndi = "queue/"+jndiSuffix;
         Queue queue;
         try {
            queue = (Queue)context.lookup(queueJndi);
         } catch(NamingException ne) {
            log.error("JndiName not found:"+queueJndi +
                      "...attempting to recover", ne);
            server.invoke(new ObjectName("JMS:service=JMSServer"),
                          "newQueue", new Object[]{jndiSuffix},
                          new String[] {"java.lang.String"});
            queue = (Queue)context.lookup(queueJndi);
         }
	       
         pool = poolFactory.
            getServerSessionPool(queueConnection,
                                 maxPoolSize, 
                                 //Transacted
                                 true, 
                                 acknowledgeMode, 
                                 new MessageListenerImpl(this));

         connectionConsumer = queueConnection.
            createConnectionConsumer(queue, 
                                     messageSelector, 
                                     pool, 
                                     maxMessagesNr); 

         // set global connection, so we have something to
         // start() and close()
         connection = queueConnection;
         log.debug("Queue connectionConsumer set up");
      }
   }

   // Start the connection
   public void start()
      throws Exception
   {
      log.debug("Starting JMSContainerInvoker");
      exListener = new ExceptionListenerImpl(this);
      connection.setExceptionListener(exListener);
      connection.start();
   }
    
   // Stop the connection
   public void stop()
   {
      log.debug("Stopping JMSContainerInvoker");
      // Silence the exception listener
      if (exListener != null) {
         exListener.stop();
      }
      innerStop();
	
   }

   /**
    * Stop done from inside, we should not stop the 
    * exceptionListener in inner stop.
    */
   protected void innerStop() {
      try {
         if (connection != null) {
            connection.setExceptionListener(null);
         }
      } catch (Exception e) {
         log.error("Could not set ExceptionListener to null", e);
      }	
	
      // Stop the connection
      try {
         if (connection != null) {
            connection.stop();
         }
      } catch (Exception e) {
         log.error("Could not stop JMS connection", e);
      }
   }
    
    
   // Take down all fixtures
   public void destroy()
   {
      log.debug("Destroying JMSContainerInvoker");
      try {
         if (pool instanceof StdServerSessionPool) {
            StdServerSessionPool p = (StdServerSessionPool)pool;
            p.clear();
         }
      } catch (Exception e) {
         log.error("Could not clear ServerSessionPool", e);
      }
		
      try {
         if (connectionConsumer != null) {
            connectionConsumer.close();
         }
      } catch(Exception e) {
         log.error("Could not close consumer", e);
      }
        
      try {
         if (connection != null) {
            connection.close();
         }
      } catch(Exception e) {
         log.error("Could not close connection", e);
      }
   }
    
   // XmlLoadable implementation
    
   public void importXml(Element element) throws DeploymentException 
   {
      try {
         String maxMessages = MetaData.getElementContent
            (MetaData.getUniqueChild(element, "MaxMessages"));
         maxMessagesNr = Integer.parseInt(maxMessages);
         
         String maxSize = MetaData.getElementContent
            (MetaData.getUniqueChild(element, "MaximumSize"));
         maxPoolSize = Integer.parseInt(maxSize);
      } catch(NumberFormatException e) {
         //Noop will take default value
      } catch(DeploymentException e) {
         //Noop will take default value
      }
	
      // If these are not found we will get a DeploymentException, I hope
      providerAdapterJNDI = MetaData.getElementContent
         (MetaData.getUniqueChild(element, "JMSProviderAdapterJNDI"));
        
      serverSessionPoolFactoryJNDI = MetaData.getElementContent
         (MetaData.getUniqueChild(element, "ServerSessionPoolFactoryJNDI"));
	
      // Check java:/ prefix
      if (!providerAdapterJNDI.startsWith("java:/"))
         providerAdapterJNDI = "java:/"+providerAdapterJNDI;
        
      if (!serverSessionPoolFactoryJNDI.startsWith("java:/"))
         serverSessionPoolFactoryJNDI = "java:/"+serverSessionPoolFactoryJNDI;
   }
    
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
    
   // Inner classes -------------------------------------------------
    
   class MessageListenerImpl
      implements MessageListener
   {
      JMSContainerInvoker invoker = null;
	
      MessageListenerImpl(final JMSContainerInvoker invoker) {
         this.invoker = invoker;
      }
        
      public void onMessage(Message message)
      {
         if (log.isDebugEnabled()) {
            log.debug("processing message: " + message);
         }
            
         Object id;
         try {
            id = message.getJMSMessageID();
         } catch(javax.jms.JMSException e) {
            id = "JMSContainerInvoke";
         }
            
         // Invoke, shuld we catch any Exceptions??
         try {
            invoker.invoke(// Object id - where used?
                           id,
                           // Method to invoke
                           ON_MESSAGE,
                           //argument
                           new Object[] {message},
                           //Transaction 
                           tm.getTransaction(),
                           //Principal
                           null,
                           //Cred
                           null);
         } catch(Exception e) {
            log.error("Exception in JMSCI message listener", e);
         }
      }
   }

   /**
    * ExceptionListener for failover handling.
    */
   class ExceptionListenerImpl
      implements ExceptionListener
   {
      JMSContainerInvoker invoker; // = null;
      Thread currentThread; // = null;
      boolean notStoped; // = true;
	
      ExceptionListenerImpl(final JMSContainerInvoker invoker) {
         this.invoker = invoker;
      }

      void stop() {
         log.debug("stop requested");
            
         notStoped = false;
         if (currentThread != null) {
            currentThread.interrupt();
            log.debug("current thread interrupted");
         }
      }

      public void onException(JMSException ex) {
         currentThread = Thread.currentThread();
	    
         log.warn("MDB lost connection to provider", ex);
         boolean tryIt = true;
         while(tryIt && notStoped) {
            log.info("MDB Trying to reconnect...");
            try {
               try {
                  Thread.sleep(10000);
               } catch (InterruptedException ie) {
                  tryIt=false; return;
               }
                    
               // Reboot container
               invoker.innerStop();
               invoker.destroy();
               invoker.init();
               invoker.start();
               tryIt = false;
               log.info("OK - reconnected");
            }
            catch(Exception e) {
               log.error("MDB error reconnecting", e);
            }
         }
         currentThread = null;
      }
   }
}
