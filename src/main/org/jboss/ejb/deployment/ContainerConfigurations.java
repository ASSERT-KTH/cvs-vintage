/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.BeanContextViewer;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   ContainerConfigurations holds the container configurations from a jar
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:marc.fleury@telkel.com>Marc Fleury</a>
 *   @version $Revision: 1.7 $
 */
public class ContainerConfigurations
   extends BeanContextSupport
   implements BeanContextChildComponentProxy, XmlExternalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Customizer c;
    
   // Static --------------------------------------------------------
   
   /*
   * For default purposes we provide methods to get access to the conf
   * Note that because the ContainerConfigurations work from the ThreadContext
   * we provide a call instead of a static method to access these guys.
   *
   */
   public static StatelessSessionContainerConfiguration getDefaultStatelessBeanContainerConfiguration() {
	   
	    StatelessSessionContainerConfiguration configuration = new StatelessSessionContainerConfiguration();
	    configuration.setCallLogging(false);
		configuration.setContainerInvoker("org.jboss.ejb.plugins.jrmp13.server.JRMPContainerInvoker");
		configuration.setInstancePool("org.jboss.ejb.plugins.StatelessSessionInstancePool");
		configuration.setName("Default StatelessBean");
		configuration.setTransactionManager("org.jboss.tm.TxManager");
		
		return configuration;
   }
   
   /*   MF FIXME Not there yet.... soon
   
    public static ContainerConfiguration getDefaultStatefulBeanContainerConfiguration() {
	   
	    ContainerConfiguration configuration = new StatefulSessionContainerConfiguration();
	    configuration.setCallLogging(false);
		configuration.setContainerInvoker("org.jboss.ejb.plugins.jrmp13.server.JRMPContainerInvoker");
		configuration.setInstancePool("org.jboss.ejb.plugins.StatefulSessionInstancePool");
		configuration.setInstanceCache("org.jboss.ejb.plugins.NoPassivationStatefulSessionInstanceCache");
		configuration.setName("Default StatefulBean");
		configuration.setTransactionManager("org.jboss.tm.TxManager");
		
		return configuration;
   }
   */
   
   public static EntityContainerConfiguration getDefaultEntityBeanContainerConfiguration(String type) {
	   
	    EntityContainerConfiguration configuration = new EntityContainerConfiguration();
	    configuration.setCallLogging(false);
		configuration.setCommitOption("A");
		configuration.setContainerInvoker("org.jboss.ejb.plugins.jrmp13.server.JRMPContainerInvoker");
		configuration.setInstancePool("org.jboss.ejb.plugins.EntityInstancePool");
		configuration.setInstanceCache("org.jboss.ejb.plugins.NoPassivationEntityInstanceCache");
		if (type.equalsIgnoreCase("BMP"))
		    configuration.setPersistenceManager("org.jboss.ejb.plugins.BMPPersistenceManager");
		else 
			configuration.setPersistenceManager("org.jboss.ejb.plugins.jaws.JAWSPersistenceManager");
		configuration.setName("Default EntityBean");
		configuration.setTransactionManager("org.jboss.tm.TxManager");
		
		return configuration;
   }

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void createContainerConfiguration(String name, String clazz)
      throws IOException, ClassNotFoundException
   {
      
      ((ContainerConfiguration)instantiateChild(clazz)).setName(name);
   }
   
   public ContainerConfiguration addContainerConfiguration(String clazz)
      throws IOException, ClassNotFoundException
   {
      return (ContainerConfiguration)instantiateChild(clazz);
   }   
   
   public ContainerConfiguration getContainerConfiguration(String name)
   {
      
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         ContainerConfiguration cc = (ContainerConfiguration)enum.next();
         if (cc.getName().equals(name))
            return cc;
      }
      return null;
   }
   
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (c == null)
      {
			c = new BeanContextViewer();
			c.setObject(this);
		}
			
      return (Component)c;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
      throws Exception
   {
      Element containerconfigurations = doc.createElement("container-configurations");
      
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         containerconfigurations.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
      
      return containerconfigurations;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(jBossEjbJar.JBOSS_DOCUMENT))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("container-configuration"))
            {
               String confType = ((Element)n).getAttribute("configuration-class");
               
               XmlExternalizable conConf = (XmlExternalizable)addContainerConfiguration(confType);
               
               conConf.importXml((Element)n);
            } 
         }
      } 
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
