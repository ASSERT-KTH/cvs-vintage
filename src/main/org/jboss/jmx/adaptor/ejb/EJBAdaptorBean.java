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

import org.jboss.jmx.connector.RemoteMBeanServer;

import org.jboss.util.jmx.MBeanServerLocator;

import org.jboss.logging.Logger;

/**
 * JMX EJB-Adaptor allowing a EJB client to work on a remote
 * MBean Server.
 *
 * @ejb:bean type="Stateless"
 *           name="jmx/ejb/Adaptor"
 *           jndi-name="ejb/jmx/ejb/Adaptor"
 *           remote-business-interface="org.jboss.jmx.adaptor.rmi.RMIAdaptor"
 * @ejb:env-entry description="JNDI-Name of the MBeanServer to be used to look it up. If 'null' the first of all listed local MBeanServer is taken"
 *                name="Server-Name"
 *                value="null"
 * @ejb:transaction type="Supports"
 *
 * @todo implement notifications
 * @todo convert to mbeanserverconnection
 * @version <tt>$Revision: 1.10 $</tt>
 * @author  Andreas Schaefer
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class EJBAdaptorBean
   implements SessionBean
{
   private Logger log = Logger.getLogger(this.getClass());
   
   /** The EJB session context. */
   private SessionContext mContext;

   /** The mbean server which we will delegate the work too. */
   private MBeanServer mServer;

   /** Pool of registered listeners **/
   private Vector mListeners = new Vector();
   
   /**
    * Create the Session Bean which takes the first available
    * MBeanServer as target server
    *
    * @throws CreateException 
    *
    * @ejb:create-method
    **/
   public void ejbCreate() throws CreateException
   {
      if (mServer != null ) {
         return;
      }
      
      try {
         Context ctx = new InitialContext();
         
         String lServerName = ((String)ctx.lookup("java:comp/env/Server-Name")).trim();
         
         if( lServerName == null || lServerName.length() == 0 || lServerName.equals( "null" ) ) {
            mServer = MBeanServerLocator.locate();
            if (mServer == null) {
               throw new CreateException("No local JMX MBeanServer available");
            }
         }
         else {
            Object lServer = ctx.lookup( lServerName );
            
            if( lServer != null ) {
               if( lServer instanceof MBeanServer ) {
                  mServer = (MBeanServer)lServer;
               }
               else {
                  if( lServer instanceof RemoteMBeanServer ) {
                     mServer = (RemoteMBeanServer) lServer;
                  }
                  else {
                     throw new CreateException(
                        "Server: " + lServer + " reference by Server-Name: " + lServerName +
                        " is not of type MBeanServer or RemoteMBeanServer: "
                     );
                  }
               }
            }
            else {
               throw new CreateException(
                  "Server-Name " + lServerName + " does not reference an Object in JNDI"
                  );
            }
         }
         
         ctx.close();
      }
      catch( NamingException ne ) {
         throw new EJBException( ne );
      }
   }
   
   // -------------------------------------------------------------------------
   // EJB Framework Callbacks
   // -------------------------------------------------------------------------  
   
   public void setSessionContext( SessionContext aContext )
      throws EJBException
   {
      mContext = aContext;
   }
   
   public void ejbActivate() throws EJBException
   {
      // empty
   }

   public void ejbPassivate() throws EJBException
   {
      // empty
   }
   
   public void ejbRemove() throws EJBException
   {
      // empty
   }

   // RMIAdaptor implementation -------------------------------------

   public Object instantiate(String className)
      throws ReflectionException, MBeanException, RemoteException
   {
      return mServer.instantiate(className);
   }
   
   public Object instantiate(String className, ObjectName loaderName) 
      throws ReflectionException, MBeanException, InstanceNotFoundException, RemoteException
   {
      return mServer.instantiate(className, loaderName);
   }
   
   public Object instantiate(String className, Object[] params, String[] signature)
      throws ReflectionException, MBeanException, RemoteException
   {
      return mServer.instantiate(className, params, signature);      
   }

   public Object instantiate(String className,
                             ObjectName loaderName,
                             Object[] params,
                             String[] signature)
      throws ReflectionException, MBeanException, InstanceNotFoundException, RemoteException
   {
      return mServer.instantiate(className, loaderName, params, signature);
   }
   
   public ObjectInstance createMBean(String pClassName, ObjectName pName)
      throws ReflectionException,
             InstanceAlreadyExistsException,
             MBeanRegistrationException,
             MBeanException,
             NotCompliantMBeanException,
             RemoteException
   {
      return mServer.createMBean( pClassName, pName );
   }

   public ObjectInstance createMBean(String pClassName,
                                     ObjectName pName,
                                     ObjectName pLoaderName)
      throws ReflectionException,
             InstanceAlreadyExistsException,
             MBeanRegistrationException,
             MBeanException,
             NotCompliantMBeanException,
             InstanceNotFoundException,
             RemoteException
   {
      return mServer.createMBean( pClassName, pName, pLoaderName );
   }

   public ObjectInstance createMBean(String pClassName,
                                     ObjectName pName,
                                     Object[] pParams,
                                     String[] pSignature)
      throws ReflectionException,
             InstanceAlreadyExistsException,
             MBeanRegistrationException,
             MBeanException,
             NotCompliantMBeanException,
             RemoteException
   {
      return mServer.createMBean( pClassName, pName, pParams, pSignature );
   }

   public ObjectInstance createMBean(String pClassName,
                                     ObjectName pName,
                                     ObjectName pLoaderName,
                                     Object[] pParams,
                                     String[] pSignature)
      throws ReflectionException,
             InstanceAlreadyExistsException,
             MBeanRegistrationException,
             MBeanException,
             NotCompliantMBeanException,
             InstanceNotFoundException,
             RemoteException
   {
      return mServer.createMBean( pClassName, pName, pLoaderName, pParams, pSignature );
   }

   public ObjectInstance registerMBean(Object object, ObjectName name) 
      throws InstanceAlreadyExistsException,
             MBeanRegistrationException,
             NotCompliantMBeanException,
             RemoteException
   {
      return mServer.registerMBean(object, name);
   }
   
   public void unregisterMBean(ObjectName pName)
      throws InstanceNotFoundException,
             MBeanRegistrationException,
             RemoteException
   {
      mServer.unregisterMBean( pName );
   }

   public ObjectInstance getObjectInstance(ObjectName pName)
      throws InstanceNotFoundException,
             RemoteException
   {
      return mServer.getObjectInstance( pName );
   }

   public Set queryMBeans(ObjectName pName, QueryExp pQuery)
      throws RemoteException
   {
      return mServer.queryMBeans( pName, pQuery );
   }

   public Set queryNames(ObjectName pName, QueryExp pQuery)
      throws RemoteException
   {
      return mServer.queryNames( pName, pQuery );
   }

   public boolean isRegistered(ObjectName pName)
      throws RemoteException
   {
      return mServer.isRegistered( pName );
   }

   public boolean isInstanceOf(ObjectName pName, String pClassName)
      throws InstanceNotFoundException,
             RemoteException
   {
      return mServer.isInstanceOf( pName, pClassName );
   }

   public Integer getMBeanCount() throws RemoteException
   {
      return mServer.getMBeanCount();
   }

   public Object getAttribute(ObjectName pName, String pAttribute)
      throws MBeanException,
             AttributeNotFoundException,
             InstanceNotFoundException,
             ReflectionException,
             RemoteException
   {
      return mServer.getAttribute( pName, pAttribute );
   }

   public AttributeList getAttributes(ObjectName pName, String[] pAttributes)
      throws InstanceNotFoundException,
             ReflectionException,
             RemoteException
   {
      return mServer.getAttributes( pName, pAttributes );
   }

   public void setAttribute(ObjectName pName, Attribute pAttribute) 
      throws InstanceNotFoundException,
             AttributeNotFoundException,
             InvalidAttributeValueException,
             MBeanException,
             ReflectionException,
             RemoteException
   {
      mServer.setAttribute( pName, pAttribute );
   }

   public AttributeList setAttributes(ObjectName pName, AttributeList pAttributes)
      throws InstanceNotFoundException,
             ReflectionException,
             RemoteException
   {
      return mServer.setAttributes( pName, pAttributes );
   }

   public Object invoke(ObjectName pName,
                        String pActionName,
                        Object[] pParams,
                        String[] pSignature)
      throws InstanceNotFoundException,
             MBeanException,
             ReflectionException,
             RemoteException
   {
      return mServer.invoke( pName, pActionName, pParams, pSignature );
   }

   public String getDefaultDomain() throws RemoteException
   {
      return mServer.getDefaultDomain();
   }

   public String[] getDomains() throws RemoteException
   {
      return mServer.getDomains();
   }
   public void addNotificationListener(ObjectName pName,
                                       ObjectName pListener,
                                       NotificationFilter pFilter,
                                       Object pHandback)
      throws InstanceNotFoundException,
             RemoteException
   {
      mServer.addNotificationListener(
         pName,
         pListener,
         pFilter,
         pHandback
         );
      mListeners.addElement( pListener );
   }

   public void removeNotificationListener(ObjectName pName,
                                          ObjectName pListener)
      throws InstanceNotFoundException,
             ListenerNotFoundException,
             RemoteException
   {
      mServer.removeNotificationListener(pName, pListener);
      mListeners.removeElement( pListener );
   }

   public void removeNotificationListener(ObjectName pName,
                                       ObjectName pListener,
                                       NotificationFilter pFilter,
                                       Object pHandback)
      throws InstanceNotFoundException,
             ListenerNotFoundException,
             RemoteException
   {
        throw new RuntimeException("NYI");
   }

   public MBeanInfo getMBeanInfo(ObjectName pName)
      throws InstanceNotFoundException,
             IntrospectionException,
             ReflectionException,
             RemoteException
   {
      return mServer.getMBeanInfo( pName );
   }   
}
