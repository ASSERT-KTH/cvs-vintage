/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector;

import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
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


/**
* Client-Side JMX Connector Interface.
* <BR>
* <B>Attention:<B>
* <BR>
* Please note that this interface has two purposes. First it
* adds additional runtime exception to the methods because
* of the nature of the remote connection. It also declares
* the unusable methods throwing the UnsupportedOperationException
* which is thrown always by the Connector implementation.
* The second purpose is to have interface to discuss it.
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface JMXConnector
//AS	extends MBeanServer
{

	// Constants -----------------------------------------------------
	
	// Public --------------------------------------------------------

	/**
	* Instantiate the given class on the remote MBeanServer and returns a Object 
	* Handler you can use to register it as a MBean with {@link #registerMBean
	* registerMBean()} or as a parameter to createMBean() or instantiate()
	* method which takes it as a parameter.
	*
	* @param pClassName						Class name of the class to be loaded 
	*										and instantiated
	*
	* @return								Object handler. Please use this handler
	*										to register it as MBean or as a parameter
	*										in the other methods as a parameter. The
	*										server-side connector will look up for
	*										an object handler parameter and then
	*										replace the object handler by the
	*										effective object.
	**/
	public Object instantiate(
		String pClassName
	) throws
		ReflectionException,
		MBeanException;

	public Object instantiate(
		String pClassName,
		ObjectName pLoaderName
	) throws
		ReflectionException,
		MBeanException,
		InstanceNotFoundException;

	/**
	* Instantiate the given class on the remote MBeanServer and
	* returns a Object Handler you can use to register it as a
	* MBean with {@link #registerMBean registerMBean()}
	*
	* @param pClassName						Class name of the class to be loaded 
	*										and instantiated
	* @param pParams						Array of parameter passed to the creator
	*										of the class. If one is of data type
	*										Object handler it will be replaced on
	*										the server-side by its effective
	*										object.
	* @param pSignature						Array of Class Names (full qualified)
	*										to find the right parameter. When there
	*										is an ObjectHandler as a parameter type
	*										then it will be replaced on the server-
	*										side by the class name of the effective
	*										object) otherwise it will be kept.
	*
	* @return								Object handler. Please use this handler
	*										to register it as MBean or as a parameter
	*										in the other methods as a parameter. The
	*										server-side connector will look up for
	*										an object handler parameter and then
	*										replace the object handler by the
	*										effective object.
	**/
	public Object instantiate(
		String pClassName,
		Object[] pParams,
		String[] pSignature
	) throws
		ReflectionException,
		MBeanException;

	public Object instantiate(
		String pClassName,
		ObjectName pLoaderName,
		Object[] pParams,
		String[] pSignature
	) throws
		ReflectionException,
		MBeanException,
		InstanceNotFoundException;

	public ObjectInstance createMBean(
		String pClassName,
		ObjectName pName
	) throws
		ReflectionException,
		InstanceAlreadyExistsException,
		MBeanRegistrationException,
		MBeanException,
		NotCompliantMBeanException;

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
		InstanceNotFoundException;

	/**
	* Instantiates the given class and registers it on the remote MBeanServer and
	* returns an Object Instance of the MBean.
	*
	* @param pClassName						Class name of the class to be loaded 
	*										and instantiated
	* @param pNameToAssign					Object Name the new MBean should be
	*										assigned to
	* @param pParams						Array of parameter passed to the creator
	*										of the class. If one is of data type
	*										Object handler it will be replaced on
	*										the server-side by its effective
	*										object.
	* @param pSignature						Array of Class Names (full qualified)
	*										to find the right parameter. When there
	*										is an ObjectHandler as a parameter type
	*										then it will be replaced on the server-
	*										side by the class name of the effective
	*										object) otherwise it will be kept.
	*
	* @return								Object Instance of the new MBean
	**/
	public ObjectInstance createMBean(
		String pClassName,
		ObjectName pNameToAssign,
		Object[] pParams,
		String[] pSignature
	) throws
		ReflectionException,
		InstanceAlreadyExistsException,
		MBeanRegistrationException,
		MBeanException,
		NotCompliantMBeanException;

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
		InstanceNotFoundException;

	/**
	* Register the given Object (already instantiated) as a MBean on the
	* remote MBeanServer
	*
	* @param pObjectHandler					Object Handler of th given object
	*										to register as MBean
	* @param pNaemToAssign					Object Name to MBean is assigned to
	*
	* @return								Object Instance of the new MBean
	**/
	public ObjectInstance registerMBean(
		Object pObjectHandler,
		ObjectName pNaemToAssign
	) throws
		InstanceAlreadyExistsException,
		MBeanRegistrationException,
		NotCompliantMBeanException;

	public void unregisterMBean(
		ObjectName pName
	) throws
		InstanceNotFoundException,
		MBeanRegistrationException;

	public ObjectInstance getObjectInstance(
		ObjectName pName
	) throws
		InstanceNotFoundException;

	public Set queryMBeans(
		ObjectName pName,
		QueryExp pQuery
	);

	public Set queryNames(
		ObjectName pName,
		QueryExp pQuery
	);

	public boolean isRegistered(
		ObjectName pName
	);
    
	public boolean isInstanceOf(
		ObjectName pName,
  	String pClassName
	) throws
		InstanceNotFoundException;

	public Integer getMBeanCount(
	);

	public Object getAttribute(
		ObjectName pName,
		String pAttribute
	) throws
		MBeanException,
		AttributeNotFoundException,
		InstanceNotFoundException,
		ReflectionException;

	public AttributeList getAttributes(
		ObjectName pName,
		String[] pAttributes
	) throws
		InstanceNotFoundException,
		ReflectionException;

	public void setAttribute(
		ObjectName pName,
		Attribute pAttribute
	) throws
		InstanceNotFoundException,
		AttributeNotFoundException,
		InvalidAttributeValueException,
		MBeanException,
		ReflectionException;

	public AttributeList setAttributes(
		ObjectName pName,
		AttributeList pAttributes
	) throws
		InstanceNotFoundException,
		ReflectionException;

	public Object invoke(
		ObjectName pName,
		String pActionName,
		Object[] pParams,
		String[] pSignature
	) throws
		InstanceNotFoundException,
		MBeanException,
		ReflectionException;

	public String getDefaultDomain(
	);

	/**
	* Adds the given Notification Listener in a way that
	* the Notification Events are send back to the listener
	* <BR>
	* Please asume that the listening is terminated when
	* the instance of these interface goes down.
	*
	* @param pBroadcasterName		Name of the Broadcaster MBean on
	*						the remote side
	* @param pListener			Local notification listener
	* @param pFilter				In general there are three ways this
	*						could work:
	*						1) A copy of the filter is send to the
	*						server but then the Filter cannot interact
	*						with the client. This is default.
	*						2) Wrapper around the filter therefore
	*						the server sends the filter call to the
	*						client to be performed. This filter must be
	*						a subclass of RemoteNotificationListener.
	*						3) All Notification events are sent to the
	*						client and the client performs the filtering.
	*						This filter must be a subclass of
	*						LocalNotificationListener.
	* @param pHandback			Object to be send back to the listener. To
	*						make it complete transparent to the client
	*						an Object Handler is sent to the Server and
	*						and when a Notification comes back it will
	*						be looked up and send to the client. Therefore
	*						it must not be serializable.
	**/
	public void addNotificationListener(
		ObjectName pName,
		NotificationListener pListener,
		NotificationFilter pFilter,
		Object pHandback		
	) throws
		InstanceNotFoundException;

	/**
	* Remoes the given Notification Listener in a way that
	* all involved instances are removed and the remote
	* listener is removed from the broadcaster.
	* <BR>
	* Please asume that the listening is terminated when
	* the instance of these interface goes down.
	*
	* @param pBroadcasterName		Name of the Broadcaster MBean on
	*						the remote side
	* @param pListener			Local notification listener
	**/
	public void removeNotificationListener(
		ObjectName pName,
		NotificationListener pListener
	) throws
		InstanceNotFoundException,
		ListenerNotFoundException;

	public MBeanInfo getMBeanInfo(
		ObjectName pName
	) throws
		InstanceNotFoundException,
		IntrospectionException,
		ReflectionException;

	// Unsupported Operations ----------------------------------------------
	/**
	* @throws UnsupportedOperationException
	*							The MBean related to the given object
	*							cannot be wrapped by the transport
	*							layer implementation and therefore it
	*							makes no sense (no callback could be
	*							instantiated) and therefore this exception
	*							is always thrown
	*/
	public void addNotificationListener(
		ObjectName pName,
		ObjectName pListener,
		NotificationFilter pFilter,
		Object pHandback		
	) throws
		InstanceNotFoundException,
		UnsupportedOperationException;
	/**
	* @throws UnsupportedOperationException
	*							The MBean related to the given object
	*							cannot be wrapped by the transport
	*							layer which prevents the remove of the
	*							the wrapper and therefore it
	*							makes no sense (no callback could be
	*							instantiated) and therefore this exception
	*							is always thrown
	*/
	public void removeNotificationListener(
		ObjectName pName,
		ObjectName pListener
	) throws
		InstanceNotFoundException,
		ListenerNotFoundException,
		UnsupportedOperationException;
	/**
	* @throws UnsupportedOperationException
	*							Return value is not serializable and
	*							the operation makes really no sense
	*							on a connector therefore this exception
	*							is always thrown
	*/
	public ObjectInputStream deserialize(
		ObjectName pName,
		byte[] pData
	) throws
		InstanceNotFoundException,
		OperationsException,
		UnsupportedOperationException;
	/**
	* @throws UnsupportedOperationException
	*							Return value is not serializable and
	*							the operation makes really no sense
	*							on a connector therefore this exception
	*							is always thrown
	*/
	public ObjectInputStream deserialize(
		String pClassName,
		byte[] pData
	) throws
		OperationsException,
		ReflectionException,
		UnsupportedOperationException;
	/**
	* @throws UnsupportedOperationException
	*							Return value is not serializable and
	*							the operation makes really no sense
	*							on a connector therefore this exception
	*							is always thrown
	*/
	public ObjectInputStream deserialize(
		String pClassName,
		ObjectName pLoaderName,
		byte[] pData
	) throws
		InstanceNotFoundException,
		OperationsException,
		ReflectionException,
		UnsupportedOperationException;
}
