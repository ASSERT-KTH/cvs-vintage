/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.interfaces;

import java.util.Set;
import java.util.Collection;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

/**
 * The remote interface of a JMX adapter.
 *      
 * @author Unknown
 * @version $Revision: 1.3 $
 */
public interface JMXAdaptor
   extends Remote
{
   /**
    * Invokes an operation on an MBean.
    *
    * @param name          The object name of the MBean on which the method
    *                      is to be invoked.
    * @param actionName    The name of the operation to be
    * @param params        An array containing the parameters to be set when
    *                      the operation is invoked.
    * @param signature     An array containing the signature of the operation.
    * @return              The object returned by the operation, which
    *                      represents the result ofinvoking the operation on
    *                      the MBean specified.
    *
    * @throws InstanceNotFoundException
    * @throws MBeanException
    * @throws ReflectionException
    * @throws RemoteException
    */
   Object invoke(ObjectName name,
                 String actionName,
                 Object[] params,
                 String[] signature)
      throws InstanceNotFoundException, MBeanException, ReflectionException,
             RemoteException;

   /**
    * Gets MBeans controlled by the MBean server.
    *
    * @param name     The object name pattern identifying the MBeans to be
    *                 retrieved. If null or no domain and key properties are
    *                 specified, all the MBeans registered will be retrieved.
    * @param query    The query expression to be applied for selecting MBeans.
    *                 If null no query expression will be applied for selecting
    *                 MBeans.
    * @return         A set containing the ObjectInstance objects for the
    *                 selected MBeans. If no MBean satisfies the query an
    *                 empty list is returned.
    *
    * @throws RemoteException
    */
   Set queryMBeans(ObjectName name, QueryExp query) throws RemoteException;

   /**
    * ???
    *
    * @return    ???
    *
    * @throws RemoteException
    */
   Collection getMBeanInfos() throws RemoteException;

   /**
    * Sets the value of a specific attribute of a named MBean.
    *
    * @param name         The name of the MBean within which the attribute is
    *                     to be set.
    * @param attribute    The identification of the attribute to be set and
    *                      the value it is to be set to.
    *
    * @throws InstanceNotFoundException
    * @throws AttributeNotFoundException
    * @throws InvalidAttributeValueException
    * @throws MBeanException
    * @throws ReflectionException
    * @throws RemoteException
    */
   void setAttribute(ObjectName name, Attribute attribute)
      throws InstanceNotFoundException, AttributeNotFoundException,
             InvalidAttributeValueException, MBeanException,
             ReflectionException, RemoteException;

   /**
    * Gets the value of a specific attribute of a named MBean.
    *
    * @param name         The object name of the MBean from which the
    *                     attribute is to be retrieved.
    * @param attribute    A String specifying the name of the attribute to be
    *                     retrieved.
    * 
    * @throws InstanceNotFoundException
    * @throws AttributeNotFoundException
    * @throws InvalidAttributeValueException
    * @throws MBeanException
    * @throws ReflectionException
    * @throws RemoteException
    */
   Object getAttribute(ObjectName name, String attribute)
      throws MBeanException, AttributeNotFoundException,
             InstanceNotFoundException, ReflectionException,
             RemoteException;
}
