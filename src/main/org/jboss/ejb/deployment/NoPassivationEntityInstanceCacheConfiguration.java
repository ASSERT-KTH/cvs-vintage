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
public class NoPassivationEntityInstanceCacheConfiguration
   implements XmlExternalizable, BeanContextChildComponentProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   int minSize = 10;
   int maxSize = 100;
   
   GenericCustomizer customizer;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setMinimumSize(int minSize) { this.minSize = minSize; }
   public int getMinimumSize() { return minSize; }
   
   public void setMaximumSize(int maxSize) { this.maxSize = maxSize; }
   public int getMaximumSize() { return maxSize; }
   
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
      Element cacheconf = doc.createElement("container-cache-conf");
      
      XMLManager.addElement(cacheconf,"min-size",getMinimumSize()+"");
      XMLManager.addElement(cacheconf,"max-size",getMaximumSize()+"");
      
      return cacheconf;
   }
   
   public void importXml(Element elt)
   {
      NodeList nl = elt.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         String name = n.getNodeName();
         
         if (name.equals("min-size"))
         {
            setMinimumSize(new Integer(n.hasChildNodes() ? XMLManager.getString(n) : "").intValue());
         } else if (name.equals("max-size"))
         {
            setMaximumSize(new Integer(n.hasChildNodes() ? XMLManager.getString(n) : "").intValue());
         } 

      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

