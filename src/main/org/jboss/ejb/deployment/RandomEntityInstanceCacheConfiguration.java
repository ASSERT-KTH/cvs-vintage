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
public class RandomEntityInstanceCacheConfiguration
   extends NoPassivationEntityInstanceCacheConfiguration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   int passivationInterval = 20;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setPassivationInterval(int pi) { this.passivationInterval = pi; }
   public int getPassivationInterval() { return passivationInterval; }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   {
      Element cacheconf = super.exportXml(doc);
      
      XMLManager.addElement(cacheconf,"passivation-interval",getPassivationInterval()+"");
      
      return cacheconf;
   }
   
   public void importXml(Element elt)
   {
      super.importXml(elt);
      
      NodeList nl = elt.getChildNodes();
      for (int i = 0; i < nl.getLength(); i++)
      {
         Node n = nl.item(i);
         String name = n.getNodeName();
         
         if (name.equals("passivation-interval"))
         {
            setPassivationInterval(new Integer(n.hasChildNodes() ? XMLManager.getString(n) : "").intValue());
         } 
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

