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
import javax.management.MBeanServer;
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
public interface RemoteMBeanServer {

	// Constants -----------------------------------------------------
	
   /**
   * If this type is used and you specify a valid QueueConnectorFactory
   * then this connector will use JMS to transfer the events asynchronous
   * back from the server to the client.
   **/
   public static final int NOTIFICATION_TYPE_JMS = 0;
   /**
   * If this type is used the Connector will use RMI Callback Objects to
   * transfer the events back from the server synchronously.
   **/
   public static final int NOTIFICATION_TYPE_RMI = 1;
   /**
   * If this type is used the Connector will use Notification Polling to
   * transfer the events back from the server synchronously.
   **/
   public static final int NOTIFICATION_TYPE_POLLING = 2;
   
	// Public --------------------------------------------------------
   
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
   
	public MBeanInfo getMBeanInfo(
		ObjectName pName
	) throws
		InstanceNotFoundException,
		IntrospectionException,
		ReflectionException;
   
	public void addNotificationListener(
		ObjectName pName,
		NotificationListener pListener,
		NotificationFilter pFilter,
		Object pHandback		
	) throws
		InstanceNotFoundException;
   
	public void removeNotificationListener(
		ObjectName pName,
		NotificationListener pListener
	) throws
		InstanceNotFoundException,
		ListenerNotFoundException;
   
   public void addNotificationListener(
		ObjectName pName,
		ObjectName pListener,
		NotificationFilter pFilter,
		Object pHandback		
	) throws
		InstanceNotFoundException;
   
	public void removeNotificationListener(
		ObjectName pName,
		ObjectName pListener
	) throws
		InstanceNotFoundException,
		ListenerNotFoundException,
		UnsupportedOperationException;
   
}
