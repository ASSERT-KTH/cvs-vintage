/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.mejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.IntrospectionException;
import javax.management.ListenerNotFoundException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.j2ee.ManagementHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.jmx.connector.RemoteMBeanServer;
import org.jboss.management.j2ee.J2EEManagedObject;

/**
* Management Session Bean to enable the client to manage the
* server its is deployed on.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
* @version $Revision: 1.2 $
*
* @ejb:bean name="MEJB"
*           display-name="JBoss Management EJB (MEJB)"
*           type="Stateless"
*           jndi-name="ejb/mgmt/J2EEManagement"
* @--ejb:interface generate="none"
*                remote-class="javax.management.j2ee.Management"
* @ejb:interface extends="javax.management.j2ee.Management"
* @--ejb:home extends="javax.management.j2ee.ManagementHome"
* @ejb:home generate="none"
*           remote-class="javax.management.j2ee.ManagementHome"
* @ejb:env-entry description="JNDI-Name of the MBeanServer to be used to look it up. If 'null' the first of all listed local MBeanServer is taken"
*                name="Server-Name"
*                value="null"
*
**/
public class ManagementBean
   implements SessionBean
{
   // -------------------------------------------------------------------------
   // Static
   // -------------------------------------------------------------------------
   
   // -------------------------------------------------------------------------
   // Members 
   // -------------------------------------------------------------------------
   
   private SessionContext mContext;
   /**
   * Reference to the MBeanServer all the methods of this Connector are
   * forwarded to
   **/
   private RemoteMBeanServer mConnector;
   
   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Object getAttribute( ObjectName pName, String pAttribute )
      throws
         MBeanException,
         AttributeNotFoundException,
         InstanceNotFoundException,
         ReflectionException,
         RemoteException
   {
      return mConnector.getAttribute( pName, pAttribute );
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public AttributeList getAttributes( ObjectName pName, String[] pAttributes )
      throws
         InstanceNotFoundException,
         ReflectionException,
         RemoteException
   {
      return mConnector.getAttributes( pName, pAttributes );
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public String getDefaultDomain()
      throws RemoteException
   {
      return J2EEManagedObject.getDomainName();
   }
   
   /**
   * @throws RemoteException Necessary for a EJB
   *
   * @ejb:interface-method view-type="remote"
   **/
   public Integer getMBeanCount()
      throws RemoteException
   {
      try {
         return new Integer(
            queryNames(
               new ObjectName( getDefaultDomain() + ":*" )
            ).size()
         );
      }
      catch( Exception e ) {
      }
      return new Integer( 0 );
   }
   
   public MBeanInfo getMBeanInfo( ObjectName pName )
      throws
         IntrospectionException,
         InstanceNotFoundException,
         ReflectionException,
         RemoteException
   {
      return mConnector.getMBeanInfo( pName );
   }
   
   public ListenerRegistration getListenerRegistry()
      throws RemoteException
   {
      return new ListenerRegistration(
         (ManagementHome) mContext.getEJBObject().getEJBHome(),
         new String[] {}
      );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public Object invoke( ObjectName pName, String pOperationName, Object[] pParams, String[] pSignature )
      throws
         InstanceNotFoundException,
         MBeanException,
         ReflectionException,
         RemoteException
   {
      return mConnector.invoke(
         pName,
         pOperationName,
         pParams,
         pSignature
      );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public boolean isRegistered( ObjectName pName )
      throws RemoteException
   {
      return mConnector.isRegistered( pName );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public Set queryNames( ObjectName pName )
      throws RemoteException
   {
      return mConnector.queryNames( pName, null );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void setAttribute( ObjectName pName, Attribute pAttribute )
      throws
         AttributeNotFoundException,
         InstanceNotFoundException,
         InvalidAttributeValueException,
         MBeanException,
         ReflectionException,
         RemoteException
   {
      mConnector.setAttribute( pName, pAttribute );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public AttributeList setAttributes( ObjectName pName, AttributeList pAttributes )
      throws
         InstanceNotFoundException,
         ReflectionException,
         RemoteException
   {
      return mConnector.setAttributes( pName, pAttributes );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public ObjectInstance createMBean(
      String pClass,
      ObjectName pName,
      Object[] pParameters,
      String[] pSignature
   )
      throws
         InstanceAlreadyExistsException,
         MBeanException,
         MBeanRegistrationException,
         NotCompliantMBeanException,
         ReflectionException,
         RemoteException
   {
      System.out.println(
         "ManagementBean.createMBean(), class: " + pClass +
         ", name: " + pName +
         ", parameters: " + pParameters +
         ", signature: " + pSignature
      );
      try {
      return mConnector.createMBean( pClass, pName, pParameters, pSignature );
      }
      catch( ReflectionException e1 ) {
         e1.printStackTrace();
         throw e1;
      }
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void unregisterMBean(
      ObjectName pName
   )
      throws
         InstanceNotFoundException,
         MBeanRegistrationException,
         RemoteException
   {
      mConnector.unregisterMBean( pName );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void addNotificationListener(
      ObjectName pBroadcaster,
      ObjectName pListener,
      NotificationFilter pFilter,
      Object pHandback
   )
      throws
         InstanceNotFoundException,
         RemoteException
   {
      mConnector.addNotificationListener( pBroadcaster, pListener, pFilter, pHandback );
   }
   
   /**
    * @throws RemoteException Necessary for a EJB
    *
    * @ejb:interface-method view-type="remote"
    **/
   public void removeNotificationListener(
      ObjectName pBroadcaster,
      ObjectName pListener
   )
      throws
         InstanceNotFoundException,
         ListenerNotFoundException,
         RemoteException
   {
      mConnector.removeNotificationListener( pBroadcaster, pListener );
   }
   
   /**
   * Create the Session Bean which takes the first available
   * MBeanServer as target server
   *
   * @throws CreateException 
   *
   * @ejb:create-method
   **/
   public void ejbCreate()
      throws
         CreateException
   {
      if( mConnector == null ) {
         try {
            Context aJNDIContext = new InitialContext();
            String lServerName = ( (String) aJNDIContext.lookup( 
               "java:comp/env/Server-Name" 
            ) ).trim();
            if( lServerName == null || lServerName.length() == 0 || lServerName.equals( "null" ) ) {
               ArrayList lServers = MBeanServerFactory.findMBeanServer( null );
               if( lServers.size() > 0 ) {
                  mConnector = new LocalConnector( (MBeanServer) lServers.get( 0 ) );
               } else {
                  throw new CreateException(
                     "No local JMX MBeanServer available"
                  );
               }
            } else {
               Object lServer = aJNDIContext.lookup( lServerName );
               if( lServer != null ) {
                  if( lServer instanceof MBeanServer ) {
                     mConnector = new LocalConnector( (MBeanServer) lServer );
                  } else
                  if( lServer instanceof RemoteMBeanServer ) {
                     mConnector = (RemoteMBeanServer) lServer;
                  } else {
                     throw new CreateException(
                        "Server: " + lServer + " reference by Server-Name: " + lServerName +
                        " is not of type MBeanServer or RemoteMBeanServer: "
                     );
                  }
               } else {
                  throw new CreateException(
                     "Server-Name " + lServerName + " does not reference an Object in JNDI"
                  );
               }
            }
         }
         catch( NamingException ne ) {
            throw new EJBException( ne );
         }
      }
   }
   
   /**
   * Describes the instance and its content for debugging purpose
   *
   * @return Debugging information about the instance and its content
   **/
   public String toString()
   {
      return "Management [ " + " ]";
   }
   
   // -------------------------------------------------------------------------
   // Framework Callbacks
   // -------------------------------------------------------------------------  
   
   /**
   * Set the associated session context. The container invokes this method on 
   * an instance after the instance has been created. 
   * <p>This method is called with no transaction context.
   *
   * @param aContext A SessionContext interface for the instance. The instance 
   *  should store the reference to the context in an instance variable.
   * @throws EJBException Should something go wrong while seting the context,
   *  an EJBException will be thrown.
   **/
   public void setSessionContext( SessionContext aContext )
      throws
         EJBException
   {
      mContext = aContext;
   }
   
   
   /**
   * The activate method is called when the instance is activated from its 
   * "passive" state. The instance should acquire any resource that it has 
   * released earlier in the ejbPassivate() method. 
   * <p>This method is called with no transaction context.
   *
   * @throws EJBException Thrown by the method to indicate a failure caused 
   *  by a system-level error
   **/
   public void ejbActivate()
      throws
         EJBException
   {
   }
   
   
   /**
   * The passivate method is called before the instance enters the "passive" 
   * state. The instance should release any resources that it can re-acquire 
   * later in the ejbActivate() method. 
   * <p>After the passivate method completes, the instance must be in a state 
   * that allows the container to use the Java Serialization protocol to 
   * externalize and store away the instance's state. 
   * <p>This method is called with no transaction context.
   *
   * @throws EJBException Thrown by the method to indicate a failure caused 
   *  by a system-level error
   **/
   public void ejbPassivate()
      throws
         EJBException
   {
   }
   
   
   /**
   * A container invokes this method before it ends the life of the session 
   * object. This happens as a result of a client's invoking a remove 
   * operation, or when a container decides to terminate the session object 
   * after a timeout. 
   * <p>This method is called with no transaction context.
   *
   * @throws EJBException Thrown by the method to indicate a failure caused 
   *  by a system-level error
   **/
   public void ejbRemove()
      throws
         EJBException
   {
   }

   private class LocalConnector implements RemoteMBeanServer {
      
      private MBeanServer mServer = null;
      
      public LocalConnector( MBeanServer pServer ) {
         mServer = pServer;
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
         return mServer.createMBean( pClassName, pName );
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
         return mServer.createMBean( pClassName, pName, pLoaderName );
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
         return mServer.createMBean( pClassName, pName, pParams, pSignature );
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
         return mServer.createMBean( pClassName, pName, pLoaderName, pParams, pSignature );
      }
      
      public void unregisterMBean(
         ObjectName pName
      ) throws
         InstanceNotFoundException,
         MBeanRegistrationException
      {
         mServer.unregisterMBean( pName );
      }
      
      public ObjectInstance getObjectInstance(
         ObjectName pName
      ) throws
         InstanceNotFoundException
      {
         return mServer.getObjectInstance( pName );
      }
      
      public Set queryMBeans(
         ObjectName pName,
         QueryExp pQuery
      ) {
         return mServer.queryMBeans( pName, pQuery );
      }
      
      public Set queryNames(
         ObjectName pName,
         QueryExp pQuery
      ) {
         return mServer.queryNames( pName, pQuery );
      }
      
      public boolean isRegistered(
         ObjectName pName
      ) {
         return mServer.isRegistered( pName );
      }
      
      public boolean isInstanceOf(
         ObjectName pName,
         String pClassName
      ) throws
         InstanceNotFoundException
      {
         return mServer.isInstanceOf( pName, pClassName );
      }
      
      public Integer getMBeanCount(
      ) {
         return mServer.getMBeanCount();
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
         return mServer.getAttribute( pName, pAttribute );
      }
      
      public AttributeList getAttributes(
         ObjectName pName,
         String[] pAttributes
      ) throws
         InstanceNotFoundException,
         ReflectionException
      {
         return mServer.getAttributes( pName, pAttributes );
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
         mServer.setAttribute( pName, pAttribute );
      }
      
      public AttributeList setAttributes(
         ObjectName pName,
         AttributeList pAttributes
      ) throws
         InstanceNotFoundException,
         ReflectionException
      {
         return mServer.setAttributes( pName, pAttributes );
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
         return mServer.invoke( pName, pActionName, pParams, pSignature );
      }
      
      public String getDefaultDomain(
      ) {
         return mServer.getDefaultDomain();
      }
      
      public MBeanInfo getMBeanInfo(
         ObjectName pName
      ) throws
         InstanceNotFoundException,
         IntrospectionException,
         ReflectionException
      {
         return mServer.getMBeanInfo( pName );
      }
      
      public void addNotificationListener(
         ObjectName pName,
         NotificationListener pListener,
         NotificationFilter pFilter,
         Object pHandback		
      ) throws
         InstanceNotFoundException
      {
         mServer.addNotificationListener( pName, pListener, pFilter, pHandback );
      }
      
      public void removeNotificationListener(
         ObjectName pName,
         NotificationListener pListener
      ) throws
         InstanceNotFoundException,
         ListenerNotFoundException
      {
         mServer.removeNotificationListener( pName, pListener );
      }
      
      public void addNotificationListener(
         ObjectName pName,
         ObjectName pListener,
         NotificationFilter pFilter,
         Object pHandback		
      ) throws
         InstanceNotFoundException
      {
         mServer.addNotificationListener( pName, pListener, pFilter, pHandback );
      }
      
      public void removeNotificationListener(
         ObjectName pName,
         ObjectName pListener
      ) throws
         InstanceNotFoundException,
         ListenerNotFoundException,
         UnsupportedOperationException
      {
         mServer.removeNotificationListener( pName, pListener );
      }
      
   }
}
