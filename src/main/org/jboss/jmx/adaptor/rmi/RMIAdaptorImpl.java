/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.adaptor.rmi;

import java.io.ObjectInputStream;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

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

import javax.naming.InitialContext;

import org.jboss.logging.Logger;

import org.jboss.jmx.connector.notification.JMSNotificationListener;
import org.jboss.jmx.connector.notification.RMINotificationListener;

/**
 * RMI Interface for the server side Connector which
 * is nearly the same as the MBeanServer Interface but
 * has an additional RemoteException.
 *
 * @todo implement notifications
 * @version <tt>$Revision: 1.4 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 **/
public class RMIAdaptorImpl
   extends UnicastRemoteObject
   implements RMIAdaptor
{
   protected Logger log = Logger.getLogger(this.getClass());
   
   /**
    * Reference to the MBeanServer all the methods of this Connector are
    * forwarded to
    **/
   protected MBeanServer mServer;

   /** Pool of registered listeners **/
   protected Vector mListeners = new Vector();

   public RMIAdaptorImpl(MBeanServer pServer) throws RemoteException
   {
      mServer = pServer;
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
                                          NotificationFilter filter,
                                          Object handback)
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

