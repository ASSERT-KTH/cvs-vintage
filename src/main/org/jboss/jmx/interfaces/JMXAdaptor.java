/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.interfaces;

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
 *   <description> 
 *      
 *   @see <related>
 *   @author <firstname> <lastname> (<email>)
 *   @version $Revision: 1.1 $
 */
public interface JMXAdaptor
   extends Remote
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------
	public java.lang.Object invoke(ObjectName name,
   	                            java.lang.String actionName,
      	                         java.lang.Object[] params,
         	                      java.lang.String[] signature)
		throws InstanceNotFoundException,
             MBeanException,
             ReflectionException,
				 RemoteException;

	public java.util.Set queryMBeans(ObjectName name,
                                 	QueryExp query)
		throws RemoteException;
	
	public java.util.Collection getMBeanInfos()
		throws RemoteException;													 
	
	public void setAttribute(ObjectName name,
                         Attribute attribute)
      throws InstanceNotFoundException,
             AttributeNotFoundException,
             InvalidAttributeValueException,
             MBeanException,
             ReflectionException,
				 RemoteException;
				 
	public java.lang.Object getAttribute(ObjectName name,
                                     java.lang.String attribute)
	   throws MBeanException,
	          AttributeNotFoundException,
	          InstanceNotFoundException,
	          ReflectionException,
				 RemoteException;
}
