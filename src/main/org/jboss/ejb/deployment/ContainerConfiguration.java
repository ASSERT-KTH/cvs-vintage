/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.util.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.10 $
 */
public abstract class ContainerConfiguration
   extends BeanContextServicesSupport
   implements BeanContextChildComponentProxy, XmlExternalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name= "";
   String type= "";
   boolean callLogging = false;
   
   String containerInvoker= "";
   String instancePool= "";
   String instanceCache= "";
   String persistenceManager= "";
   String transactionManager= "";
   String authenticationModule = "";
   String roleMappingManager = "";
   
   Object containerInvokerConfiguration;
   Object instancePoolConfiguration;
   Object instanceCacheConfiguration;
   Object transactionManagerConfiguration;
   
   ArrayList interceptors = new ArrayList();
   
   Component c;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setName(String n) { name = n; }
   public String getName() { return name; }
   
   public void setType(String t) { type = t; }
   public String getType() { return type; }
   
   public void setContainerInvoker(String ci) 
   { 
      containerInvoker = ci; 
      
      if (containerInvokerConfiguration != null)
      {
         remove(containerInvokerConfiguration);
         containerInvokerConfiguration = null;
      }
      
      try
      {
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(getConfigurationClassName(ci));
         Object obj = clazz.newInstance();
         if (obj instanceof BeanContextChildComponentProxy ||
             obj instanceof BeanContextContainerProxy)
         {
            containerInvokerConfiguration = obj;
            add(containerInvokerConfiguration);
         }
      } catch (Throwable e)
      {
         System.out.println(e);
      }
   }
   
   public String getContainerInvoker() { return containerInvoker; }
   public Object getContainerInvokerConfiguration() { return containerInvokerConfiguration; }
   
   public void setInstancePool(String ip) 
   { 
      instancePool = ip; 
      
      if (instancePoolConfiguration != null)
      {
         remove(instancePoolConfiguration);
         instancePoolConfiguration = null;
      }
      
      try
      {
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(getConfigurationClassName(ip));
         Object obj = clazz.newInstance();
         if (obj instanceof BeanContextChildComponentProxy ||
             obj instanceof BeanContextContainerProxy)
         {
            instancePoolConfiguration = obj;
            add(instancePoolConfiguration);
         }
      } catch (Throwable e)
      {
//         Logger.log(e);
      }
   }
   public String getInstancePool() { return instancePool; }
   public Object getInstancePoolConfiguration() { return instancePoolConfiguration; }

   public void setInstanceCache(String ic) 
   { 
      instanceCache = ic;
      
      if (instanceCacheConfiguration != null)
      {
         remove(instanceCacheConfiguration);
         instanceCacheConfiguration = null;
      }
      
      try
      {
         Class clazz = Thread.currentThread().getContextClassLoader().loadClass(getConfigurationClassName(ic));
         Object obj = clazz.newInstance();
         if (obj instanceof BeanContextChildComponentProxy ||
             obj instanceof BeanContextContainerProxy)
         {
            instanceCacheConfiguration = obj;
            add(instanceCacheConfiguration);
         }
      } catch (Throwable e)
      {
//         Logger.log(e);
      }
   }
   
   public String getInstanceCache() { return instanceCache; }
   public Object getInstanceCacheConfiguration() { return instanceCacheConfiguration; }

   public void setPersistenceManager(String pm) { persistenceManager = pm; }
   public String getPersistenceManager() { return persistenceManager; }
   
   public void setTransactionManager(String tm) { transactionManager = tm; }
   public String getTransactionManager() { return transactionManager; }

   public void setAuthenticationModule(String am) { authenticationModule = am; }
   public String getAuthenticationModule() { return authenticationModule; }

   public void setRoleMappingManager(String rm) { roleMappingManager = rm; }
   public String getRoleMappingManager() { return roleMappingManager; }

   public void setCallLogging(boolean cl) { callLogging = cl; }
   public boolean getCallLogging() { return callLogging; }
   
   public String toString()
   {
      return name.equals("") ? "Container configuration" : name;
   }
   
   public ContainerConfigurations getContainerConfigurations()
   {
      return (ContainerConfigurations)getBeanContext();
   }
   
	public void removeConfiguration()
	{
		getContainerConfigurations().remove(this);
	}
	
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (c == null)
          c = new com.dreambean.awt.GenericCustomizer(false, this);
      return c;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element containerconfiguration = doc.createElement("container-configuration");
      XMLManager.addAttribute(containerconfiguration,"configuration-class",getClass().getName());
      XMLManager.addElement(containerconfiguration,"container-name",getName());
      XMLManager.addElement(containerconfiguration,"call-logging",new Boolean(callLogging).toString());
      XMLManager.addElement(containerconfiguration,"container-invoker",getContainerInvoker());
      XMLManager.addElement(containerconfiguration,"instance-pool",getInstancePool());
      XMLManager.addElement(containerconfiguration,"instance-cache",getInstanceCache());
      XMLManager.addElement(containerconfiguration,"persistence-manager",getPersistenceManager());
      XMLManager.addElement(containerconfiguration,"transaction-manager",getTransactionManager());
      XMLManager.addElement(containerconfiguration,"authentication-module",getAuthenticationModule());
      XMLManager.addElement(containerconfiguration,"role-mapping-manager",getRoleMappingManager());

      if (containerInvokerConfiguration != null)
		{
			if (containerInvokerConfiguration instanceof XmlExternalizable)
			{
				containerconfiguration.appendChild(((XmlExternalizable)containerInvokerConfiguration).exportXml(doc));
			}
			else
			{
				containerconfiguration.appendChild(XMLManager.exportBean(doc, containerInvokerConfiguration, "container-invoker-conf"));	
			}
		}
      
      if (instanceCacheConfiguration != null)
 	   {
		   if (instanceCacheConfiguration instanceof XmlExternalizable)
		   {
		   	containerconfiguration.appendChild(((XmlExternalizable)instanceCacheConfiguration).exportXml(doc));
		   }
		   else
		   {
		   	containerconfiguration.appendChild(XMLManager.exportBean(doc, instanceCacheConfiguration, "container-cache-conf"));	
		   }
	   }

      if (instancePoolConfiguration != null)
      {
         if (instancePoolConfiguration instanceof XmlExternalizable)
         {
         	containerconfiguration.appendChild(((XmlExternalizable)instancePoolConfiguration).exportXml(doc));
         }
         else
         {
         	containerconfiguration.appendChild(XMLManager.exportBean(doc, instancePoolConfiguration, "container-pool-conf"));	
         }
      }
         
      return containerconfiguration;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
   	if (elt.getOwnerDocument().getDocumentElement().getTagName().equals("jboss"))
   	{
	      NodeList nl = elt.getChildNodes();
	      for (int i = 0; i < nl.getLength(); i++)
	      {
	         Node n = nl.item(i);
	         String name = n.getNodeName();
	         
	         if (name.equals("container-name"))
            {
               setName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("call-logging"))
	         {
	            setCallLogging(new Boolean(XMLManager.getString(n)).booleanValue());
	         } else if (name.equals("container-invoker"))
	         {
	            setContainerInvoker(n.hasChildNodes() ? XMLManager.getString(n) : "");
	         } else if (name.equals("instance-pool"))
            {
               setInstancePool(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("instance-cache"))
            {
               setInstanceCache(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("persistence-manager"))
            {
               setPersistenceManager(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("transaction-manager"))
            {
               setTransactionManager(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("authentication-module"))
            {
               setAuthenticationModule(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("role-mapping-manager"))
            {
               setRoleMappingManager(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("container-invoker-conf"))
            {
					if (containerInvokerConfiguration instanceof XmlExternalizable)
	               ((XmlExternalizable)containerInvokerConfiguration).importXml((Element)n);
					else
						XMLManager.importBean((Element)n, containerInvokerConfiguration);
            } else if (name.equals("container-cache-conf"))
            {
               if (instanceCacheConfiguration instanceof XmlExternalizable)
                  ((XmlExternalizable)instanceCacheConfiguration).importXml((Element)n);
               else
               	XMLManager.importBean((Element)n, instanceCacheConfiguration);
            } else if (name.equals("container-pool-conf"))
            {
               if (instancePoolConfiguration instanceof XmlExternalizable)
                  ((XmlExternalizable)instancePoolConfiguration).importXml((Element)n);
               else
               	XMLManager.importBean((Element)n, instancePoolConfiguration);
            }
	      }
   	}
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected String getConfigurationClassName(String name)
   {
      name = name.substring(name.lastIndexOf(".")+1);
      return "org.jboss.ejb.deployment."+name+"Configuration";
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
