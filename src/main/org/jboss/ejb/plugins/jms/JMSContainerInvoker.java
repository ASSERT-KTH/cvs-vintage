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

import org.jboss.jms.ConnectionFactoryHelper;
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
 * @version $Revision: 1.21 $
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
    
   public void setOptimized(final boolean optimize) {
      log.debug("Container Invoker optimize set to " + optimize);
      this.optimize = optimize;
   }

   public boolean isOptimized() {
      log.debug("Optimize in action: " + optimize);
      return optimize;
   }

   public EJBMetaData getEJBMetaData() {
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

   /**
    * Set the container for which this is an invoker to.
    *
    * @param container    The container for which this is an invoker to.
    */
   public void setContainer(final Container container)
   {
      this.container = container;
      //jndiName = container.getBeanMetaData().getJndiName();
   }

   /**
    * Return the JMSProviderAdapter that should be used.
    *
    * @return    The JMSProviderAdapter to use.
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
    * Create a new connection from the given factory.
    *
    * @param factory     An object that implements QueueConnectionFactory,
    *                    XAQueueConnectionFactory, TopicConnectionFactory or
    *                    XATopicConnectionFactory.
    * @param username    The username to use or null for no user.
    * @param password    The password for the given username or null if no
    * @return            A new connection from the given factory.
    * 
    * @throws JMSException    Failed to create connection.
    */
   protected Connection createConnection(final Object factory,
                                         final String username,
                                         final String password)
      throws JMSException
   {
      connection = ConnectionFactoryHelper.createConnection
         (factory, username, password);
      log.debug("created connection: " + connection);
      
      return connection;
   }

   /**
    * Parse the JNDI suffix from the given JNDI name.
    *
    * @param jndiname        The JNDI name used to lookup the destination.
    * @param defaultSuffix   The default suffix to use if parsing fails.
    * @return                The parsed suffix or the defaultSuffix
    */
   protected String parseJndiSuffix(final String jndiname,
                                    final String defautSuffix)
   {
      // jndiSuffix is merely the name that the user has given the MDB.
      // since the jndi name contains the message type I have to split 
      // at the "/" if there is no slash then I use the entire jndi name...
      String jndiSuffix = "";
      if (jndiname != null) {
         int indexOfSlash = jndiname.indexOf("/");
         if (indexOfSlash != -1) {
            jndiSuffix = jndiname.substring(indexOfSlash+1);
         } else {
            jndiSuffix = jndiname;
         }
      }
      else {
         // if the jndi name from jboss.xml is null then lets use the ejbName
         jndiSuffix = defautSuffix;
      }

      return jndiSuffix;
   }
   
   /**
    * Create and or lookup a JMS destination.
    *
    * @param type          Either javax.jms.Queue or javax.jms.Topic.
    * @param ctx           The naming context to lookup destinations from.
    * @param jndiName      The name to use when looking up destinations.
    * @param jndiSuffix    The name to use when creating destinations.
    * @return              The destination.
    *
    * @throws IllegalArgumentException    Type is not Queue or Topic.
    */
   protected Destination createDestination(final Class type,
                                           final Context ctx,
                                           final String jndiName,
                                           final String jndiSuffix)
      throws Exception
   {
      try {
         // first try to look it up
         return (Destination)ctx.lookup(jndiName);
      }
      catch (NamingException e) {
         // if the lookup failes, the try to create it
         log.warn("destination not found: " + jndiName, e);

         //
         // attempt to create the destination (note, this is very
         // very, very unportable).
         //
         MBeanServer server = (MBeanServer)
            MBeanServerFactory.findMBeanServer(null).iterator().next();

         String methodName;
         if (type == Topic.class) {
            methodName = "createTopic";
         }
         else if (type == Queue.class) {
            methodName = "createQueue";
         }
         else {
            // type was not a Topic or Queue, bad user
            throw new IllegalArgumentException
               ("expected javax.jms.Queue or javax.jms.Topic: " + type);
         }

         // invoke the server to create the destination
         server.invoke(new ObjectName("JBossMQ", "service", "Server"),
                       methodName,
                       new Object[] { jndiSuffix },
                       new String[] { "java.lang.String" });

         // try to look it up again
         return (Destination)ctx.lookup(jndiName);
      }
   }

   /**
    * Create a server session pool for the given connection.
    *
    * @param connection      The connection to use.
    * @param maxSession      The maximum number of sessions.
    * @param isTransacted    True if the sessions are transacted.
    * @param ack             The session acknowledgement mode.
    * @param listener        The message listener.
    * @return                A server session pool.
    *
    * @throws JMSException
    */
   protected ServerSessionPool
      createSessionPool(final Connection connection,
                        final int maxSession,
                        final boolean isTransacted,
                        final int ack,
                        final MessageListener listener)
      throws NamingException, JMSException
   {
      ServerSessionPool pool;
      Context context = new InitialContext();
      
      try {
         // first lookup the factory
         log.debug("looking up session pool factory: " +
                   serverSessionPoolFactoryJNDI);
         ServerSessionPoolFactory factory = (ServerSessionPoolFactory)
            context.lookup(serverSessionPoolFactoryJNDI);

         // the create the pool
         pool = factory.getServerSessionPool
            (connection, maxSession, isTransacted, ack, listener);
      }
      finally {
         context.close();
      }

      return pool;
   }

   /**
    * Initialize the container invoker.  Sets up a connection, a server
    * session pool and a connection consumer for the configured destination.
    *
    * @throws Exception    Failed to initalize.
    */
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
      
      // Is containermanages TX (not used?)
      boolean isContainerManagedTx = config.isContainerManagedTx();
      acknowledgeMode = config.getAcknowledgeMode();

      // Get configuration data from jboss.xml
      String destinationJNDI = config.getDestinationJndiName();
      String user = config.getUser();
      String password = config.getPasswd();
      
      // Get the JMS provider
      JMSProviderAdapter adapter = getJMSProviderAdapter();
      log.debug("provider adapter: " + adapter);

      // Connect to the JNDI server and get a reference to root context
      Context context = adapter.getInitialContext();
      log.debug("context: " + context);
      
      // if we can't get the root context then exit with an exception
      if (context == null) {
         throw new RuntimeException("Failed to get the root context");
      }

      // Get the JNDI suffix of the destination
      String jndiSuffix = parseJndiSuffix(destinationJNDI,
                                          config.getEjbName());
      log.debug("jndiSuffix: " + jndiSuffix);
      
      if (destinationType.equals("javax.jms.Topic")) 
      {
         log.debug("Got destination type Topic for " + config.getEjbName());

         // create a topic connection
         Object factory = context.lookup(adapter.getTopicFactoryRef());
         TopicConnection tConnection = 
            (TopicConnection)createConnection(factory, user, password);

         // lookup or create the destination topic
         Topic topic =
            (Topic)createDestination(Topic.class,
                                     context,
                                     "topic/" + jndiSuffix,
                                     jndiSuffix);

         // set up the server session pool
         pool = createSessionPool(tConnection,
                                  maxPoolSize,
                                  true, // tx
                                  acknowledgeMode,
                                  new MessageListenerImpl(this));

         // To be no-durable or durable
         if (config.getSubscriptionDurability() != 
             MessageDrivenMetaData.DURABLE_SUBSCRIPTION) 
         {
            // Create non durable
            connectionConsumer = tConnection.
               createConnectionConsumer(topic, 
                                        messageSelector, 
                                        pool, 
                                        maxMessagesNr); 
         } 
         else {
            //Durable subscription
            String clientId = config.getClientId();
            String durableName =
               clientId != null ? clientId: config.getEjbName();
               
            connectionConsumer = tConnection.
               createDurableConnectionConsumer(topic, 
                                               durableName,
                                               messageSelector, 
                                               pool, 
                                               maxMessagesNr);
         }
            
         log.debug("Topic connectionConsumer set up");
      }
      else if (destinationType.equals("javax.jms.Queue")) 
      {
         log.debug("Got destination type Queue for " + config.getEjbName());
         
         // create a queue connection
         Object qFactory = context.lookup(adapter.getQueueFactoryRef());
         QueueConnection qConnection =
            (QueueConnection)createConnection(qFactory, user, password);

         // lookup or create the destination queue
         Queue queue = 
            (Queue)createDestination(Queue.class,
                                     context,
                                     "queue/" + jndiSuffix,
                                     jndiSuffix);

         // set up the server session pool
         pool = createSessionPool(qConnection,
                                  maxPoolSize,
                                  true, // tx
                                  acknowledgeMode,
                                  new MessageListenerImpl(this));
         log.debug("server session pool: " + pool);
         
         // create the connection consumer
         connectionConsumer = qConnection.
            createConnectionConsumer(queue, 
                                     messageSelector, 
                                     pool, 
                                     maxMessagesNr); 
         log.debug("connection consumer: " + connectionConsumer);
      }

      log.debug("initialized");
   }

   /**
    * Start the connection.
    */
   public void start() throws Exception
   {
      log.debug("Starting JMSContainerInvoker");
      exListener = new ExceptionListenerImpl(this);
      connection.setExceptionListener(exListener);
      connection.start();
   }
    
   /**
    * Stop the connection.
    */
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
            log.debug("unset exception listener");
         }
      } catch (Exception e) {
         log.error("Could not set ExceptionListener to null", e);
      }	
	
      // Stop the connection
      try {
         if (connection != null) {
            connection.stop();
            log.debug("connection stopped");
         }
      } catch (Exception e) {
         log.error("Could not stop JMS connection", e);
      }
   }
    
   /**
    * Take down all fixtures.
    */
   public void destroy()
   {
      log.debug("Destroying JMSContainerInvoker");

      // clear the server session pool (if it is clearable)
      try {
         if (pool instanceof StdServerSessionPool) {
            StdServerSessionPool p = (StdServerSessionPool)pool;
            p.clear();
         }
      } catch (Exception e) {
         log.error("Could not clear ServerSessionPool", e);
      }

      // close the connection consumer
      try {
         if (connectionConsumer != null) {
            connectionConsumer.close();
         }
      } catch (Exception e) {
         log.error("Could not close consumer", e);
      }

      // close the connection
      if (connection != null) {      
         try {
            connection.close();
         } catch (Exception e) {
            log.error("Could not close connection", e);
         }
      }
   }
    
   /**
    * XmlLoadable implementation
    */
   public void importXml(Element element) throws DeploymentException 
   {
      try {
         String maxMessages = MetaData.getElementContent
            (MetaData.getUniqueChild(element, "MaxMessages"));
         maxMessagesNr = Integer.parseInt(maxMessages);
         
         String maxSize = MetaData.getElementContent
            (MetaData.getUniqueChild(element, "MaximumSize"));
         maxPoolSize = Integer.parseInt(maxSize);
      } catch (NumberFormatException e) {
         //Noop will take default value
      } catch (DeploymentException e) {
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

   /**
    * An implementation of MessageListener that passes messages on
    * to the container invoker.
    */
   class MessageListenerImpl
      implements MessageListener
   {
      /** The container invoker. */
      JMSContainerInvoker invoker; // = null;

      /**
       * Construct a <tt>MessageListenerImpl</tt>.
       *
       * @param invoker   The container invoker.  Must not be null.
       */
      MessageListenerImpl(final JMSContainerInvoker invoker) {
         // assert invoker != null;
         
         this.invoker = invoker;
      }

      /**
       * Process a message.
       *
       * @param message    The message to process.
       */
      public void onMessage(final Message message)
      {
         // assert message != null;
         
         if (log.isDebugEnabled()) {
            log.debug("processing message: " + message);
         }
            
         Object id;
         try {
            id = message.getJMSMessageID();
         } catch (JMSException e) {
            // what ?
            id = "JMSContainerInvoke";
         }
            
         // Invoke, shuld we catch any Exceptions??
         try {
            invoker.invoke(id,                     // Object id - where used?
                           ON_MESSAGE,             // Method to invoke
                           new Object[] {message}, // argument
                           tm.getTransaction(),    // Transaction 
                           null,                   // Principal
                           null);                  // Cred
         }
         catch (Exception e) {
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
            catch (Exception e) {
               log.error("MDB error reconnecting", e);
            }
         }
         currentThread = null;
      }
   }
}
