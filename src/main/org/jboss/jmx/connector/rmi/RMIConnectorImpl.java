/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.rmi;

import java.io.ObjectInputStream;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.MBeanServer;
import javax.management.MBeanInfo;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.OperationsException;
import javax.management.ReflectionException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.jboss.system.ServiceMBeanSupport;

import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.jboss.jmx.connector.RemoteMBeanServer;
// import org.jboss.jmx.connector.RemoteMBeanServerMBean;
import org.jboss.jmx.connector.notification.ClientNotificationListener;
import org.jboss.jmx.connector.notification.JMSClientNotificationListener;
import org.jboss.jmx.connector.notification.PollingClientNotificationListener;
import org.jboss.jmx.connector.notification.RMIClientNotificationListener;
import org.jboss.jmx.connector.notification.SearchClientNotificationListener;

/**
* Implementation of the JMX Connector over the RMI protocol 
*      
* @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
* @author <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
*/
public class RMIConnectorImpl
   implements /* RemoteMBeanServerMBean, */ RMIConnectorImplMBean
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private RMIAdaptor        mRemoteAdaptor;
   private Object            mServer = "";
   private Vector            mListeners = new Vector();
   private int               mEventType = NOTIFICATION_TYPE_RMI;
   private String[]          mOptions = new String[ 0 ];
   private Random            mRandom = new Random();
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * AS For evaluation purposes
   * Creates a Connector based on an already found Adaptor
   *
   * @param pAdaptor RMI-Adaptor used to connect to the remote JMX Agent
   **/
   public RMIConnectorImpl(
      RMIAdaptor pAdaptor
   ) {
      mRemoteAdaptor = pAdaptor;
      mServer = "Dummy";
   }
   
   public RMIConnectorImpl(
      int pNotificationType,
      String[] pOptions,
      String pServerName
   ) {
      super();
      mEventType = pNotificationType;
      if( pOptions == null ) {
         mOptions = new String[ 0 ];
      } else {
         mOptions = pOptions;
      }
      start( pServerName );
   }
   
   // JMXClientConnector implementation -------------------------------
   public void start(
      Object pServer
   ) throws IllegalArgumentException {
      if( pServer == null ) {
         throw new IllegalArgumentException( "Server cannot be null. "
            + "To close the connection use stop()" );
      }
      try {
         InitialContext lNamingContext = new InitialContext();
         System.out.println( "RMIClientConnectorImp.start(), got Naming Context: " +   lNamingContext +
            ", environment: " + lNamingContext.getEnvironment() +
            ", name in namespace: " + lNamingContext.getNameInNamespace()
         );
         // This has to be adjusted later on to reflect the given parameter
         mRemoteAdaptor = (RMIAdaptor) new InitialContext().lookup( "jmx:" + pServer + ":rmi" );
         System.err.println( "RMIClientConnectorImpl.start(), got remote connector: " + mRemoteAdaptor );
         mServer = pServer;
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
   }

   public void stop() {
      System.out.println( "RMIClientConnectorImpl.stop(), start" );
      // First go through all the reistered listeners and remove them
      if( mRemoteAdaptor != null ) {
         // Loop through all the listeners and remove them
         Iterator i = mListeners.iterator();
         while( i.hasNext() ) {
            ClientNotificationListener lListener = (ClientNotificationListener) i.next();
            try {
               lListener.removeNotificationListener( this );
            }
            catch( Exception e ) {
            }
            i.remove();
         }
      }
      mRemoteAdaptor = null;
      mServer = "";
   }
   
   public boolean isAlive() {
      return mRemoteAdaptor != null;
   }

   public String getServerDescription() {
      return "" + mServer;
   }

   /**
   * Creates a Queue Connection
   *
   * @return Returns a QueueConnection if found otherwise null
   **/
   private QueueConnection getQueueConnection( String pJNDIName )
      throws
         NamingException,
         JMSException
   {
      Context lJNDIContext = null;
      if( mOptions.length > 0 && mOptions[ 0 ] != null ) {
         Hashtable lProperties = new Hashtable();
//         lProperties.put( Context.PROVIDER_URL, mOptions[ 0 ] );
         lProperties.put( Context.PROVIDER_URL, (String) mServer );
         lJNDIContext = new InitialContext( lProperties );
      } else {
         lJNDIContext = new InitialContext();
      }
      System.out.println( "JNDI Environment: " + lJNDIContext.getEnvironment() );
      System.out.println( "Lookup Queue Connection Factory: " + pJNDIName );
      Object aRef = lJNDIContext.lookup( pJNDIName );
      System.out.println( "Narrow Queue Connection Factory" );
      QueueConnectionFactory aFactory = (QueueConnectionFactory) 
         PortableRemoteObject.narrow( aRef, QueueConnectionFactory.class );
      System.out.println( "Narrow Queue Connection" );
      QueueConnection lConnection = aFactory.createQueueConnection();
      lConnection.start();
      return lConnection;
   }
   
   // RemoteMBeanServer implementation -------------------------------------

   public ObjectInstance createMBean(
      String pClassName,
      ObjectName pName
   ) throws
      ReflectionException,
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      MBeanException,
      NotCompliantMBeanException
   {
      try {
         return mRemoteAdaptor.createMBean( pClassName, pName );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public ObjectInstance createMBean(
      String pClassName,
      ObjectName pName,
      ObjectName pLoaderName
   ) throws
      ReflectionException,
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      MBeanException,
      NotCompliantMBeanException,
      InstanceNotFoundException
   {
      try {
         return mRemoteAdaptor.createMBean( pClassName, pName, pLoaderName );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public ObjectInstance createMBean(
      String pClassName,
      ObjectName pName,
      Object[] pParams,
      String[] pSignature
   ) throws
      ReflectionException,
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      MBeanException,
      NotCompliantMBeanException
   {
      try {
         return mRemoteAdaptor.createMBean( pClassName, pName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public ObjectInstance createMBean(
      String pClassName,
      ObjectName pName,
      ObjectName pLoaderName,
      Object[] pParams,
      String[] pSignature
   ) throws
      ReflectionException,
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      MBeanException,
      NotCompliantMBeanException,
      InstanceNotFoundException
   {
      try {
         return mRemoteAdaptor.createMBean( pClassName, pName, pLoaderName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public void unregisterMBean(
      ObjectName pName
   ) throws
      InstanceNotFoundException,
      MBeanRegistrationException
   {
      try {
         mRemoteAdaptor.unregisterMBean( pName );
      }
      catch( RemoteException re ) {
      }
   }

   public ObjectInstance getObjectInstance(
      ObjectName pName
   ) throws
      InstanceNotFoundException
   {
      try {
         return mRemoteAdaptor.getObjectInstance( pName );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public Set queryMBeans(
      ObjectName pName,
      QueryExp pQuery
   ) {
      try {
         return mRemoteAdaptor.queryMBeans( pName, pQuery );
      }
      catch( RemoteException re ) {
         re.printStackTrace();
         //AS Not a good style but for now
         return null;
      }
   }

   public Set queryNames(
      ObjectName pName,
      QueryExp pQuery
   ) {
      try {
         return mRemoteAdaptor.queryNames( pName, pQuery );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public boolean isRegistered(
      ObjectName pName
   ) {
      try {
         return mRemoteAdaptor.isRegistered( pName );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return false;
      }
   }

   public boolean isInstanceOf(
      ObjectName pName,
      String pClassName
   ) throws
      InstanceNotFoundException
   {
      try {
         return mRemoteAdaptor.isInstanceOf( pName, pClassName );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return false;
      }
   }

   public Integer getMBeanCount(
   ) {
      try {
         return mRemoteAdaptor.getMBeanCount();
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public Object getAttribute(
      ObjectName pName,
      String pAttribute
   ) throws
      MBeanException,
      AttributeNotFoundException,
      InstanceNotFoundException,
      ReflectionException
   {
      try {
         return mRemoteAdaptor.getAttribute( pName, pAttribute );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public AttributeList getAttributes(
      ObjectName pName,
      String[] pAttributes
   ) throws
      InstanceNotFoundException,
      ReflectionException
   {
      try {
         return mRemoteAdaptor.getAttributes( pName, pAttributes );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public void setAttribute(
      ObjectName pName,
      Attribute pAttribute
   ) throws
      InstanceNotFoundException,
      AttributeNotFoundException,
      InvalidAttributeValueException,
      MBeanException,
      ReflectionException
   {
      try {
         mRemoteAdaptor.setAttribute( pName, pAttribute );
      }
      catch( RemoteException re ) {
      }
   }

   public AttributeList setAttributes(
      ObjectName pName,
      AttributeList pAttributes
   ) throws
      InstanceNotFoundException,
      ReflectionException
   {
      try {
         return mRemoteAdaptor.setAttributes( pName, pAttributes );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   public Object invoke(
      ObjectName pName,
      String pActionName,
      Object[] pParams,
      String[] pSignature
   ) throws
      InstanceNotFoundException,
      MBeanException,
      ReflectionException
   {
      try {
         return mRemoteAdaptor.invoke( pName, pActionName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         re.printStackTrace();
         return null;
      }
   }

   public String getDefaultDomain(
   ) {
      try {
         return mRemoteAdaptor.getDefaultDomain();
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   /**
   * Add a notification listener which was previously loaded
   * as MBean. ATTENTION: note that the both the Notification-
   * Filter and Handback must be serializable and the class
   * definition must be available for the RMI-Connector
   **/
   public void addNotificationListener(
      ObjectName pName,
      ObjectName pListener,
      NotificationFilter pFilter,
      Object pHandback
   ) throws
      InstanceNotFoundException
   {
      try {
         mRemoteAdaptor.addNotificationListener( pName, pListener, pFilter, pHandback );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public void addNotificationListener(
      ObjectName pName,
      NotificationListener pListener,
      NotificationFilter pFilter,
      Object pHandback
   ) throws
      InstanceNotFoundException
   {
      try {
         ClientNotificationListener lListener = null;
         switch( mEventType ) {
            case NOTIFICATION_TYPE_RMI:
               lListener = new RMIClientNotificationListener(
                  pName,
                  pListener,
                  pHandback,
                  pFilter,
                  this
               );
               break;
            case NOTIFICATION_TYPE_JMS:
               lListener = new JMSClientNotificationListener(
                  pName,
                  pListener,
                  pHandback,
                  pFilter,
                  mOptions[ 0 ],
                  (String) mServer,
                  this
               );
               break;
            case NOTIFICATION_TYPE_POLLING:
               lListener = new PollingClientNotificationListener(
                  pName,
                  pListener,
                  pHandback,
                  pFilter,
                  5000, // Sleeping Period
                  2500, // Maximum Pooled List Size
                  this
               );
         }
         // Add this listener on the client to remove it when the client goes down
         mListeners.addElement( lListener );
      }
      catch( Exception e ) {
         if( e instanceof RuntimeException ) {
            throw (RuntimeException) e;
         }
         if( e instanceof InstanceNotFoundException ) {
            throw (InstanceNotFoundException) e;
         }
         e.printStackTrace();
         throw new RuntimeException( "Remote access to perform this operation failed: " + e.getMessage() );
      }
   }

   public void removeNotificationListener(
      ObjectName pName,
      NotificationListener pListener
   ) throws
      InstanceNotFoundException,
      ListenerNotFoundException
   {
      ClientNotificationListener lCheck = new SearchClientNotificationListener( pName, pListener );
      int i = mListeners.indexOf( lCheck );
      if( i >= 0 ) {
         ClientNotificationListener lListener = (ClientNotificationListener) mListeners.get( i );
         lListener.removeNotificationListener( this );
      }
   }

   public void removeNotificationListener(
      ObjectName pName,
      ObjectName pListener
   ) throws
      InstanceNotFoundException,
      ListenerNotFoundException
   {
      try {
         mRemoteAdaptor.removeNotificationListener( pName, pListener );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public MBeanInfo getMBeanInfo(
      ObjectName pName
   ) throws
      InstanceNotFoundException,
      IntrospectionException,
      ReflectionException
   {
      try {
         return mRemoteAdaptor.getMBeanInfo( pName );
      }
      catch( RemoteException re ) {
         //AS Not a good style but for now
         return null;
      }
   }

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

}   

