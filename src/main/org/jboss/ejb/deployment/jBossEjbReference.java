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

import org.w3c.dom.*;

import com.dreambean.awt.*;
import com.dreambean.ejx.xml.*;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.3 $
 */
public class jBossEjbReference
   extends com.dreambean.ejx.ejb.EjbReference
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String jndiName = "";

   Customizer c;
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setJndiName(String n) { jndiName = n; }
   public String getJndiName() { return jndiName; }

   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   {
      Element entity = doc.createElement("ejb-ref");
      XMLManager.addElement(entity,"ejb-ref-name",getName());
      
      XMLManager.addElement(entity,"jndi-name",getJndiName());
      
      return entity;
   }
   
   public void importXml(Element elt)
   {
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals("jboss"))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("jndi-name"))
            {
               setJndiName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 
         }
      } else // EJB-JAR XML
      {
         super.importXml(elt);
      }
   }
   
   public String toString()
   {
      return (getName().equals("")) ? "EJB reference" : getName();
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
