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
import javax.management.RuntimeMBeanException;
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

import org.jboss.jmx.connector.rmi.RMIConnectorImpl;

import org.jboss.util.NestedRuntimeException;

/**
 * This is the equivalent to the RMI Connector but uses the
 * EJB Adaptor. The only advantage of using EJB-Adaptor is
 * that you can utilize the security.
 *
 * <p>
 * <b>ATTENTION</b>: Note that for the event transport (or in the
 * JMX Notations: Notification) the server must be able to
 * load the RMI Stubs and the Remote Listener classes.
 * Therefore you must make them available to the JMX Server
 * and the EJB-Adaptor (meaning the EJB-Container).
 *
 * <p>
 * Translates RemoteExceptions into MBeanExceptions where declared and
 * RuntimeMBeanExceptions when not declared. RuntimeMBeanException contain
 * NestedRuntimeException containg the root RemoteException due to
 * RuntimeMBeanException taking a RuntimeException and not a Throwable for
 * detail.
 *
 * @version <tt>$Revision: 1.9 $</tt>
 * @author  Andreas Schaefer (andreas.schaefer@madplanet.com)
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 **/
public class EJBConnector
   extends RMIConnectorImpl
   implements RemoteMBeanServer, EJBConnectorMBean
{
   // private Adaptor mRemoteAdaptor;
   // protected Vector mListeners = new Vector();
   protected String mJNDIServer;
   protected String mJNDIName;
   // protected int mEventType = NOTIFICATION_TYPE_RMI;
   // protected String[] mOptions = new String[ 0 ];

   /**
    * AS For evaluation purposes
    * Creates a Connector based on an already found Adaptor
    *
    * @param pAdaptor RMI-Adaptor used to connect to the remote JMX Agent
    **/
   public EJBConnector(AdaptorHome pAdaptorHome)
   {
      //
      // jason: should expose the exception thrown from create() instead
      //        of wrap it in an IAE
      //
      
      try {
         mRemoteAdaptor = pAdaptorHome.create();
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
   /*

   jason: This is pointless
   
   public String toString()
   {
      return "EJBAdaptorClient [ " + " ]";
   }

   */

   /**
    * Checks if the given instance is the same (same address) as
    * this instance.
    *
    * @return The result from the super class equals() method
    **/
   /*

   jason: This is pointless
   
   public boolean equals( Object pTest )
   {
      return super.equals( pTest );
   }
   */
   
   /**
    * Returns the hashcode of this instance
    *
    * @return Hashcode of its super class
    **/
   /*

   jason: This is pointless
   
   public int hashCode()
   {
      return super.hashCode();
   }
   */
   
   /**
    * Creates a SurveyManagement bean.
    *
    * @return Returns a SurveyManagement bean for use by the Survey handler.
    **/
   protected Adaptor getAdaptorBean( String pJNDIName )
      throws NamingException,
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
      }
      else {
         lJNDIContext = new InitialContext();
      }
      
      Object aEJBRef = lJNDIContext.lookup( pJNDIName );
      AdaptorHome aHome = (AdaptorHome) 
         PortableRemoteObject.narrow( aEJBRef, AdaptorHome.class );

      return aHome.create();
   }

   // JMXClientConnector implementation -------------------------------
   
   public void start(Object pServer)
      throws IllegalArgumentException
   {
      try {
         mRemoteAdaptor = getAdaptorBean( mJNDIName );
      }
      catch( Exception e ) {
         //
         // jason: why does start() only declare a IAE?
         //
         throw new NestedRuntimeException(e);
      }
   }
   
   public String getServerDescription() {
      return String.valueOf(mJNDIServer);
   }
}
