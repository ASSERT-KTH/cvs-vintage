/*
 * Copyright 1999 by dreamBean Software,
 * All rights reserved.
 */
package org.jboss.ejb.plugins.jaws.deployment;

import java.awt.*;
import java.beans.*;
import java.beans.beancontext.*;
import java.io.*;
import java.util.*;

import javax.swing.JTabbedPane;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.*;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;
import com.dreambean.ejx.ejb.EjbReference;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class Entity
   extends com.dreambean.ejx.ejb.Entity
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String tableName= "";
   boolean createTable = true;
   boolean removeTable = false;
   boolean tunedUpdates = true;

   Container c;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setTableName(String n) { String old = tableName; tableName = n; firePropertyChange("TableName",old,tableName); }
   public String getTableName() { return tableName; }
   
   public void setCreateTable(boolean c) { createTable = c; }
   public boolean getCreateTable() { return createTable; }
   
   public void setRemoveTable(boolean r) { removeTable = r; }
   public boolean getRemoveTable() { return removeTable; }
   
   public void setTunedUpdates(boolean t) { tunedUpdates = t; }
   public boolean getTunedUpdates() { return tunedUpdates; }
   
   public com.dreambean.ejx.ejb.CMPField addCMPField()
      throws Exception
   {
      return (com.dreambean.ejx.ejb.CMPField)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.CMPField");
   }
   
   public Finder addFinder()
      throws Exception
   {
      return (Finder)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.Finder");
   }
   
   public Iterator getFinders()
   {
      return Util.getChildrenByClass(iterator(),Finder.class);
   }
   
   // BeanContextContainerProxy implementation -----------------
   public Container getContainer()
   {
      if (c == null)
      {
          c = new JTabbedPane();
          c.add(new GenericCustomizer(this), "Entity");
          
          try
          {
             c.add(new GenericPropertySheet(this, CMPField.class), "CMP mappings");
          } catch (Exception e) {}
          
          try
          {
             c.add(new GenericPropertySheet(this, Finder.class), "Finders");
          } catch (Exception e) {}
      }
      return (Container)c;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element entity = doc.createElement("entity");
      XMLManager.addElement(entity,"ejb-name",getEjbName());
      
      XMLManager.addElement(entity,"table-name",getTableName());
      XMLManager.addElement(entity,"create-table",new Boolean(getCreateTable()).toString());
      XMLManager.addElement(entity,"remove-table",new Boolean(getRemoveTable()).toString());
      XMLManager.addElement(entity,"tuned-updates",new Boolean(getTunedUpdates()).toString());
      
      for (Iterator enum = getCMPFields(); enum.hasNext();)
      {
         entity.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
      
      for (Iterator enum = getFinders(); enum.hasNext();)
      {
         entity.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
      
      return entity;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
   	if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(EjbJar.JAWS_DOCUMENT))
   	{
	      NodeList nl = elt.getChildNodes();
	      for (int i = 0; i < nl.getLength(); i++)
	      {
	         Node n = nl.item(i);
	         String name = n.getNodeName();
	         
	         if (name.equals("table-name"))
	         {
	            setTableName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("create-table"))
            {
               setCreateTable(new Boolean(n.hasChildNodes() ? XMLManager.getString(n) : "").booleanValue());
            } else if (name.equals("remove-table"))
            {
               setRemoveTable(new Boolean(n.hasChildNodes() ? XMLManager.getString(n) : "").booleanValue());
            } else if (name.equals("tuned-updates"))
            {
               setTunedUpdates(new Boolean(n.hasChildNodes() ? XMLManager.getString(n) : "").booleanValue());
            } else if (name.equals("cmp-field"))
            {
               NodeList rnl = ((Element)n).getElementsByTagName("field-name");
               String fieldName = XMLManager.getString(rnl.item(0));
               Iterator enum = getCMPFields();
               while(enum.hasNext())
               {
                  CMPField field = (CMPField)enum.next();
                  if (field.getFieldName().equals(fieldName))
                  {
                     field.importXml((Element)n);
                     break;
                  }
               }
            } else if (name.equals("finder"))
            {
               addFinder().importXml((Element)n);
            } 
	      }
   	} else // EJB-JAR XML
   	{
         super.importXml(elt);
         setTableName(getEjbName());
         
         // Remove everything but CMP fields
         Iterator enum = iterator();
         while(enum.hasNext())
         {
            Object child = enum.next();
            if (child instanceof CMPField || child instanceof EjbReference)
               continue;
               
            remove(child);
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
