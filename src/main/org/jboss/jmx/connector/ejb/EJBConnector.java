/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.ejb;

import org.jboss.jmx.adaptor.interfaces.Adaptor;
import org.jboss.jmx.adaptor.interfaces.AdaptorHome;

import java.io.ObjectInputStream;
import java.io.Serializable;
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
import javax.management.RuntimeOperationsException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.jboss.jmx.connector.RemoteMBeanServer;
import org.jboss.jmx.connector.notification.ClientNotificationListener;
import org.jboss.jmx.connector.notification.JMSClientNotificationListener;
import org.jboss.jmx.connector.notification.PollingClientNotificationListener;
import org.jboss.jmx.connector.notification.RMIClientNotificationListener;
import org.jboss.jmx.connector.notification.SearchClientNotificationListener;

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
* @version $Revision: 1.8 $
**/
public class EJBConnector
   implements RemoteMBeanServer, EJBConnectorMBean
{

   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
   
   // -------------------------------------------------------------------------
   // Members 
   // -------------------------------------------------------------------------  

   private Adaptor mAdaptor;

   private Vector            mListeners = new Vector();
   private String            mJNDIServer;
   private String            mJNDIName;
   private int               mEventType = NOTIFICATION_TYPE_RMI;
   private String[]          mOptions = new String[ 0 ];

   // -------------------------------------------------------------------------
   // Constructor
   // -------------------------------------------------------------------------
    
   /**
   * AS For evaluation purposes
   * Creates a Connector based on an already found Adaptor
   *
   * @param pAdaptor RMI-Adaptor used to connect to the remote JMX Agent
   **/
   public EJBConnector(
      AdaptorHome pAdaptorHome
   ) {
      try {
         mAdaptor = pAdaptorHome.create();
      }
      catch( Exception e ) {
         throw new IllegalArgumentException( "Adaptor could not be created: " + e.getMessage() );
      }
   }

   /**
   * Creates an Client Connector using the EJB-Adaptor to
   * access the remote JMX Server.
   *
   * @param pType Defines the type of event transport. Please have a
   *              look at the constants with the prefix NOTIFICATION_TYPE
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
   *              look at the constants with the prefix NOTIFICATION_TYPE
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
   *              look at the constants with the prefix NOTIFICATION_TYPE
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
      if( pType == NOTIFICATION_TYPE_RMI || pType == NOTIFICATION_TYPE_JMS
         || pType == NOTIFICATION_TYPE_POLLING ) {
         mEventType = pType;
      }
      if( pOptions != null ) {
         mOptions = pOptions;
      }
      if( pJNDIName == null || pJNDIName.trim().length() == 0 ) {
         mJNDIName = "ejb/jmx/ejb/Adaptor";
      } else {
         mJNDIName = pJNDIName;
      }
      if( pJNDIServer == null || pJNDIServer.trim().length() == 0 ) {
         mJNDIServer = null;
      } else {
         mJNDIServer = pJNDIServer;
      }
      start( null );
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
      throws
         NamingException,
         RemoteException,
         CreateException
   {
      Context lJNDIContext = null;
      // The Adaptor can be registered on another JNDI-Server therefore
      // the user can overwrite the Provider URL
      if( mJNDIServer != null ) {
         Hashtable lProperties = new Hashtable();
         lProperties.put( Context.PROVIDER_URL, mJNDIServer );
         lJNDIContext = new InitialContext( lProperties );
      } else {
         lJNDIContext = new InitialContext();
      }
      Object aEJBRef = lJNDIContext.lookup( pJNDIName );
      AdaptorHome aHome = (AdaptorHome) 
         PortableRemoteObject.narrow( aEJBRef, AdaptorHome.class );

      return aHome.create();
   }
   
   // -------------------------------------------------------------------------
   // MBeanServer Implementations
   // -------------------------------------------------------------------------  
   
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
         re.printStackTrace();
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
         throw new RuntimeException( "Remote access to perform this operation failed: " + re.getMessage() );
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
                  (String) mJNDIServer,
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
         throw new RuntimeException( "Remote access to perform this operation failed: " + e.getMessage() );
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

	// JMXClientConnector implementation -------------------------------
	public void start(
		Object pServer
	) throws IllegalArgumentException {
      try {
         mAdaptor = getAdaptorBean( mJNDIName );
      }
      catch( Exception e ) {
         throw new IllegalArgumentException( "Adaptor could not be found or created: " + e.getMessage() );
      }
	}

	public void stop() {
      if( mAdaptor != null ) {
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
         mAdaptor = null;
      }
	}
	
	public boolean isAlive() {
		return mAdaptor != null;
	}

	public String getServerDescription() {
		return "" + mJNDIServer;
	}
}

// ----------------------------------------------------------------------------
// EOF
