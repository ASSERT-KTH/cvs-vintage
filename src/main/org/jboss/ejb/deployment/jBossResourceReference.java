/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
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
 *   @version $Revision: 1.2 $
 */
public class jBossResourceReference
   extends com.dreambean.ejx.ejb.ResourceReference
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String resourceName = "";

   Customizer c;
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setResourceName(String n) { resourceName = n; }
   public String getResourceName() { return resourceName; }

   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   {
      Element entity = doc.createElement("resource-ref");
      XMLManager.addElement(entity,"res-ref-name",getName());
      
      XMLManager.addElement(entity,"resource-name",getResourceName());
      
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
            
            if (name.equals("resource-name"))
            {
               setResourceName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 
         }
      } else // EJB-JAR XML
      {
         super.importXml(elt);
			setResourceName(getName());
      }
   }
   
   public String toString()
   {
      return (getName().equals("")) ? "Resource reference" : getName();
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
