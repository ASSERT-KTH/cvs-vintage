/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jmx.server;

import java.io.File;
import java.net.URL;
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
import org.jboss.util.ServiceMBeanSupport;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class JMXAdaptorServer
   extends ServiceMBeanSupport
   implements JMXAdaptorServerMBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	MBeanServer server;
	JMXAdaptorImpl adaptor;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
		this.server = server;
      return new ObjectName(OBJECT_NAME);
   }
   
   public String getName()
   {
      return "JMX RMI Adaptor";
	}
   
   public void startService()
      throws Exception
   {
		adaptor = new JMXAdaptorImpl(server);
		new InitialContext().bind("jmx", adaptor);
   }
   
   public void stopService()
   {
		try
		{
			adaptor = null;
			new InitialContext().unbind("jmx");
		} catch (Exception e)
		{
			System.err.println(e);
		}
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

