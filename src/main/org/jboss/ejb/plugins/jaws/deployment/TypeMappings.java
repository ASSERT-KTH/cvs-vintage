/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class TypeMappings
   extends BeanContextSupport
   implements BeanContextChildComponentProxy, XmlExternalizable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Customizer c;
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void createTypeMapping(String name)
      throws IOException, ClassNotFoundException
   {
      TypeMapping tm = new TypeMapping();
      tm.setName(name);
      add(tm);
   }
   
   public TypeMapping addTypeMapping()
      throws IOException, ClassNotFoundException
   {
      return (TypeMapping)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.TypeMapping");
   }   
   
   public TypeMapping getTypeMapping(String name)
   {
      
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         TypeMapping tm = (TypeMapping)enum.next();
         if (tm.getName().equals(name))
            return tm;
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
      Element typemappings = doc.createElement("type-mappings");
      
      for (Iterator enum = iterator(); enum.hasNext();)
      {
         typemappings.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
      
      return typemappings;
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
            
            if (name.equals("type-mapping"))
            {
               TypeMapping tm = (TypeMapping)Beans.instantiate(getClass().getClassLoader(), "org.jboss.ejb.plugins.jaws.deployment.TypeMapping");
               tm.importXml((Element)n);
               
               // Check if already exists
               if (getTypeMapping(tm.getName()) != null)
               {
                  remove(getTypeMapping(tm.getName()));
               }
               add(tm);
            } 
         }
      } 
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
