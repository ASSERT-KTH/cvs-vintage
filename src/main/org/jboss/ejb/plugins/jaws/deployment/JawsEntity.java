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
import com.dreambean.ejx.ejb.CMPField;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.5 $
 */
public class JawsEntity
   extends com.dreambean.ejx.ejb.Entity
	implements BeanContextContainerProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String tableName= "";
   boolean createTable = true;
   boolean removeTable = false;
   boolean tunedUpdates = true;
	boolean readOnly = false;
	int timeOut = 5*60; // 5 minute timeout on read-only state
   boolean pkConstraint = false;

   Customizer c;
   
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
   
   public void setReadOnly(boolean t) { readOnly = t; }
   public boolean getReadOnly() { return readOnly; }
	
   public void setTimeOut(int t) { timeOut = t; }
   public int getTimeOut() { return timeOut; }
	
   public void setPkConstraint(boolean pkConstraint) { this.pkConstraint = pkConstraint; }
   public boolean getPkConstraint() { return pkConstraint; }
	
   public com.dreambean.ejx.ejb.CMPField addCMPField()
      throws Exception
   {
      return (CMPField)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.JawsCMPField");
   }
   
   public EjbReference addEjbReference()
      throws Exception
   {
      return (EjbReference)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.JawsEjbReference");
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
   
   // BeanContextChildComponentProxy implementation -----------------
   public Container getContainer()
   {
      if (c == null)
      {
          c = new JawsEntityViewer();
			 c.setObject(this);
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
      XMLManager.addElement(entity,"read-only",new Boolean(getReadOnly()).toString());
      XMLManager.addElement(entity,"time-out",new Integer(getTimeOut()).toString());
      XMLManager.addElement(entity,"pk-constraint",new Boolean(getPkConstraint()).toString());
      
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
   	if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(JawsEjbJar.JAWS_DOCUMENT))
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
            } else if (name.equals("read-only"))
            {
               setReadOnly(new Boolean(n.hasChildNodes() ? XMLManager.getString(n) : "").booleanValue());
            } else if (name.equals("time-out"))
            {
               setTimeOut(new Integer(n.hasChildNodes() ? XMLManager.getString(n) : "").intValue());
            } else if (name.equals("cmp-field"))
            {
               NodeList rnl = ((Element)n).getElementsByTagName("field-name");
               String fieldName = XMLManager.getString(rnl.item(0));
               Iterator enum = getCMPFields();
               while(enum.hasNext())
               {
                  JawsCMPField field = (JawsCMPField)enum.next();
                  if (field.getFieldName().equals(fieldName))
                  {
                     field.importXml((Element)n);
                     break;
                  }
               }
            } else if (name.equals("finder"))
            {
               addFinder().importXml((Element)n);
            } else if (name.equals("pk-constraint"))
	    {
		setPkConstraint(new Boolean(n.hasChildNodes() ? XMLManager.getString(n) : "").booleanValue());
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
            if (child instanceof JawsCMPField || child instanceof JawsEjbReference)
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
