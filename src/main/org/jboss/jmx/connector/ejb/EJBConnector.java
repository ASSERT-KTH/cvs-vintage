/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.ejb;

import org.jboss.jmx.adaptor.ejb.ServiceUnavailableException;
import org.jboss.jmx.adaptor.interfaces.Adaptor;
import org.jboss.jmx.adaptor.interfaces.AdaptorHome;

import java.io.ObjectInputStream;
import java.io.Serializable;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

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

import org.jboss.jmx.ObjectHandler;
import org.jboss.jmx.connector.notification.RMINotificationSender;
import org.jboss.jmx.connector.notification.JMSNotificationListener;

/**
* This is the equivalent to the RMI Connector but uses the
* EJB Adaptor. The only advantage of using EJB-Adaptor is
* that you can utilize the security.
*
* ATTENTION: Note that for the event transport (or in the
* JMX Notations: Notification) the server must be able to
* load the RMI Stubs and the Remote Listener classes.
* Therefore you must make them available to the JMX Server
* and the EJB-Adaptor (meaning the EJB-Container).
*
* @author Andreas Schaefer (andreas.schaefer@madplanet.com)
* @version $Revision: 1.1 $
**/
public class EJBConnector
   implements MBeanServer
{

   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
   
   /**
   * If this type is used and you specify a valid QueueConnectorFactory
   * then this connector will use JMS to transfer the events asynchronous
   * back from the server to the client.
   **/
   public static final int EVENT_TYPE_JMS = 0;
   /**
   * If this type is used the Connector will use RMI Callback Objects to
   * transfer the events back from the server synchronously.
   **/
   public static final int EVENT_TYPE_RMI = 1;
   
   // -------------------------------------------------------------------------
   // Members 
   // -------------------------------------------------------------------------  

   private Adaptor mAdaptor;

   private String            mJNDIServer;
   private Hashtable         mHandbackPool = new Hashtable();
   private Vector            mListeners = new Vector();
   private int               mEventType = EVENT_TYPE_RMI;
   private String[]          mOptions = new String[ 0 ];

   // -------------------------------------------------------------------------
   // Constructor
   // -------------------------------------------------------------------------
    
   /**
   * Creates an Client Connector using the EJB-Adaptor to
   * access the remote JMX Server.
   *
   * @param pType Defines the type of event transport. Please have a
   *              look at the constants with the prefix EVENT_TYPE
   *              which protocols are supported
   * @param pOptions List of options used for the event transport. Right
   *                 now only for event type JMS there is the JMS Queue-
   *                 Factory JNDI Name supported.
   **/
   public EJBConnector( int pType, String[] pOptions ) {
      this( pType, pOptions, null, null );
   }
   
   /**
   * Creates an Client Connector using the EJB-Adaptor to
   * access the remote JMX Server.
   *
   * @param pType Defines the type of event transport. Please have a
   *              look at the constants with the prefix EVENT_TYPE
   *              which protocols are supported
   * @param pOptions List of options used for the event transport. Right
   *                 now only for event type JMS there is the JMS Queue-
   *                 Factory JNDI Name supported.
   * @param pJNDIName JNDI Name of the EJB-Adaptor to lookup its Home interface
   *                  and if null then "ejb/jmx/ejb/Adaptor" is used as default
   **/
   public EJBConnector( int pType, String[] pOptions, String pJNDIName ) {
      this( pType, pOptions, pJNDIName, null );
   }
   
   /**
   * Creates an Client Connector using the EJB-Adaptor to
   * access the remote JMX Server.
   *
   * @param pType Defines the type of event transport. Please have a
   *              look at the constants with the prefix EVENT_TYPE
   *              which protocols are supported
   * @param pOptions List of options used for the event transport. Right
   *                 now only for event type JMS there is the JMS Queue-
   *                 Factory JNDI Name supported.
   * @param pJNDIName JNDI Name of the EJB-Adaptor to lookup its Home interface
   *                  and if null then "ejb/jmx/ejb/Adaptor" is used as default
   * @param pJNDIServer Server name of the JNDI server to look up the EJB-Adaptor
   *                    and QueueFactory if JMS is used for the event transport.
   *                    If null then the default specified in the "jndi.properties"
   *                    will be used.
   **/
   public EJBConnector( int pType, String[] pOptions, String pJNDIName, String pJNDIServer ) {
      try {
         if( pType == EVENT_TYPE_RMI || pType == EVENT_TYPE_JMS ) {
            mEventType = pType;
         }
         if( pOptions != null ) {
            mOptions = pOptions;
         }
         if( pJNDIName == null || pJNDIName.trim().length() == 0 ) {
            pJNDIName = "ejb/jmx/ejb/Adaptor";
         }
         if( pJNDIServer.trim().length() == 0 ) {
            mJNDIServer = null;
         } else {
            mJNDIServer = pJNDIServer;
         }
         mAdaptor = getAdaptorBean( pJNDIName );
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
   }
   
   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  
   
   /**
   * Describes the instance and its content for debugging purpose
   *
   * @return Debugging information about the instance and its content
   **/
   public String toString() {
      return "EJBAdaptorClient [ " + " ]";
   }

   /**
   * Checks if the given instance is the same (same address) as
   * this instance.
   *
   * @return The result from the super class equals() method
   **/
   public boolean equals( Object pTest ) {
      return super.equals( pTest );
   }

   /**
   * Returns the hashcode of this instance
   *
   * @return Hashcode of its super class
   **/
   public int hashCode() {
      return super.hashCode();
   }

   /**
   * Creates a SurveyManagement bean.
   *
   * @return Returns a SurveyManagement bean for use by the Survey handler.
   **/
   private Adaptor getAdaptorBean( String pJNDIName )
      throws ServiceUnavailableException
   {
      try {
         Context lJNDIContext = null;
         if( mJNDIServer != null ) {
            Hashtable lProperties = new Hashtable();
            lProperties.put( Context.PROVIDER_URL, mJNDIServer );
            lJNDIContext = new InitialContext( lProperties );
         } else {
            lJNDIContext = new InitialContext();
         }
         System.out.println( "JNDI Context properties: " + lJNDIContext.getEnvironment() );
         Object aEJBRef = lJNDIContext.lookup( pJNDIName );
         AdaptorHome aHome = (AdaptorHome) 
            PortableRemoteObject.narrow( aEJBRef, AdaptorHome.class );

         return aHome.create();
      }
      catch( NamingException pNE ) {
         pNE.printStackTrace();
         throw new ServiceUnavailableException( 
            "JNDI lookup failed: " + pNE.getMessage() 
         );
      }
      catch( RemoteException pRE ) {
         throw new ServiceUnavailableException(
            "Remote communication error: " + pRE.getMessage()
         );
      }
      catch( CreateException pCE ) {
         throw new ServiceUnavailableException(
            "Problem creating content management session bean: " + pCE.getMessage()
         );
      }
   }
   
   /**
   * Creates a SurveyManagement bean.
   *
   * @return Returns a SurveyManagement bean for use by the Survey handler.
   **/
   private QueueConnection getQueueConnection( String pJNDIName )
      throws ServiceUnavailableException
   {
      try {
         Context lJNDIContext = null;
         if( mJNDIServer != null ) {
            Hashtable lProperties = new Hashtable();
            lProperties.put( Context.PROVIDER_URL, mJNDIServer );
            lJNDIContext = new InitialContext( lProperties );
         } else {
            lJNDIContext = new InitialContext();
         }
         Object aRef = lJNDIContext.lookup( pJNDIName );
         QueueConnectionFactory aFactory = (QueueConnectionFactory) 
            PortableRemoteObject.narrow( aRef, QueueConnectionFactory.class );
         QueueConnection lConnection = aFactory.createQueueConnection();
         lConnection.start();
         return lConnection;
      }
      catch( NamingException pNE ) {
         pNE.printStackTrace();
         throw new ServiceUnavailableException( 
            "JNDI lookup failed: " + pNE.getMessage() 
         );
      }
      catch( JMSException je ) {
         throw new ServiceUnavailableException(
            "Remote communication error: " + je.getMessage()
         );
      }
   }
   
   // -------------------------------------------------------------------------
   // MBeanServer Implementations
   // -------------------------------------------------------------------------  
   
   public Object instantiate(
      String pClassName
   ) throws
      ReflectionException,
      MBeanException
   {
      try {
         return mAdaptor.instantiate( pClassName );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }
   
   public Object instantiate(
      String pClassName,
      ObjectName pLoaderName
   ) throws
      ReflectionException,
      MBeanException,
      InstanceNotFoundException
   {
      try {
         return mAdaptor.instantiate( pClassName, pLoaderName );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public Object instantiate(
      String pClassName,
      Object[] pParams,
      String[] pSignature
   ) throws
      ReflectionException,
      MBeanException
   {
      try {
         return mAdaptor.instantiate( pClassName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public Object instantiate(
      String pClassName,
      ObjectName pLoaderName,
      Object[] pParams,
      String[] pSignature
   ) throws
      ReflectionException,
      MBeanException,
      InstanceNotFoundException
   {
      try {
         return mAdaptor.instantiate( pClassName, pLoaderName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

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
         return mAdaptor.createMBean( pClassName, pName );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
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
         return mAdaptor.createMBean( pClassName, pName, pLoaderName );
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
         return mAdaptor.createMBean( pClassName, pName, pParams, pSignature );
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
         return mAdaptor.createMBean( pClassName, pName, pLoaderName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public ObjectInstance registerMBean(
      Object pObject,
      ObjectName pName
   ) throws
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      NotCompliantMBeanException
   {
      try {
         return mAdaptor.registerMBean( (ObjectHandler) pObject, pName );
      }
      catch( RemoteException re ) {
         throw new MBeanRegistrationException( re );
      }
   }

   public void unregisterMBean(
      ObjectName pName
   ) throws
      InstanceNotFoundException,
      MBeanRegistrationException
   {
      try {
         mAdaptor.unregisterMBean( pName );
      }
      catch( RemoteException re ) {
         throw new MBeanRegistrationException( re );
      }
   }

   public ObjectInstance getObjectInstance(
      ObjectName pName
   ) throws
      InstanceNotFoundException
   {
      try {
         return mAdaptor.getObjectInstance( pName );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public Set queryMBeans(
      ObjectName pName,
      QueryExp pQuery
   ) {
      try {
         return mAdaptor.queryMBeans( pName, pQuery );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public Set queryNames(
      ObjectName pName,
      QueryExp pQuery
   ) {
      try {
         return mAdaptor.queryNames( pName, pQuery );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public boolean isRegistered(
      ObjectName pName
   ) {
      try {
         return mAdaptor.isRegistered( pName );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public boolean isInstanceOf(
      ObjectName pName,
      String pClassName
   ) throws
      InstanceNotFoundException
   {
      try {
         return mAdaptor.isInstanceOf( pName, pClassName );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public Integer getMBeanCount(
   ) {
      try {
         return mAdaptor.getMBeanCount();
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
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
         return mAdaptor.getAttribute( pName, pAttribute );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
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
         return mAdaptor.getAttributes( pName, pAttributes );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
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
         mAdaptor.setAttribute( pName, pAttribute );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
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
         return mAdaptor.setAttributes( pName, pAttributes );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
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
         return mAdaptor.invoke( pName, pActionName, pParams, pSignature );
      }
      catch( RemoteException re ) {
         throw new MBeanException( re );
      }
   }

   public String getDefaultDomain() {
      try {
         return mAdaptor.getDefaultDomain();
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
      switch( mEventType ) {
         case EVENT_TYPE_RMI:
            // Because the it is not possible to create a remote
            // NotificationListener directly a MBean must be loaded
            // and then added as new listener
            try {
               Listener lRemoteListener = new Listener(
                  pListener,
                  pHandback,
                  pName
               );
               UnicastRemoteObject.exportObject( lRemoteListener );
               ObjectName lName = createMBean(
                  "org.jboss.jmx.connector.notification.RMINotificationListener",
                  new ObjectName( "EJBAdaptor:id=" + lRemoteListener.getId() ),
                  new Object[] { lRemoteListener },
                  new String[] { RMINotificationSender.class.getName() }
               ).getObjectName();
               mAdaptor.addNotificationListener( pName, lName, pFilter, lRemoteListener.getHandback() );
               mListeners.addElement( lRemoteListener );
            }
            catch( Exception e ) {
               if( e instanceof RuntimeException ) {
                  throw (RuntimeException) e;
               }
               if( e instanceof InstanceNotFoundException ) {
                  throw (InstanceNotFoundException) e;
               }
               throw new RuntimeException( "Remote access to perform this operation failed: " + e.getMessage() );
            }
            break;
         case EVENT_TYPE_JMS:
            try {
               // Get the JMX QueueConnectionFactory from the J2EE server
               QueueConnection lConnection = getQueueConnection( mOptions[ 0 ] );
               QueueSession lSession = lConnection.createQueueSession( false, Session.AUTO_ACKNOWLEDGE );
               Queue lQueue = lSession.createTemporaryQueue();
               JMSNotificationListener lRemoteListener = new JMSNotificationListener( mOptions[ 0 ], lQueue );
               mAdaptor.addNotificationListener( pName, lRemoteListener, pFilter, null );
               QueueReceiver lReceiver = lSession.createReceiver( lQueue, null );
               lReceiver.setMessageListener( new LocalJMSListener( pListener, pHandback ) );
               mListeners.addElement( new JMSListenerSet( pName, pListener, lRemoteListener ) );
            }
            catch( Exception e ) {
               if( e instanceof RuntimeException ) {
                  throw (RuntimeException) e;
               }
               if( e instanceof InstanceNotFoundException ) {
                  throw (InstanceNotFoundException) e;
               }
               throw new RuntimeException( "Remote access to perform this operation failed: " + e.getMessage() );
            }
      }
   }

   /**
   * Add a notification listener which was previously loaded
   * as MBean. ATTENTION: note that the both the Notification-
   * Filter and Handback must be serializable and the class
   * definition must be available for the EJB-Adaptor and
   * JMX Server.
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
         mAdaptor.addNotificationListener( pName, pListener, pFilter, pHandback );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public void removeNotificationListener(
      ObjectName pName,
      NotificationListener pListener
   ) throws
      InstanceNotFoundException,
      ListenerNotFoundException
   {
      Iterator i = mListeners.iterator();
      switch( mEventType ) {
         case EVENT_TYPE_RMI:
            Listener lCheck = new Listener( pListener, null, pName );
            // Lookup if the given listener is registered
            while( i.hasNext() ) {
               Listener lListener = (Listener) i.next();
               if( lCheck.equals( lListener ) ) {
                  // If found then get the remote listener and remove it from the
                  // the Connector
                  try {
                     ObjectName lName = new ObjectName( "EJBAdaptor:id=" + lListener.getId() );
                     mAdaptor.removeNotificationListener(
                        pName,
                        lName
                     );
                     mAdaptor.unregisterMBean( lName );
                  }
                  catch( Exception e ) {
                     if( e instanceof InstanceNotFoundException ) {
                        throw (InstanceNotFoundException) e;
                     }
                     if( e instanceof ListenerNotFoundException ) {
                        throw (ListenerNotFoundException) e;
                     }
                     throw new RuntimeException( "Remote access to perform this operation failed: " + e.getMessage() );
                  }
                  finally {
                     i.remove();
                  }
               }
            }
            break;
         case EVENT_TYPE_JMS:
            JMSListenerSet lSet = new JMSListenerSet( pName, pListener, null );
            // Lookup if the given listener is registered
            while( i.hasNext() ) {
               JMSListenerSet lJMSListener = (JMSListenerSet) i.next();
               if( lSet.equals( lJMSListener ) ) {
                  // If found then get the remote listener and remove it from the
                  // the Connector
                  try {
                     mAdaptor.removeNotificationListener(
                        pName,
                        (NotificationListener) lJMSListener.getRemoteListener()
                     );
                  }
                  catch( Exception e ) {
                     if( e instanceof InstanceNotFoundException ) {
                        throw (InstanceNotFoundException) e;
                     }
                     if( e instanceof ListenerNotFoundException ) {
                        throw (ListenerNotFoundException) e;
                     }
                     throw new RuntimeException( "Remote access to perform this operation failed: " + e.getMessage() );
                  }
                  finally {
                     i.remove();
                  }
               }
            }
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
         mAdaptor.removeNotificationListener( pName, pListener );
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
         return mAdaptor.getMBeanInfo( pName );
      }
      catch( RemoteException re ) {
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
      }
   }

   public ObjectInputStream deserialize(
      ObjectName pName,
      byte[] pData
   ) throws
      InstanceNotFoundException,
      OperationsException,
      UnsupportedOperationException
   {
      throw new UnsupportedOperationException(
         "Remotely this method cannot be supported"
      );
   }

   public ObjectInputStream deserialize(
      String pClassName,
      byte[] pData
   ) throws
      OperationsException,
      ReflectionException,
      UnsupportedOperationException
   {
      throw new UnsupportedOperationException(
         "Remotely this method cannot be supported"
      );
   }

   public ObjectInputStream deserialize(
      String pClassName,
      ObjectName pLoaderName,
      byte[] pData
   ) throws
      InstanceNotFoundException,
      OperationsException,
      ReflectionException,
      UnsupportedOperationException
   {
      throw new UnsupportedOperationException(
         "Remotely this method cannot be supported"
      );
   }

   /**
   * Listener wrapper around the remote RMI Notification Listener
   **/
   public class Listener implements RMINotificationSender {

      private NotificationListener         mLocalListener;
      private ObjectHandler               mHandbackHandler;
      private Object                     mHandback;
      //AS This is necessary becasue to remove all of the registered
      //AS listeners when the connection is going down.
      //AS But maybe this is the wrong place !!
      private ObjectName                  mObjectName;
      
      public Listener(
         NotificationListener pLocalListener,
         Object pHandback,
         ObjectName pName
      ) {
         mLocalListener = pLocalListener;
         mHandback = pHandback;
         mHandbackHandler = new ObjectHandler( this.toString() );
         mObjectName = pName;
      }

      /**
      * Handles the given notification by sending this to the remote
      * client listener
      *
      * @param pNotification            Notification to be send
      * @param pHandback               Handback object
      **/
      public void handleNotification(
         Notification pNotification,
         Object pHandback
      ) throws
         RemoteException
      {
         Object lHandback;
         // Take the given handback object (which should be the same object
         // as the stored one. If yes then replace it by the stored object
         if( mHandbackHandler.equals( pHandback ) ) {
            lHandback = mHandback;
         }
         else {
            lHandback = pHandback;
         }
         mLocalListener.handleNotification(
            pNotification,
            lHandback
         );
      }
      
      /**
      * @return                     Object Handler of the given Handback
      *                           object
      **/
      public ObjectHandler getHandback() {
         return mHandbackHandler;
      }
      /** Redesign it (AS) **/
      public ObjectName getObjectName() {
         return mObjectName;
      }
      /** Redesign it (AS) **/
      public NotificationListener getLocalListener() {
         return mLocalListener;
      }
      /** Redesign it (AS) **/
      public long getId() {
         return hashCode() * mObjectName.hashCode();
      }
      /**
      * Test if this and the given Object are equal. This is true if the given
      * object both refer to the same local listener
      *
      * @param pTest                  Other object to test if equal
      *
      * @return                     True if both are of same type and
      *                           refer to the same local listener
      **/
      public boolean equals( Object pTest ) {
         if( pTest instanceof Listener ) {
            return mLocalListener.equals(
               ( (Listener) pTest).mLocalListener
            );
         }
         return false;
      }
      /**
      * @return                     Hashcode of the local listener
      **/
      public int hashCode() {
         return mLocalListener.hashCode();
      }
   }

   /**
   * Local JMX Listener to receive the message and send to the listener
   **/
   public class LocalJMSListener implements MessageListener {

      private NotificationListener         mLocalListener;
      private Object                      mHandback;
      
      public LocalJMSListener(
         NotificationListener pLocalListener,
         Object pHandback
      ) {
         mLocalListener = pLocalListener;
         mHandback = pHandback;
      }

      public void onMessage( Message pMessage ) {
         try {
            Notification lNotification = (Notification) ( (ObjectMessage) pMessage ).getObject();
            mLocalListener.handleNotification( lNotification, mHandback );
         }
         catch( JMSException je ) {
            je.printStackTrace();
         }
      }

      /** Redesign it (AS) **/
      public NotificationListener getLocalListener() {
         return mLocalListener;
      }
      /**
      * Test if this and the given Object are equal. This is true if the given
      * object both refer to the same local listener
      *
      * @param pTest                  Other object to test if equal
      *
      * @return                     True if both are of same type and
      *                           refer to the same local listener
      **/
      public boolean equals( Object pTest ) {
         if( pTest instanceof Listener ) {
            return mLocalListener.equals(
               ( (Listener) pTest).mLocalListener
            );
         }
         return false;
      }
      /**
      * @return                     Hashcode of the local listener
      **/
      public int hashCode() {
         return mLocalListener.hashCode();
      }
   }

   /**
   * Container for a JMS Listener Set to find later on the
   * Remote Listener based on the Object Name it is register
   * on and the local Notification Listener
   **/
   private class JMSListenerSet {
      
      private ObjectName mName;
      private NotificationListener mListener;
      private JMSNotificationListener mRemoteListener;
      
      public JMSListenerSet(
         ObjectName pName,
         NotificationListener pListener,
         JMSNotificationListener pRemoteListener
      ) {
         mName = pName;
         mListener = pListener;
         mRemoteListener = pRemoteListener;
      }
      
      public NotificationListener getRemoteListener() {
         return mRemoteListener;
      }
      
      public boolean equals( Object pTest ) {
         if( pTest instanceof JMSListenerSet ) {
            JMSListenerSet lTest = (JMSListenerSet) pTest;
            return mName.equals( lTest.mName ) &&
               mListener.equals( lTest.mListener );
         }
         return false;
      }
   }

}

// ----------------------------------------------------------------------------
// EOF
