/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.awt.GenericCustomizer;
   
/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class JRMPContainerInvokerConfiguration
   implements XmlExternalizable, BeanContextChildComponentProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   boolean optimize = false;
   
   GenericCustomizer customizer;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
   }
   
   public boolean isOptimized()
   {
      return optimize;
   }
   
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (customizer == null)
          customizer = new GenericCustomizer(this);
      return (Component)customizer;
   }

   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   {
      Element coninvconf = doc.createElement("container-invoker-conf");
      
      XMLManager.addElement(coninvconf,"optimized",isOptimized()+"");
      
      return coninvconf;
   }
   
   public void importXml(Element elt)
   {
      NodeList nl = elt.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         String name = n.getNodeName();
         
         if (name.equals("optimized"))
         {
            setOptimized(new Boolean(n.hasChildNodes() ? XMLManager.getString(n) : "").booleanValue());
         } 
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

