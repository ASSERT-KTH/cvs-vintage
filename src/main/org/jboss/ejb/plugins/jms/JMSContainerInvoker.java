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


import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.DeploymentException;

import org.jboss.tm.TxManager;

import org.jboss.logging.Logger;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.MessageDrivenMetaData;

import org.jboss.jms.jndi.JMSProviderAdapter;
import org.jboss.jms.asf.ServerSessionPoolFactory;

import org.exolab.jms.client.JmsServerSessionPool;

import org.w3c.dom.Element;

import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * ContainerInvoker for JMS MessageDrivenBeans, based on JRMPContainerInvoker.
 *      <description>
 *   
 *      @see <related>
 *      @author Peter Antman (peter.antman@tim.se)
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *      @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *      @version $Revision: 1.11 $
 */
public class JMSContainerInvoker implements
ContainerInvoker, XmlLoadable
{
    // Constants -----------------------------------------------------
    static final String msgInterface = "javax.jms.MessageListener";
    static final String msgMethod = "onMessage";
    static final String msgArgument = "javax.jms.Message";
    static Method listenerMethod;
    static {
	// Get the method
	try {
	    Class msgInterfaceClass = Class.forName(msgInterface);
	    Class argumentClass = Class.forName(msgArgument);
	    listenerMethod = msgInterfaceClass.getMethod(msgMethod, new Class[] {argumentClass});
	    
	} catch(ClassNotFoundException ex) {
	    Logger.error("Could not the classes for message interface" + msgInterface + ": " + ex);
	}catch(NoSuchMethodException ex) {
	    Logger.error("Could not get the method for message interface" + msgMethod + ": " + ex);
	}
    };

   // Attributes ----------------------------------------------------
    protected boolean optimize = false;
    protected int maxMessagesNr = 1;
    protected int maxPoolSize = 15;
    protected String jMSProviderAdapterJNDI;
    protected String serverSessionPoolFactoryJNDI;
    protected int acknowledgeMode;

    protected Container container;
    
    protected Connection connection;
    protected ConnectionConsumer connectionConsumer;
    protected TxManager tm;
    protected ServerSessionPool pool;
    protected ExceptionListenerImpl exListener;


   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
      //DEBUG		Logger.debug("Container Invoker optimize set to '"+optimize+"'");
   }

   public boolean isOptimized()
   {
       //DEBUG  Logger.debug("Optimize in action: '"+optimize+"'");
      return optimize;
   }

   public EJBMetaData getEJBMetaData()
   {
       throw new Error("Not valid for MessageDriven beans");
   }

    // ContainerInvoker implementation
    public EJBHome getEJBHome() {throw new Error("Not valid for MessageDriven beans");}

    public EJBObject getStatelessSessionEJBObject() {throw new Error("Not valid for MessageDriven beans");}

    public EJBObject getStatefulSessionEJBObject(Object id) {throw new Error("Not valid for MessageDriven beans");}

    public EJBObject getEntityEJBObject(Object id) {throw new Error("Not valid for MessageDriven beans");}

    public Collection getEntityCollection(Collection ids) {throw new Error("Not valid for MessageDriven beans");}

    
   public Object invoke(Object id, Method m, Object[] args, Transaction tx,
    Principal identity, Object credential )
      throws Exception
   {
       
       MethodInvocation mi = new MethodInvocation(id, m, args, tx, identity, credential);
       
       // Set the right context classloader
       ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
       Thread.currentThread().setContextClassLoader(container.getClassLoader());
       
       
       try
	   {
          return container.invoke(mi);
       } finally {	   
          Thread.currentThread().setContextClassLoader(oldCl);
       }
   }

   // ContainerService implementation -------------------------------
   public void setContainer(Container con)
   {
      this.container = con;
      //jndiName = container.getBeanMetaData().getJndiName();
   }

   public void init()
      throws Exception
   {

       // Store TM reference locally - should we test for CMT Required
        tm = (TxManager) container.getTransactionManager();

       /*
	* Get configuration information - from EJB-xml
	*/
       MessageDrivenMetaData config = ((MessageDrivenMetaData)container.getBeanMetaData());
       
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
       

       /*
	* Set upp JNDI
	* connect to the JNDI server and get a reference to 
	* root context
	*/
       Context context = null;

       Context jbossContext = new InitialContext();
       JMSProviderAdapter adapter = (JMSProviderAdapter)jbossContext.lookup(jMSProviderAdapterJNDI);
       context = adapter.getInitialContext();
       
       
       // if we can't get the root context then exit with an exception
       if (context == null)
	   {
	       throw new RuntimeException("Failed to get the root context");
	   }
       
       // Set up pool
       ServerSessionPoolFactory poolFactory = (ServerSessionPoolFactory)jbossContext.lookup(serverSessionPoolFactoryJNDI);

       // jndiSuffix is merely the name that the user has given the MDB.
       // since the jndi name contains the message type I have to split at the "/"
       // if there is no slash then I use the entire jndi name.....
       String jndiSuffix = "";
       if(destinationJNDI != null){
		   int indexOfSlash = destinationJNDI.indexOf("/");
		   if(indexOfSlash != -1){
	         jndiSuffix = destinationJNDI.substring(indexOfSlash+1);
		   }else{
			 jndiSuffix = destinationJNDI;
		   }

	   // if the jndi name from jboss.xml is null then lets use the ejbName
	   }else{
	       jndiSuffix = config.getEjbName();
	   }
       MBeanServer server = (MBeanServer)MBeanServerFactory.findMBeanServer(null).iterator().next();       
       
       if (destinationType.equals("javax.jms.Topic")) 
	   {
	       Logger.debug("Got destination type Topic for " + config.getEjbName());
	       
	       // All classes are different between topics and queues!!
	       TopicConnectionFactory topicFactory = 
		   (TopicConnectionFactory)context.
		   lookup(adapter.getTopicFactoryName());
	       // Do we have a user - this is messy code (should be done for queues to)
	       TopicConnection topicConnection;
	       if(user != null) 
		   {
		       Logger.debug("Creating topic connection with user: " + 
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
	       try{
	          topic = (Topic)context.lookup(topicJndi);
		   }catch(NamingException ne){
			  Logger.log("JndiName not found:"+topicJndi + "...attempting to recover");
			  server.invoke(new ObjectName("JMS","service","JMSServer"), "newTopic", new Object[]{jndiSuffix}, new String[] {"java.lang.String"});
			  topic = (Topic)context.lookup(topicJndi);
		   }
	       
	       pool = poolFactory.
		   getServerSessionPool(
					topicConnection,
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
		       createConnectionConsumer(
						topic, 
						messageSelector, 
						pool, 
						maxMessagesNr); 
	       } 
	   else 
	       {
		   //Durable subscription
		   String clientId = config.getClientId();
		   String durableName = clientId != null ? 
		       clientId:
		   config.getEjbName();
		   connectionConsumer = topicConnection.
		       createDurableConnectionConsumer(
						       topic, 
						       durableName,
						       messageSelector, 
						       pool, 
						       maxMessagesNr);
	   }
	   // set global connection, so we have something to start() and close()
	   connection = topicConnection;
	   Logger.debug("Topic connectionConsumer set up");

       }
       else if(destinationType.equals("javax.jms.Queue")) 
	   {
	       Logger.debug("Got destination type Queue");
	       QueueConnectionFactory queueFactory = 
		   (QueueConnectionFactory)context.lookup(adapter.getQueueFactoryName());
	       
	       // Do we have a user
	       QueueConnection queueConnection;
	       if (user != null) 
		   {
		   queueConnection = queueFactory.
		       createQueueConnection(
					     user,
					     config.getPasswd());
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
	      try
                {
	        queue = (Queue)context.lookup(queueJndi);
		}
                catch(NamingException ne){
		  Logger.log("JndiName not found:"+queueJndi + "...attempting to recover");
		  server.invoke(new ObjectName("JMS:service=JMSServer"), "newQueue", new Object[]{jndiSuffix}, new String[] {"java.lang.String"});
		  queue = (Queue)context.lookup(queueJndi);
		}
	       
	      pool = poolFactory.
		   getServerSessionPool(
					queueConnection,
					maxPoolSize, 
					//Transacted
					true, 
					acknowledgeMode, 
					new MessageListenerImpl(this));


	       connectionConsumer = queueConnection.
		   createConnectionConsumer(
					    queue, 
					    messageSelector, 
					    pool, 
					    maxMessagesNr); 

	   // set global connection, so we have something to start() and close()
	   connection = queueConnection;
	   Logger.debug("Queue connectionConsumer set up");
	   }
   }
    
    // Start the connection
    public void start()
	throws Exception
    {
	Logger.debug("Starting JMSContainerInvoker");
	exListener = new ExceptionListenerImpl(this);
	connection.setExceptionListener(exListener);
	connection.start();
    }
    
    // Stop the connection
    public void stop()
    {
	Logger.debug("Stopping JMSContainerInvoker");
	// Silence the exception listener
	if (exListener != null)
	    exListener.stop();
	innerStop();
	
    }

    // Stop done from inside, we should not stop the exceptionListener in
    // inner stop
    protected void innerStop() {
	try {
	    if (connection != null)
		connection.setExceptionListener(null);
	    
	}catch(Exception se) {
	    Logger.log("Could not set JMSContainerInvoker ExceptionListener to null :" + se);
	}	
	
	// Stop the connection
	try {
	    if(connection != null) 
		connection.stop();
	}catch(Exception cs) {
	    Logger.log("Could not stop JMS connection:" + cs);
	}
    }
    
    
    // Take down all fixtures
    public void destroy()
    {
	Logger.debug("Destroying JMSContainerInvoker");
	try {
	    if (pool instanceof org.jboss.jms.asf.StdServerSessionPool) {
		org.jboss.jms.asf.StdServerSessionPool p =
		    (org.jboss.jms.asf.StdServerSessionPool)pool;
		p.clear();
	    }
	}catch(Exception pe) {
	    Logger.log("Could not clear ServerSessionPool:" + pe);
	}
	
	
	try {
	    if (connectionConsumer != null)
		connectionConsumer.close();
	}catch(Exception ex) {
	    Logger.log("Could not close JMSContainerInvoker consumer:" + ex);
	}
	try {
	    if (connection != null)
		connection.close();
	}catch(Exception ex) {
	    Logger.log("Could not close JMSContainerInvoker connection:" + ex);
	}
    }
    
    // XmlLoadable implementation
    public void importXml(Element element) throws DeploymentException 
    {
	
	try {
	    String maxMessages = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaxMessages"));
	    maxMessagesNr = Integer.parseInt(maxMessages);
	    String maxSize = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
	    maxPoolSize = Integer.parseInt(maxSize);
	} catch(NumberFormatException e) {
	    //Noop will take default value
	} catch(DeploymentException e) {
	    //Noop will take default value
	}
	
	// If these are not found we will get a DeploymentException, I hope
	jMSProviderAdapterJNDI = MetaData.getElementContent(MetaData.getUniqueChild(element, "JMSProviderAdapterJNDI"));
	serverSessionPoolFactoryJNDI = MetaData.getElementContent(MetaData.getUniqueChild(element, "ServerSessionPoolFactoryJNDI"));
	
	// Check java:/ prefix
	if (!jMSProviderAdapterJNDI.startsWith("java:/"))
	    jMSProviderAdapterJNDI = "java:/"+jMSProviderAdapterJNDI;
	if (!serverSessionPoolFactoryJNDI.startsWith("java:/"))
	    serverSessionPoolFactoryJNDI = "java:/"+serverSessionPoolFactoryJNDI;
    }
    
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    class MessageListenerImpl implements MessageListener {
	
	
	JMSContainerInvoker invoker = null;
	
	
	MessageListenerImpl(JMSContainerInvoker invoker) {
	    this.invoker = invoker;
	}
	public void onMessage(Message message)
	{
	    /*
	      Logger.debug(
	      "[" + Thread.currentThread().hashCode() +
	      "] Processing message " + message);
	    */
	    Object id;
	    try {
		id = message.getJMSMessageID();
	    }catch(javax.jms.JMSException ex) {
		id = "JMSContainerInvoke";
	    }
	    // Invoke, shuld we catch any Exceptions??
	    try {
		invoker.invoke(
			       // Object id - where used?
			       id,
			       // Method to invoke
			       listenerMethod,
			       //argument
			       new Object[] {message},
			       //Transaction 
			       tm.getTransaction(),
			       //Principal
			       null,
			       //Cred
			       null);
	    }catch(Exception ex) {
		Logger.log("Exception in JMSCI message listener: : " + ex);
		ex.printStackTrace();
	    }
	    
	}
	
    }

    /**
     * ExceptionListener for failover handling.
     */
    class ExceptionListenerImpl implements ExceptionListener {
	JMSContainerInvoker invoker = null;
	Thread currentThread = null;
	boolean notStoped = true;
	
	ExceptionListenerImpl(JMSContainerInvoker invoker) {
	    this.invoker = invoker;
	}

	void stop() {
	    notStoped = false;
	    if ( currentThread  != null) currentThread.interrupt();
	}

	public void onException(JMSException ex) {
	    currentThread = Thread.currentThread();
	    try {
		Logger.warning("MDB lost connection to provider");
		boolean tryIt = true;
		while(tryIt && notStoped) {
		    Logger.log("MDB Trying to reconnect...");
		    try {
			try {
			    Thread.sleep(10000);
			}catch(InterruptedException ie) { tryIt=false; return;}
			//try {
			// Reboot container
			invoker.innerStop();
			invoker.destroy();
			invoker.init();
			invoker.start();
			tryIt = false;
			Logger.log("OK - reconnected");
			//return;
		    }catch(Exception e) {
			Logger.log("MDB error reconnecting: " +e);
		    }
		}
		
		
		//topicConnection.close();
	    }catch(Exception je) {
		Logger.warning("Could not restart connection " + je);
		je.printStackTrace();
	    } finally {
		currentThread = null;
	    }
	}
	
    }
}
