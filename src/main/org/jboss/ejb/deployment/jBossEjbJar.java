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
import javax.swing.*;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.GenericCustomizer;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.5 $
 */
public class jBossEjbJar
   extends com.dreambean.ejx.ejb.EjbJar
{
   // Constants -----------------------------------------------------
   public static final String JBOSS_DOCUMENT="jboss";
    
   // Attributes ----------------------------------------------------
   ResourceManagers rm;
   ContainerConfigurations cc;
	boolean secure;
	
	Customizer c;	

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public jBossEjbJar()
   {
      super();
      
      rm = new ResourceManagers();
      add(rm);
      
      cc = new ContainerConfigurations();
      add(cc);
   }
   
   // Public --------------------------------------------------------
   public void setSecure(boolean s) { secure = s; }
   public boolean isSecure() { return secure; }
   
   public ResourceManagers getResourceManagers() { return rm; }
   public ContainerConfigurations getContainerConfigurations() { return cc; }
   
   // BeanContextChildComponentProxy implementation -----------------
   public java.awt.Component getComponent()
   {
      if (c == null)
      {
         c = new jBossEjbJarViewer();
         c.setObject(this);
      }
   	
      return (java.awt.Component)c;
   }
	
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element ejbjar = doc.createElement("jboss");

      XMLManager.addElement(ejbjar,"secure",new Boolean(isSecure()).toString());
		
      ejbjar.appendChild(cc.exportXml(doc));
      ejbjar.appendChild(rm.exportXml(doc));
      ejbjar.appendChild(eb.exportXml(doc));
      
      return ejbjar;
   }
   
   public void importXml(Element elt)
   	throws Exception
   {
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(JBOSS_DOCUMENT))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("enterprise-beans"))
            {
               eb.importXml((Element)n);
            } else if (name.equals("resource-managers"))
            {
               rm.importXml((Element)n);
            } else if (name.equals("container-configurations"))
            {
               cc.importXml((Element)n);
            } else if (name.equals("secure"))
            {
               setSecure(new Boolean(XMLManager.getString(n)).booleanValue());
            } 
         }
      } else
      {
         super.importXml(elt);
			remove(ad);
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected void createEnterpriseBeans()
   {
      eb = new jBossEnterpriseBeans();
      add(eb);
   }
   
   protected void createAssemblyDescriptor()
   {
      ad = new com.dreambean.ejx.ejb.AssemblyDescriptor();
      add(ad);
   }
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
