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

import javax.ejb.EJBObject;
import javax.swing.JTabbedPane;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.GenericPropertySheet;
import com.dreambean.awt.GenericCustomizer;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;
import com.dreambean.ejx.ejb.EjbReference;
import org.jboss.logging.Logger;


/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.7 $
 */
public class TypeMapping
   extends BeanContextServicesSupport
   implements BeanContextContainerProxy, XmlExternalizable
{
   // Constants -----------------------------------------------------
   static String[] PRIMITIVES = {"boolean","byte","char","short","int","long","float","double"};
   static String[] PRIMITIVE_CLASSES = {"java.lang.Boolean","java.lang.Byte","java.lang.Character","java.lang.Short","java.lang.Integer","java.lang.Long","java.lang.Float","java.lang.Double"};
    
   // Attributes ----------------------------------------------------
   String name= "";
   
   Container c;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setName(String n) { name = n; }
   public String getName() { return name; }
   
   public String toString()
   {
      return name.equals("") ? "Type mapping" : name;
   }
   
   public String getSqlTypeForJavaType(Class type, JawsEntity entity)
   {
      String name = type.getName();
      
      // Check primitive first
      for (int i = 0; i < PRIMITIVES.length; i++)
      {
         if (type.getName().equals(PRIMITIVES[i]))
         {
            // Translate into class
            name = PRIMITIVE_CLASSES[i];
            break;
         }
      }
      
      // Check for reference
      try
      {
         if (EJBObject.class.isAssignableFrom(type))
         {
            // It's a EJB reference. Find it in EJB-reference list
            Iterator enum = entity.getEjbReferences();
            while (enum.hasNext())
            {
               EjbReference ref = (EjbReference)enum.next();
               if (ref.getRemote().equals(name))
               {
                  return ref.getName();
               }
            }
			}
		} catch (Exception e)
      {
         Logger.exception(e);
      }
      
      // Check other types
      String objectType = null;
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         Mapping m = (Mapping)enum.next();
         if (m.getJavaType().equals(name))
            return m.getSqlType();
         if (m.getJavaType().equals("java.lang.Object"))
            objectType = m.getSqlType();
      }
      
      // Return mapping for java.lang.Object
      return objectType;
   }
   
   public String getJdbcTypeForJavaType(Class type, JawsEntity entity)
   {
      String name = type.getName();
      
      // Check primitive first
      for (int i = 0; i < PRIMITIVES.length; i++)
      {
         if (type.getName().equals(PRIMITIVES[i]))
         {
            // Translate into class
            name = PRIMITIVE_CLASSES[i];
            break;
         }
      }
      
      // Check for reference
      try
      {
         if (EJBObject.class.isAssignableFrom(type))
         {
            // It's a EJB reference. Find it in EJB-reference list
            Iterator enum = entity.getEjbReferences();
            while (enum.hasNext())
            {
               EjbReference ref = (EjbReference)enum.next();
               if (ref.getRemote().equals(name))
               {
                  return "REF";
               }
            }
         }
      } catch (Exception e)
      {
         Logger.exception(e);
      }
      
      // Check other types
      String objectType = null;
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         Mapping m = (Mapping)enum.next();
         if (m.getJavaType().equals(name))
            return m.getJdbcType();
         if (m.getJavaType().equals("java.lang.Object"))
            objectType = m.getJdbcType();
      }
      
      // Return mapping for java.lang.Object
      return objectType;
   }
   
   public Mapping addMapping()
      throws Exception
   {
      return (Mapping)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.Mapping");
   }
   
   // BeanContextContainerProxy implementation -----------------
   public Container getContainer()
   {
      if (c == null)
      {
          c = new JTabbedPane();
          c.add(new GenericCustomizer(this), "Type mapping");
          
          try
          {
             c.add(new GenericPropertySheet(this, Mapping.class), "Mappings");
          } catch (Exception e) {}
      }
      return (Container)c;
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element typemapping = doc.createElement("type-mapping-definition");
      XMLManager.addElement(typemapping,"name",getName());
      
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         typemapping.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
         
      return typemapping;
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
            } else if (name.equals("mapping"))
	         {
	            addMapping().importXml((Element)n);
	         } 
	      }
   	}
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
