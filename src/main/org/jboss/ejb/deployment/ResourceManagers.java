/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
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
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.5 $
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
   public void createResourceManager(String clazz)
      throws IOException, ClassNotFoundException
   {
      addResourceManager(clazz);
   }
	
   public XmlExternalizable addResourceManager(String clazz)
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
               
               XmlExternalizable resMgr = (XmlExternalizable)addResourceManager(resType);
               
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
