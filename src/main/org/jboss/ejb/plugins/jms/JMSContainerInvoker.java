/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.ejb.plugins.jms;

import java.lang.reflect.Method;
import java.security.Principal;

import javax.jms.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;

import javax.transaction.Transaction;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.ConfigurationException;
import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;
import org.jboss.util.TCLStack;

import org.jboss.ejb.Container;
import org.jboss.ejb.EJBProxyFactory;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import org.jboss.jms.asf.ServerSessionPoolFactory;
import org.jboss.jms.jndi.JMSProviderAdapter;

import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.ejb.plugins.TxSupport;

/**
 * EJBProxyFactory for JMS MessageDrivenBeans.
 * 
 * @version <tt>$Revision: 1.53 $</tt>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class JMSContainerInvoker
   extends ServiceMBeanSupport
   implements EJBProxyFactory
{
   /** {@link MessageListener#onMessage} reference. */
   protected static Method ON_MESSAGE;
   
   /**
    * Default destination type.
    *
    * <p>
    * Used when no message-driven-destination is given in ejb-jar,
    * and a lookup of destinationJNDI from jboss.xml is not successfull.
    */
   protected final static String DEFAULT_DESTINATION_TYPE = "javax.jms.Topic";

   /**
    * Initialize the ON_MESSAGE reference.
    */
   static
   {
      try
      {
         final Class type = MessageListener.class;
         final Class arg = Message.class;
         ON_MESSAGE = type.getMethod("onMessage", new Class[]{ arg });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   /** Maximum number provider is allowed to stuff into a session. */
   protected int maxMessages = 1;

   /** Maximun pool size of server sessions. */
   protected int maxPoolSize = 15;
   
   /** Time to wait before retrying to reconnect a lost connection. */
   protected long reconnectInterval = 10000;
   
   /** If Dead letter queue should be used or not. */
   protected boolean useDLQ = false;
   
   /**
    * JNDI name of the provider adapter.
    * 
    * @see org.jboss.jms.jndi.JMSProviderAdapter
    */
   protected String providerAdapterJNDI;
   
   /**
    * JNDI name of the server session factory.
    * 
    * @see org.jboss.jms.asf.ServerSessionPoolFactory
    */
   protected String serverSessionPoolFactoryJNDI;

   /** Flag passed to the session pool factory. */
   protected boolean useLocalTx;

   /** The container. */
   protected Container container;
   
   /** The JMS connection. */
   protected Connection connection;

   /** The JMS connection consumer. */
   protected ConnectionConsumer connectionConsumer;

   /** The JMS server session pool. */
   protected ServerSessionPool pool;

   /** The exception listener to handle reconnecting to resources on failure. */
   protected ExceptionListenerImpl exListener;

   /** Dead letter queue handler. */
   protected DLQHandler dlqHandler;

   /** DLQConfig element from MDBConfig element from jboss.xml. */
   protected Element dlqConfig;

   protected InvokerProxyBindingMetaData invokerMetaData;
   protected String invokerBinding;
   
   /**
    * Set the invoker meta data so that the ProxyFactory can initialize properly
    */
   public void setInvokerMetaData(InvokerProxyBindingMetaData imd)
   {
      invokerMetaData = imd;
   }
   
   /**
    * Set the invoker jndi binding
    */
   public void setInvokerBinding(String binding)
   {
      invokerBinding = binding;
   }

   public String toString()
   {
      return super.toString() + 
         "{ maxMessages=" + maxMessages +
         ", maxPoolSize=" + maxPoolSize +
         ", reconnectInterval=" + reconnectInterval +
         ", providerAdapterJNDI=" + providerAdapterJNDI +
         ", serverSessionPoolFactoryJNDI=" + serverSessionPoolFactoryJNDI +
         ", useLocalTx=" + useLocalTx +
         ", useDLQ=" + useDLQ +
         ", dlqHandler=" + dlqHandler +
         " }";
   }   
   

   /////////////////////////////////////////////////////////////////////////
   //                   ContainerPlugin/EJBProxyFactory                   //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * Set the container for which this is an invoker to.
    *
    * @param container  The container for which this is an invoker to.
    */
   public void setContainer(final Container container)
   {
      this.container = container;
   }
   
   /**
    * Always throws an Error
    * 
    * @throws Error Not valid for MDB
    */
   public Object getEJBHome()
   {
      throw new Error("Not valid for MessageDriven beans");
   }

   /**
    * Always throws an Error
    * 
    * @throws Error Not valid for MDB
    */
   public javax.ejb.EJBMetaData getEJBMetaData()
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   /**
    * Always throws an Error
    * 
    * @throws Error Not valid for MDB
    */
   public java.util.Collection getEntityCollection(java.util.Collection ids)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   /**
    * Always throws an Error
    * 
    * @throws Error Not valid for MDB
    */
   public Object getEntityEJBObject(Object id)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   /**
    * Always throws an Error
    * 
    * @throws Error Not valid for MDB
    */
   public Object getStatefulSessionEJBObject(Object id)
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   
   /**
    * Always throws an Error
    * 
    * @throws Error Not valid for MDB
    */
   public Object getStatelessSessionEJBObject()
   {
      throw new Error("Not valid for MessageDriven beans");
   }
   

   /////////////////////////////////////////////////////////////////////////
   //                       XmlLoadable/Configuration                     //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * FIXME - we ought to move all config into MDBConfig, but I do not
    * do that now due to backward compatibility.
    */
   public void importXml(final Element element) throws Exception
   {
      String temp;

      // If these are not found we will get a DeploymentException, I hope
      providerAdapterJNDI = MetaData.getElementContent
         (MetaData.getUniqueChild(element, "JMSProviderAdapterJNDI"));
      
      serverSessionPoolFactoryJNDI = MetaData.getElementContent
         (MetaData.getUniqueChild(element, "ServerSessionPoolFactoryJNDI"));
      
      try {
         temp = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaxMessages"));
         maxMessages = Integer.parseInt(temp);
      }
      catch (Exception ignore) {}
      
      try {
         temp = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
         maxPoolSize = Integer.parseInt(temp);
      }
      catch (Exception ignore) {}
         
      Element mdbConfig = MetaData.getUniqueChild(element, "MDBConfig");
         
      try {
         temp = MetaData.getElementContent
            (MetaData.getUniqueChild(mdbConfig, "ReconnectInterval"));
         reconnectInterval = Long.parseLong(temp);
      }
      catch (Exception ignore) {}
         
      // Get Dead letter queue config - and save it for later use
      dlqConfig = MetaData.getOptionalChild(mdbConfig, "DLQConfig");
      if (dlqConfig != null)
      {
         dlqConfig = (Element)((Node)dlqConfig).cloneNode(true);
         useDLQ = true;
      }
      else
      {
         useDLQ = false;
      }
   }

   
   /////////////////////////////////////////////////////////////////////////
   //                            Service Support                          //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * Initialize the container invoker. Sets up a connection, a server session
    * pool and a connection consumer for the configured destination.
    * 
    * <p>
    * Any JMSExceptions produced while initializing will be assumed to be
    * caused due to JMS Provider failure.
    *
    * @throws Exception  Failed to initalize.
    */
   protected void createService() throws Exception
   {
      importXml(invokerMetaData.getProxyFactoryConfig());

      exListener = new ExceptionListenerImpl(this);
   }

   /**
    * Try to get a destination type by looking up the destination JNDI, or
    * provide a default if there is not destinationJNDI or if it is not possible
    * to lookup.
    *
    * @param ctx              The naming context to lookup destinations from.
    * @param destinationJNDI  The name to use when looking up destinations.
    * @return                 The destination type, either derived from
    *                         destinationJDNI or DEFAULT_DESTINATION_TYPE
    */
   protected String getDestinationType(Context ctx, String destinationJNDI)
   {
      String destType = null;
      
      if (destinationJNDI != null)
      {
         try
         {
            Destination dest = (Destination)ctx.lookup(destinationJNDI);
            if (dest instanceof javax.jms.Topic)
            {
               destType = "javax.jms.Topic";
            }
            else if (dest instanceof javax.jms.Queue)
            {
               destType = "javax.jms.Queue";
            }
         }
         catch (NamingException ex)
         {
            log.debug("Could not do heristic lookup of destination ", ex);
         }
      }
      
      if (destType == null)
      {
         //
         // jason: should throw an exception, user should specify this (screw the spec)
         //
         
         destType = DEFAULT_DESTINATION_TYPE;
         log.warn("Could not determine destination type; using default: " + destType);
      }
      
      return destType;
   }
   
   /**
    * Initialize the container invoker. Sets up a connection, a server session
    * pool and a connection consumer for the configured destination.
    *
    * @throws Exception  Failed to initalize.
    */
   protected void connect() throws Exception
   {
      log.debug("Connecting...");

      Context context = new InitialContext();
      log.debug("Initial context: " + context);

      JMSProviderAdapter adapter;
      try {
         // Get the JMS provider
         adapter = (JMSProviderAdapter)context.lookup(providerAdapterJNDI);
         log.debug("Provider adapter: " + adapter);
      }
      finally {
         context.close();
      }
      
      // Set up Dead Letter Queue handler  
      if (useDLQ)
      {
         dlqHandler = new DLQHandler(adapter);
         dlqHandler.importXml(dlqConfig);
         dlqHandler.create();
      }
      
      // Get configuration information - from EJB-xml
      MessageDrivenMetaData config =
         ((MessageDrivenMetaData)container.getBeanMetaData());
      
      // Queue or Topic - optional unfortunately
      String destinationType = config.getDestinationType();
      
      // Should we used local tranactions
      TxSupport txType = config.getMethodTransactionType("onMessage", 
                                         new Class[]{ Message.class }, 
                                         InvocationType.LOCAL);
      boolean isNotSupportedTx = (txType == TxSupport.NOT_SUPPORTED); 
      useLocalTx = !config.isContainerManagedTx() || isNotSupportedTx;

      boolean useXAConnection = config.getUseXAConnection();
      log.debug("XA connection will be used: " + useXAConnection);

      boolean sessionPoolTransacted = config.getSessionPoolTransacted();
      log.debug("Session pool will be transacted: " + sessionPoolTransacted);

      // Connect to the JNDI server and get a reference to root context
      context = adapter.getInitialContext();
      log.debug("Provider context: " + context);
      
      // if we can't get the root context then exit with an exception
      if (context == null)
      {
         throw new RuntimeException("Failed to get the root context");
      }
      
      // Unfortunately the destination is optional, so if we do not have one
      // here we have to look it up if we have a destinationJNDI, else give it
      // a default.
      if (destinationType == null)
      {
         log.warn("No message-driven-destination given; guessing type");
         destinationType = getDestinationType(context, config.getDestinationJndiName());
      }

      Destination destination = (Destination)context.lookup(config.getDestinationJndiName());
      log.debug("Using destination: " + destination);
      
      boolean isTopic;
      if (destinationType.equals("javax.jms.Topic")) {
         isTopic = true;
      }
      else if (destinationType.equals("javax.jms.Queue")) {
         isTopic = false;
      }
      else {
         throw new ConfigurationException("Invalid destination type: " + destinationType);
      }

      String username = config.getUsername();
      String password = config.getPassword();

      if (username == null) {
         log.debug("Using default user identity for connection; username is null");
      }
      else {
         log.debug("Using username: " + username);
      }
      
      // Create a connection for the topic or queue
      if (isTopic) {
         Object factory = context.lookup(adapter.getTopicFactoryRef());

         if (username == null) {
            if (useXAConnection) {
               connection =
                  ((XATopicConnectionFactory)factory).createXATopicConnection();
            }
            else {
               connection =
                  ((TopicConnectionFactory)factory).createTopicConnection();
            }
         }
         else {
            if (useXAConnection) {
               connection =
                  ((XATopicConnectionFactory)factory).
                  createXATopicConnection(username, password);
            }
            else {
               connection =
                  ((TopicConnectionFactory)factory).
                  createTopicConnection(username, password);
            }
         }
      }
      else { // Queue
         Object factory = context.lookup(adapter.getQueueFactoryRef());

         if (username == null) {
            if (useXAConnection) {
               connection =
                  ((XAQueueConnectionFactory)factory).createXAQueueConnection();
            }
            else {
               connection =
                  ((QueueConnectionFactory)factory).createQueueConnection();
            }
         }
         else {
            if (useXAConnection) {
               connection =
                  ((XAQueueConnectionFactory)factory).
                  createXAQueueConnection(username, password);
            }
            else {
               connection =
                  ((QueueConnectionFactory)factory).
                  createQueueConnection(username, password);
            }
         }
      }
      log.debug("Using connection: " + connection);

      // Setup the ClientID
      String id = config.getClientId();
      if (id != null && id.length() > 0) {
         log.debug("Using client id: " + id);
         connection.setClientID(id);
      }

      // Setup the session pool
      ServerSessionPoolFactory factory = (ServerSessionPoolFactory)
         context.lookup(serverSessionPoolFactoryJNDI);
      log.debug("Using session pool factory: " + factory);

      // We don't need this anymore
      context.close();
      
      pool = factory.getServerSessionPool(connection,
                                          maxPoolSize,
                                          sessionPoolTransacted,
                                          config.getAcknowledgeMode(),
                                          useLocalTx,
                                          new MessageListenerImpl(this));
      log.debug("Using session pool: " + pool);

      // Setup the connection consumer
      if (isTopic) {
         // To be no-durable or durable
         if (config.getSubscriptionDurability() != MessageDrivenMetaData.DURABLE_SUBSCRIPTION)
         {
            // Create non durable
            connectionConsumer =
               ((TopicConnection)connection).
               createConnectionConsumer((Topic)destination,
                                        config.getMessageSelector(),
                                        pool,
                                        maxMessages);
         }
         else
         {
            // Durable subscription
            connectionConsumer =
               ((TopicConnection)connection).
               createDurableConnectionConsumer((Topic)destination,
                                               config.getSubscriptionId(),
                                               config.getMessageSelector(),
                                               pool,
                                               maxMessages);
         }
      }
      else { // Queue
         connectionConsumer =
            ((QueueConnection)connection).
            createConnectionConsumer((Queue)destination,
                                     config.getMessageSelector(),
                                     pool,
                                     maxMessages);
      }
      log.debug("Using connection consumer: " + connectionConsumer);

      log.debug("Connected");
   }
   
   protected void startService() throws Exception
   {
      try
      {
         connect();
      }
      catch (final JMSException e)
      {
      	 //
      	 // start a thread up to handle recovering the connection. so we can
         // attach to the jms resources once they become available
         //
      	 new Thread("JMSContainerInvoker Create Recovery Thread")
         {
            public void run()
            {
               exListener.onException(e);
            }
      	 }.start();
      }

      if (dlqHandler != null)
      {
         dlqHandler.start();
      }
      
      if (connection != null)
      {
         connection.setExceptionListener(exListener);
         connection.start();
      }
   }

   protected void stopService() throws Exception
   {
      // Silence the exception listener
      if (exListener != null)
      {
         exListener.stop();
      }
      
      disconnect();
   }

   protected void disconnect() throws JMSException
   {
      if (connection != null)
      {
         connection.setExceptionListener(null);
         connection.stop();
      }

      if (dlqHandler != null) {
         dlqHandler.stop();
      }
      
      log.debug("Disconnected");
   }
   
   protected void destroyService() throws Exception
   {
      // Take down DLQ
      if (dlqHandler != null)
      {
         dlqHandler.destroy();
         dlqHandler = null;
      }

      // close the connection consumer
      if (connectionConsumer != null)
      {
         connectionConsumer.close();
         connectionConsumer = null;
      }

      //
      // jason: should really fix the default pool to not need this special hack
      //

      // clear the server session pool (if it is clearable)
      if (pool instanceof org.jboss.jms.asf.StdServerSessionPool)
      {
         ((org.jboss.jms.asf.StdServerSessionPool)pool).clear();
      }
      
      // close the connection
      if (connection != null)
      {
         connection.close();
         connection = null;
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                                Invoker                              //
   /////////////////////////////////////////////////////////////////////////

   //
   // jason: appears to only be used by MessageListenerImpl, could be protected
   //        or ML could do this itself.
   //
      
   public Object invoke(Object id,
                        Method m,
                        Object[] args,
                        Transaction tx,
                        Principal identity,
                        Object credential)
      throws Exception
   {
      Invocation invocation = new Invocation(id, m, args, tx, identity, credential);

      // Set the right context classloader
      TCLStack.push(container.getClassLoader());
      
      try
      {
         return container.invoke(invocation);
      }
      finally
      {
         TCLStack.pop();
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                            MessageListener                          //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * An implementation of MessageListener that passes messages on to the
    * container invoker.
    */
   class MessageListenerImpl
      implements MessageListener
   {
      private Logger log = Logger.getLogger(MessageListenerImpl.class);
      
      /** The container invoker. */
      JMSContainerInvoker invoker;
      
      /**
       * Construct a <tt>MessageListenerImpl</tt>.
       *
       * @param invoker  The container invoker. Must not be null.
       */
      MessageListenerImpl(final JMSContainerInvoker invoker)
      {
         // assert invoker != null;
         
         this.invoker = invoker;
      }

      /**
       * Process a message.
       *
       * @param message  The message to process.
       */
      public void onMessage(final Message message)
      {
         // assert message != null;
         
         if (log.isTraceEnabled())
         {
            log.trace("processing message: " + message);
         }
         
         // Invoke, shuld we catch any Exceptions??
         try
         {
            Object id = message.getJMSMessageID();

            // DLQHandling
            if (useDLQ &&                                      // Is Dead Letter Queue used at all
                message.getJMSRedelivered() &&                 // Was message resent
                dlqHandler.handleRedeliveredMessage(message))  // True if moved to DLQ
            {
               // Message will be placed on Dead Letter Queue,
               // if redelivered to many times
               return;
            }
            
            invoker.invoke(id,
                           ON_MESSAGE,
                           new Object[]{ message },
                           container.getTransactionManager().getTransaction(),
                           null,
                           null);
                           
         }
         catch (Exception e)
         {
            log.error("Exception in message listener", e);
         }
      }
   }


   /////////////////////////////////////////////////////////////////////////
   //                           ExceptionListener                         //
   /////////////////////////////////////////////////////////////////////////
   
   /**
    * ExceptionListener for failover handling.
    */
   class ExceptionListenerImpl
      implements ExceptionListener
   {
      private Logger log = Logger.getLogger(ExceptionListenerImpl.class);
      
      JMSContainerInvoker invoker;
      Thread currentThread;
      boolean running = true;
      
      ExceptionListenerImpl(final JMSContainerInvoker invoker)
      {
         this.invoker = invoker;
      }
      
      public void onException(JMSException ex)
      {
         log.warn("JMS provider failure detected: ", ex);

         currentThread = Thread.currentThread();
         boolean restartInvoker = true;
         
         while (restartInvoker && running)
         {
            try
            {
               log.debug("Sleeping for " + reconnectInterval + " before reconnecting");
               Thread.sleep(reconnectInterval);
               
               log.info("Trying to reconnect to JMS provider");
               
               // Reboot the invoker
               invoker.disconnect();
               invoker.destroy();
               invoker.connect();
               invoker.start();

               // If we get this far the container is rebooted
               restartInvoker = false;
               
               log.info("Reconnected to JMS provider");
            }
            catch (InterruptedException e)
            {
               log.debug("Sleep interrupted; aborting reconnect");
               return;
            }
            catch (Exception e)
            {
               log.error("Reconnect failed: JMS provider failure detected", e);
            }
         }
         
         currentThread = null;
      }

      public void stop()
      {
         log.debug("Stop requested");
         
         running = false;
         if (currentThread != null)
         {
            currentThread.interrupt();
            log.debug("Thread interrupted: " + currentThread);
         }
      }
   }
}
