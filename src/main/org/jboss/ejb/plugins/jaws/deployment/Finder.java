/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws.deployment;

import java.awt.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.util.*;

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
 *   @version $Revision: 1.4 $
 */
public class Finder
   extends BeanContextServicesSupport
   implements XmlExternalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name= "";
   String query= "";
   String order= "";
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setName(String n) { name = n; }
   public String getName() { return name; }
   
   public void setQuery(String q) { query = q; }
   public String getQuery() { return query; }
   
   public void setOrder(String o) { order = o; }
   public String getOrder() { return order; }
   
   public String toString()
   {
      return name.equals("") ? "Finder" : name;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element finder = doc.createElement("finder");
      XMLManager.addElement(finder,"name",getName());
      XMLManager.addElement(finder,"query",getQuery());
      XMLManager.addElement(finder,"order",getOrder());
      
      return finder;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
   	if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(JawsEjbJar.JAWS_DOCUMENT))
   	{
	      NodeList nl = elt.getChildNodes();
	      for (int i = 0; i < nl.getLength(); i++)
	      {
	         Node n = nl.item(i);
	         String name = n.getNodeName();
	         
	         if (name.equals("name"))
            {
               setName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("query"))
	         {
               setQuery(n.hasChildNodes() ? XMLManager.getString(n) : "");
	         } else if (name.equals("order"))
            {
               setOrder(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 
	      }
   	}
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
