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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class ResourceManagers
   extends BeanContextSupport
   implements BeanContextChildComponentProxy, XmlExternalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Customizer c;
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public XmlExternalizable createResourceManager(String clazz)
      throws IOException, ClassNotFoundException
   {
      return (XmlExternalizable)instantiateChild(clazz);
   }
   
   public Iterator getResourceManagersByType(String type)
   {
      ArrayList al = new ArrayList();
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         ResourceManager rm = (ResourceManager)enum.next();
         if (rm.getType().equals(type))
            al.add(rm);
      }
      
      return al.iterator();
   }
   
   public ResourceManager getResourceManager(String name)
   {
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         ResourceManager rm = (ResourceManager)enum.next();
         if (rm.getName().equals(name))
            return rm;
      }
      return null;
   }
   
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (c == null)
          c = new GenericCustomizer(this);
      return (Component)c;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
      throws Exception
   {
      Element resourcemanagers = doc.createElement("resource-managers");
      
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         resourcemanagers.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
      
      return resourcemanagers;
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
            
            if (name.equals("resource-manager"))
            {
               String resType = ((Element)n).getAttribute("res-class");
               
               XmlExternalizable resMgr = (XmlExternalizable)createResourceManager(resType);
               
               resMgr.importXml((Element)n);
            } 
         }
      } 
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
