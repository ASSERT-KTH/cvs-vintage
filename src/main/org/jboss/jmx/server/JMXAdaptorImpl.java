/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jmx.server;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

import org.jboss.logging.Log;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.jmx.interfaces.JMXAdaptor;

/**
*   <description> 
*      
*   @see <related>
*   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*   @version $Revision: 1.6 $
*/
public class JMXAdaptorImpl
   extends UnicastRemoteObject
   implements JMXAdaptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	MBeanServer server;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public JMXAdaptorImpl(MBeanServer server)
		throws RemoteException
   {
		super();
		this.server = server;
   }
   
   // JMXAdaptor implementation -------------------------------------
   public java.lang.Object invoke(ObjectName name,
   	                            java.lang.String actionName,
      	                         java.lang.Object[] params,
         	                      java.lang.String[] signature)
   	throws InstanceNotFoundException,
             MBeanException,
             ReflectionException,
   			 RemoteException
	{
   	return server.invoke(name, actionName, params, signature);
   }

   public java.util.Set queryMBeans(ObjectName name,
                                 	QueryExp query)
   	throws RemoteException
	{
		return server.queryMBeans(name, query);
	}
   
   public java.util.Collection getMBeanInfos()
   	throws RemoteException
	{
		try
		{
			ArrayList infos = new ArrayList();
			Iterator mbeans = server.queryNames(null, null).iterator();
			while(mbeans.hasNext())
			{
				infos.add(server.getMBeanInfo((ObjectName)mbeans.next()));
			}
			return infos;
		} catch (Exception e)
		{
			throw new ServerException("Exception occurred", e);
		}
		
	}
   
   public void setAttribute(ObjectName name,
                         Attribute attribute)
      throws InstanceNotFoundException,
             AttributeNotFoundException,
             InvalidAttributeValueException,
             MBeanException,
             ReflectionException,
   			 RemoteException
	{
		server.setAttribute(name, attribute);
	}
   			 
   public java.lang.Object getAttribute(ObjectName name,
                                     java.lang.String attribute)
      throws MBeanException,
             AttributeNotFoundException,
             InstanceNotFoundException,
             ReflectionException,
   			 RemoteException
	{
		return server.getAttribute(name, attribute);
	}
	
   // Protected -----------------------------------------------------
}

