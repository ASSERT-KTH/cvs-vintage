/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.adaptor.ejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.jmx.ObjectHandler;

/**
* JMX EJB-Adaptor allowing a EJB client to work on a remote
* MBean Server.
*
* @author Andreas Schaefer
* @version $Revision: 1.4 $
*
* @ejb:bean type="Stateless" name="jmx/ejb/Adaptor" jndi-name="ejb/jmx/ejb/Adaptor"
* @ejb:env-entry name="Agent-Id" value="null"
* @ejb:env-entry name="Server-Number" value="-1"
*
**/
public class EJBAdaptorBean
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
   private MBeanServer mServer;
   /** Pool of object referenced by an object handler **/
   private Hashtable               mObjectPool = new Hashtable();
   /** Pool of registered listeners **/
   private Vector                  mListeners = new Vector();
   
   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  
   
   /**
   * Instantiate the given class on the remote MBeanServer and returns a Object 
   * Handler you can use to register it as a MBean with {@link #registerMBean
   * registerMBean()} or as a parameter to createMBean() or instantiate()
   * method which takes it as a parameter.
   *
   * @param pClassName Class name of the class to be loaded and instantiated
   *
   * @return Object handler. Please use this handler to register it as MBean
   *         or as a parameter in the other methods as a parameter. The
   *         server-side connector will look up for an object handler parameter
   *         and then replace the object handler by the effective object.
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectHandler instantiate(
      String pClassName
   ) throws
      ReflectionException,
      MBeanException,
      RemoteException
   {
      return assignObjectHandler(
         mServer.instantiate( pClassName )
      );
   }
   
   /**
   * Instantiate the given class on the remote MBeanServer and returns a Object 
   * Handler you can use to register it as a MBean with {@link #registerMBean
   * registerMBean()} or as a parameter to createMBean() or instantiate()
   * method which takes it as a parameter.
   *
   * @param pClassName Class name of the class to be loaded and instantiated
   * @param pLoaderName Name of the classloader to be used to
   *                    load this class
   *
   * @return Object handler. Please use this handler to register it as MBean
   *         or as a parameter in the other methods as a parameter. The
   *         server-side connector will look up for an object handler parameter
   *         and then replace the object handler by the effective object.
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectHandler instantiate(
      String pClassName,
      ObjectName pLoaderName
   ) throws
      ReflectionException,
      MBeanException,
      InstanceNotFoundException,
      RemoteException
   {
      return assignObjectHandler(
         mServer.instantiate( pClassName, pLoaderName )
      );
   }

   /**
   * Instantiate the given class on the remote MBeanServer and
   * returns a Object Handler you can use to register it as a
   * MBean with {@link #registerMBean registerMBean()}
   *
   * @param pClassName                  Class name of the class to be loaded 
   *                              and instantiated
   * @param pParams                  Array of parameter passed to the creator
   *                              of the class. If one is of data type
   *                              Object handler it will be replaced on
   *                              the server-side by its effective
   *                              object.
   * @param pSignature                  Array of Class Names (full qualified)
   *                              to find the right parameter. When there
   *                              is an ObjectHandler as a parameter type
   *                              then it will be replaced on the server-
   *                              side by the class name of the effective
   *                              object) otherwise it will be kept.
   *
   * @return                        Object handler. Please use this handler
   *                              to register it as MBean or as a parameter
   *                              in the other methods as a parameter. The
   *                              server-side connector will look up for
   *                              an object handler parameter and then
   *                              replace the object handler by the
   *                              effective object.
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectHandler instantiate(
      String pClassName,
      Object[] pParams,
      String[] pSignature
   ) throws
      ReflectionException,
      MBeanException,
      RemoteException
   {
      // First check the given parameters to see if there is an ObjectHandler
      // to be replaced
      checkForObjectHandlers(
         pParams,
         pSignature
      );
      // Instantiate the Object on the Server and then create and return the
      // remote reference
      return assignObjectHandler(
         mServer.instantiate(
            pClassName,
            pParams, 
            pSignature 
         )
      );
   }
   
   /**
   * Instantiate the given class on the remote MBeanServer and
   * returns a Object Handler you can use to register it as a
   * MBean with {@link #registerMBean registerMBean()}
   *
   * @param pClassName Class name of the class to be loaded 
   *                   and instantiated
   * @param pLoaderName Name of the classloader to be used to
   *                    load this class
   * @param pParams Array of parameter passed to the creator
   *                of the class. If one is of data type
   *                Object handler it will be replaced on
   *                the server-side by its effective
   *                object.
   * @param pSignature Array of Class Names (full qualified)
   *                   to find the right parameter. When there
   *                   is an ObjectHandler as a parameter type
   *                   then it will be replaced on the server-
   *                   side by the class name of the effective
   *                   object) otherwise it will be kept.
   *
   * @return Object handler. Please use this handler
   *         to register it as MBean or as a parameter
   *         in the other methods as a parameter. The
   *         server-side connector will look up for
   *         an object handler parameter and then
   *         replace the object handler by the
   *         effective object.
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectHandler instantiate(
      String pClassName,
      ObjectName pLoaderName,
      Object[] pParams,
      String[] pSignature
   ) throws
      ReflectionException,
      MBeanException,
      InstanceNotFoundException,
      RemoteException
   {
      // First check the given parameters to see if there is an ObjectHandler
      // to be replaced
      checkForObjectHandlers(
         pParams,
         pSignature
      );
      return assignObjectHandler(
         mServer.instantiate( 
            pClassName, 
            pLoaderName, 
            pParams, 
            pSignature 
         )
      );
   }

   /**
   * Instantiates the given class and registers it on the remote MBeanServer and
   * returns an Object Instance of the MBean.
   *
   * @param pClassName Class name of the class to be loaded 
   *                   and instantiated
   * @param pName Object Name the new MBean should be
   *              assigned to
   *
   * @return Object Instance of the new MBean
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectInstance createMBean(
      String pClassName,
      ObjectName pName
   ) throws
      ReflectionException,
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      MBeanException,
      NotCompliantMBeanException,
      RemoteException
   {
      return mServer.createMBean( pClassName, pName );
   }

   /**
   * Instantiates the given class and registers it on the remote MBeanServer and
   * returns an Object Instance of the MBean.
   *
   * @param pClassName Class name of the class to be loaded 
   *                   and instantiated
   * @param pName Object Name the new MBean should be
   *              assigned to
   * @param pLoaderName Name of the classloader to be used to
   *                    load this class
   *
   * @return Object Instance of the new MBean
   *
   * @ejb:interface-method type="remote"
   **/
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
      InstanceNotFoundException,
      RemoteException
   {
      return mServer.createMBean( pClassName, pName, pLoaderName );
   }

   /**
   * Instantiates the given class and registers it on the remote MBeanServer and
   * returns an Object Instance of the MBean.
   *
   * @param pClassName                  Class name of the class to be loaded 
   *                              and instantiated
   * @param pName               Object Name the new MBean should be
   *                              assigned to
   * @param pParams                  Array of parameter passed to the creator
   *                              of the class. If one is of data type
   *                              Object handler it will be replaced on
   *                              the server-side by its effective
   *                              object.
   * @param pSignature                  Array of Class Names (full qualified)
   *                              to find the right parameter. When there
   *                              is an ObjectHandler as a parameter type
   *                              then it will be replaced on the server-
   *                              side by the class name of the effective
   *                              object) otherwise it will be kept.
   *
   * @return                        Object Instance of the new MBean
   *
   * @ejb:interface-method type="remote"
   **/
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
      NotCompliantMBeanException,
      RemoteException
   {
      // First check the given parameters to see if there is an ObjectHandler
      // to be replaced
      checkForObjectHandlers(
         pParams,
         pSignature
      );
      return mServer.createMBean( pClassName, pName, pParams, pSignature );
   }

   /**
   * Instantiates the given class and registers it on the remote MBeanServer and
   * returns an Object Instance of the MBean.
   *
   * @param pClassName                  Class name of the class to be loaded 
   *                              and instantiated
   * @param pName               Object Name the new MBean should be
   *                              assigned to
   * @param pLoaderName Name of the classloader to be used to
   *                    load this class
   * @param pParams                  Array of parameter passed to the creator
   *                              of the class. If one is of data type
   *                              Object handler it will be replaced on
   *                              the server-side by its effective
   *                              object.
   * @param pSignature                  Array of Class Names (full qualified)
   *                              to find the right parameter. When there
   *                              is an ObjectHandler as a parameter type
   *                              then it will be replaced on the server-
   *                              side by the class name of the effective
   *                              object) otherwise it will be kept.
   *
   * @return                        Object Instance of the new MBean
   *
   * @ejb:interface-method type="remote"
   **/
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
      InstanceNotFoundException,
      RemoteException
   {
      // First check the given parameters to see if there is an ObjectHandler
      // to be replaced
      checkForObjectHandlers(
         pParams,
         pSignature
      );
      return mServer.createMBean( pClassName, pName, pLoaderName, pParams, pSignature );
   }

   /**
   * Register the given Object (already instantiated) as a MBean on the
   * remote MBeanServer
   *
   * @param pObjectHandler               Object Handler of th given object
   *                              to register as MBean
   * @param pNaemToAssign               Object Name to MBean is assigned to
   *
   * @return                        Object Instance of the new MBean
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectInstance registerMBean(
      ObjectHandler pObjectHandler,
      ObjectName pNameToAssign
   ) throws
      InstanceAlreadyExistsException,
      MBeanRegistrationException,
      NotCompliantMBeanException,
      RemoteException
   {
      return mServer.registerMBean(
         // Replace the Remote Reference by the actual object
         checkForObjectHandler( pObjectHandler ),
         pNameToAssign
      );
   }

   /**
   * Unregister a MBean from the Server
   *
   * @param pName Object Name of the MBean to be unregistered
   *
   * @ejb:interface-method type="remote"
   **/
   public void unregisterMBean(
      ObjectName pName
   ) throws
      InstanceNotFoundException,
      MBeanRegistrationException,
      RemoteException
   {
      mServer.unregisterMBean( pName );
   }

   /**
   * Return the Object Instance according to its Name
   *
   * @param pName Object Name of the MBean of which the Object
   *              Instance is requested
   *
   * @ejb:interface-method type="remote"
   **/
   public ObjectInstance getObjectInstance(
      ObjectName pName
   ) throws
      InstanceNotFoundException,
      RemoteException
   {
      return mServer.getObjectInstance( pName );
   }

   /**
   * Returns a Set of Object Instances matching the query parameters
   *
   * @param pName Object Name either partially or fully qualified or
   *              null if not restrictions
   * @param pQuery Query Expression to restrict the query or null if no
   *
   * @return Set of Object Instance which is never null but maybe empty
   *
   * @ejb:interface-method type="remote"
   **/
   public Set queryMBeans(
      ObjectName pName,
      QueryExp pQuery
   ) throws
      RemoteException
   {
      return mServer.queryMBeans( pName, pQuery );
   }

   /**
   * Returns a Set of Object Names matching the query parameters
   *
   * @param pName Object Name either partially or fully qualified or
   *              null if not restrictions
   * @param pQuery Query Expression to restrict the query or null if no
   *
   * @return Set of Object Names which is never null but maybe empty
   *
   * @ejb:interface-method type="remote"
   **/
   public Set queryNames(
      ObjectName pName,
      QueryExp pQuery
   ) throws
      RemoteException
   {
      return mServer.queryNames( pName, pQuery );
   }

   /**
   * Checks if a specified MBean is already register on the server
   *
   * @param pName Object Name of the MBean to check for
   *
   * @return Treu if the given MBean is already registered
   *
   * @ejb:interface-method type="remote"
   **/
   public boolean isRegistered(
      ObjectName pName
   ) throws
      RemoteException
   {
      return mServer.isRegistered( pName );
   }
    
   /**
   * Checks if a specified MBean is an instance of the given class
   *
   * @param pName Object Name of the MBean to check for
   * @param pClassName Fully qualified classname to check for
   *
   * @return Treu if the given MBean is an instance of this class
   *
   * @ejb:interface-method type="remote"
   **/
   public boolean isInstanceOf(
      ObjectName pName,
      String pClassName
   ) throws
      InstanceNotFoundException,
      RemoteException
   {
      return mServer.isInstanceOf( pName, pClassName );
   }

   /**
   * @return Number of registered MBeans on the server
   *
   * @ejb:interface-method type="remote"
   **/
   public Integer getMBeanCount(
   ) throws
      RemoteException
   {
      return mServer.getMBeanCount();
   }

   /**
   * Returns the Value of the given Attribute
   *
   * @param pName Name of the MBean to get one of its attribute
   * @param pAttribute Name of the Attribute
   *
   * @param Value of the attribute
   *
   * @ejb:interface-method type="remote"
   **/
   public Object getAttribute(
      ObjectName pName,
      String pAttribute
   ) throws
      MBeanException,
      AttributeNotFoundException,
      InstanceNotFoundException,
      ReflectionException,
      RemoteException
   {
      return mServer.getAttribute( pName, pAttribute );
   }

   /**
   * Returns the Values of the given Attribute List
   *
   * @param pName Name of the MBean to get its attributes
   * @param pAttributes Array of Attribute Name
   *
   * @param Attribute List containing the Attribute Values
   *
   * @ejb:interface-method type="remote"
   **/
   public AttributeList getAttributes(
      ObjectName pName,
      String[] pAttributes
   ) throws
      InstanceNotFoundException,
      ReflectionException,
      RemoteException
   {
      return mServer.getAttributes( pName, pAttributes );
   }

   /**
   * Set the Value of the given Attribute
   *
   * @param pName Name of the MBean to set one of its attribute
   * @param pAttribute Attribute instance containg the name and value
   *                   of the attribute to be set
   *
   * @ejb:interface-method type="remote"
   **/
   public void setAttribute(
      ObjectName pName,
      Attribute pAttribute
   ) throws
      InstanceNotFoundException,
      AttributeNotFoundException,
      InvalidAttributeValueException,
      MBeanException,
      ReflectionException,
      RemoteException
   {
      mServer.setAttribute( pName, pAttribute );
   }

   /**
   * Set the Values of the given Attributes
   *
   * @param pName Name of the MBean to set its attributes
   * @param pAttribute Attribute List instance containg the name and value
   *                   of the attributes to be set
   *
   * @return Attribute List with the new and current values of the Attributes
   *
   * @ejb:interface-method type="remote"
   **/
   public AttributeList setAttributes(
      ObjectName pName,
      AttributeList pAttributes
   ) throws
      InstanceNotFoundException,
      ReflectionException,
      RemoteException
   {
      return mServer.setAttributes( pName, pAttributes );
   }

   /**
   * Invokes a method on the given MBean
   *
   * @param pName Object Name of the MBean to invoke the method on
   * @param pActionName Name of the operation
   * @param pParams List of parameter values to be passed to the method
   * @param pSignature List of the fully qualified classnames of the
   *                   parameters to be passed to the method
   *
   * @return Return value (if available) of the method invocation
   *
   * @ejb:interface-method type="remote"
   **/
   public Object invoke(
      ObjectName pName,
      String pActionName,
      Object[] pParams,
      String[] pSignature
   ) throws
      InstanceNotFoundException,
      MBeanException,
      ReflectionException,
      RemoteException
   {
      // First check the given parameters to see if there is an ObjectHandler
      // to be replaced
      checkForObjectHandlers(
         pParams,
         pSignature
      );
      return mServer.invoke( pName, pActionName, pParams, pSignature );
   }

   /**
   * @return Default Domain of the Server
   *
   * @ejb:interface-method type="remote"
   **/
   public String getDefaultDomain(
   ) throws
      RemoteException
   {
      return mServer.getDefaultDomain();
   }

   /**
   * Adds the given Notification Listener in a way that
   * the Notification Events are send back to the listener
   * <BR>
   * Please asume that the listening is terminated when
   * the instance of these interface goes down.
   *
   * @param pBroadcasterName      Name of the Broadcaster MBean on
   *                  the remote side
   * @param pListener         Local notification listener
   * @param pFilter            In general there are three ways this
   *                  could work:
   *                  1) A copy of the filter is send to the
   *                  server but then the Filter cannot interact
   *                  with the client. This is default.
   *                  2) Wrapper around the filter therefore
   *                  the server sends the filter call to the
   *                  client to be performed. This filter must be
   *                  a subclass of RemoteNotificationListener.
   *                  3) All Notification events are sent to the
   *                  client and the client performs the filtering.
   *                  This filter must be a subclass of
   *                  LocalNotificationListener.
   * @param pHandback         Object to be send back to the listener. To
   *                  make it complete transparent to the client
   *                  an Object Handler is sent to the Server and
   *                  and when a Notification comes back it will
   *                  be looked up and send to the client. Therefore
   *                  it must not be serializable.
   *
   * @ejb:interface-method type="remote"
   **/
   public void addNotificationListener(
      ObjectName pName,
      NotificationListener pListener,
      NotificationFilter pFilter,
      Object pHandback      
   ) throws
      InstanceNotFoundException,
      RemoteException
   {
      mServer.addNotificationListener( pName, pListener, pFilter, pHandback );
   }

   /**
   * Remoes the given Notification Listener in a way that
   * all involved instances are removed and the remote
   * listener is removed from the broadcaster.
   * <BR>
   * Please asume that the listening is terminated when
   * the instance of these interface goes down.
   *
   * @param pBroadcasterName      Name of the Broadcaster MBean on
   *                  the remote side
   * @param pListener         Local notification listener
   *
   * @ejb:interface-method type="remote"
   **/
   public void removeNotificationListener(
      ObjectName pName,
      NotificationListener pListener
   ) throws
      InstanceNotFoundException,
      ListenerNotFoundException,
      RemoteException
   {
      mServer.removeNotificationListener( pName, pListener );
   }

   /**
   * Returns the MBean Info of the requested MBean
   *
   * @param pName Object Name of the MBean its info is requested
   *
   * @return Information of the MBean
   *
   * @ejb:interface-method type="remote"
   **/
   public MBeanInfo getMBeanInfo(
      ObjectName pName
   ) throws
      InstanceNotFoundException,
      IntrospectionException,
      ReflectionException,
      RemoteException
   {
      return mServer.getMBeanInfo( pName );
   }

   /**
   * Adds the given Notification Listener in a way that
   * the Notification Events are send back to the listener
   * <BR>
   * Please asume that the listening is terminated when
   * the instance of these interface goes down.
   *
   * @param pBroadcasterName      Name of the Broadcaster MBean on
   *                  the remote side
   * @param pListener Object Name of the Listener MBean acting as
   *                  Notification Listener
   * @param pFilter            In general there are three ways this
   *                  could work:
   *                  1) A copy of the filter is send to the
   *                  server but then the Filter cannot interact
   *                  with the client. This is default.
   *                  2) Wrapper around the filter therefore
   *                  the server sends the filter call to the
   *                  client to be performed. This filter must be
   *                  a subclass of RemoteNotificationListener.
   *                  3) All Notification events are sent to the
   *                  client and the client performs the filtering.
   *                  This filter must be a subclass of
   *                  LocalNotificationListener.
   * @param pHandback         Object to be send back to the listener
   *
   * @ejb:interface-method type="remote"
   **/
   public void addNotificationListener(
      ObjectName pName,
      ObjectName pListener,
      NotificationFilter pFilter,
      Object pHandback      
   ) throws
      InstanceNotFoundException,
      RemoteException
   {
      mServer.addNotificationListener( pName, pListener, pFilter, pHandback );
   }

   /**
   * Remoes the given Notification Listener in a way that
   * all involved instances are removed and the remote
   * listener is removed from the broadcaster.
   * <BR>
   * Please asume that the listening is terminated when
   * the instance of these interface goes down.
   *
   * @param pBroadcasterName      Name of the Broadcaster MBean on
   *                  the remote side
   * @param pListener Object Name of the Listener MBean acting as
   *                  Notification Listener
   *
   * @ejb:interface-method type="remote"
   **/
   public void removeNotificationListener(
      ObjectName pName,
      ObjectName pListener
   ) throws
      InstanceNotFoundException,
      ListenerNotFoundException,
      RemoteException
   {
      mServer.removeNotificationListener( pName, pListener );
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
      if( mServer == null ) {
         try {
            Context aJNDIContext = new InitialContext();
            String lAgentId = null;
            String lServerNumber = "-1";
            lAgentId = (String) aJNDIContext.lookup( 
               "java:comp/env/Agent-Id" 
            );
            lServerNumber = (String) aJNDIContext.lookup( 
               "java:comp/env/Server-Number" 
            );
            if( lAgentId == null || lAgentId.equals( "" ) || lAgentId.equals( "null" ) ) {
               lAgentId = null;
            }
            if( lServerNumber == null || lServerNumber.equals( "" ) ) {
               lServerNumber = "-1";
            }
            int lNumber = new Integer( lServerNumber ).intValue();
            ArrayList lServers = MBeanServerFactory.findMBeanServer( lAgentId );
            if( lNumber >= 0 && lNumber < lServers.size() ) {
               mServer = (MBeanServer) lServers.get( lNumber );
            } else {
               mServer = (MBeanServer) lServers.get( 0 );
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
      return "SurveyManagementBean [ " + " ]";
   }
   
   // -------------------------------------------------------------------------
   // Private Methods
   // -------------------------------------------------------------------------  
   
   /**
   * Checks in the given list of object if there is one of type ObjectHandler
   * and if it will replaced by the referenced object. In addition it checks
   * if the given signature is of type ObjectHandler and if then it replace
   * it by the type of the referenced object.
   * <BR>
   * Please note that this method works directly on the given arrays!
   *
   * @param pListOfObjects               Array of object to be checked
   * @param pSignature                  Array of class names (full paht)
   *                              beeing the signature for the object
   *                              on the according object (list above)
   */
   private void checkForObjectHandlers(
      Object[] pListOfObjects,
      String[] pSignature
   ) {
      for( int i = 0; i < pListOfObjects.length; i++ ) {
         Object lEffective = checkForObjectHandler( pListOfObjects[ i ] );
         if( pListOfObjects[ i ] != lEffective ) {
            // Replace the Object Handler by the effective object
            pListOfObjects[ i ] = lEffective;
            if( i < pSignature.length ) {
               if( pSignature[ i ].equals( ObjectHandler.class.getName() ) ) {
                  pSignature[ i ] = lEffective.getClass().getName();
               }
            }
         }
      }
   }
   /**
   * Checks if the given object is of type ObjectHandler and if then
   * it replaces by the object referenced by the ObjectHandler
   *
   * @param pObjectToCheck               Object to be checked
   *
   * @return                        The given object if not a reference or
   *                              or the referenced object
   */
   private Object checkForObjectHandler( Object pObjectToCheck ) {
      if( pObjectToCheck instanceof ObjectHandler ) {
         return mObjectPool.get(
            pObjectToCheck
         );
      }
      else {
         return pObjectToCheck;
      }
   }
   
   /**
   * Creates an ObjectHandler for the given object and store it on
   * this side
   *
   * @param pNewObject                  New object to be referenced by an
   *                              ObjectHandler
   *
   * @return                        Object Handler which stands for a
   *                              remote reference to an object created
   *                              and only usable on this side
   */
   private ObjectHandler assignObjectHandler( Object pNewObject ) {
      ObjectHandler lObjectHandler = new ObjectHandler(
         this.toString()
      );
      mObjectPool.put(
         lObjectHandler,
         pNewObject
      );
      return lObjectHandler;
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
   
}

// ----------------------------------------------------------------------------
// EOF
