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

import com.dreambean.awt.GenericCustomizer;
import com.dreambean.awt.GenericMethodDialog;
import com.dreambean.awt.BeanContextPanel;
import com.dreambean.awt.BeanContextTreeView;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class ResourceManagers
   extends BeanContextSupport
   implements BeanContextChildComponentProxy, XmlExternalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Component c;
    
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
      {
   		c = new BeanContextPanel(this);
   		JSplitPane sp = (JSplitPane)c;
   		JScrollPane scrollPane = (JScrollPane)sp.getLeftComponent();
			((BeanContextTreeView)scrollPane.getViewport().getView()).expandPath(((BeanContextTreeView)scrollPane.getViewport().getView()).getPathForRow(0));
			((BeanContextTreeView)scrollPane.getViewport().getView()).setRootVisible(false);
			
			JToolBar toolBar = new JToolBar();
			toolBar.add(new AbstractAction("New")
			{
				public void actionPerformed(ActionEvent evt)
				{
					try
					{
						BeanInfo bi = Introspector.getBeanInfo(ResourceManagers.class);
						
						MethodDescriptor[] mdList = bi.getMethodDescriptors();
						for (int i = 0; i < mdList.length; i++)
						{
							if (mdList[i].getName().equals("createResourceManager"))
							{
								new GenericMethodDialog(ResourceManagers.this, mdList[i], (Frame)SwingUtilities.getRoot(c));
								break;
							}
						}
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			
			JPanel p = new JPanel(new BorderLayout());
			p.add("Center", c);
			p.add("North", toolBar);
			
			c = p;
			c.setName("Resource managers");
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
