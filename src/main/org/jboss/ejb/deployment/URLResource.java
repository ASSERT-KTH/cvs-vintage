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
public class URLResource
   extends BeanContextChildSupport
   implements BeanContextChildComponentProxy, XmlExternalizable, ResourceManager
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name= "";
   
   String url= "";

   Component c;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setName(String n) { name = n; }
   public String getName() { return name; }
   
   public void setUrl(String u) { url = u; }
   public String getUrl() { return url; }
   
   public String getType() { return "java.net.URL"; }
   
   public ResourceManagers getResourceManagers()
   {
      return (ResourceManagers)getBeanContext();
   }
   
   public void removeResource()
   {
   	getResourceManagers().remove(this);
   }
	
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (c == null)
          c = new GenericCustomizer(false, this);
      return c;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element resourcemanager = doc.createElement("resource-manager");
      XMLManager.addAttribute(resourcemanager,"res-class",getClass().getName());
      XMLManager.addElement(resourcemanager,"res-name",getName());
      
      XMLManager.addElement(resourcemanager,"res-url",getUrl());
      
      return resourcemanager;
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
	         
	         if (name.equals("res-url"))
	         {
	            setUrl(n.hasChildNodes() ? XMLManager.getString(n) : "");
	         } else if (name.equals("res-name"))
            {
               setName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            }
	      }
   	}
   }
   
   public void propertyChange(PropertyChangeEvent evt)
   {
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
