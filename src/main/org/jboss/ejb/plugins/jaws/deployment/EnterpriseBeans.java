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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class EnterpriseBeans
   extends com.dreambean.ejx.ejb.EnterpriseBeans
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String dataSource = "";
   String typeMapping = "";
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setDataSource(String ds) { dataSource = ds; }
   public String getDataSource() { return dataSource; }
   
   public void setTypeMapping(String tm) { String old = typeMapping; typeMapping = tm; firePropertyChange("TypeMapping", old, typeMapping); }
   public String getTypeMapping() { return typeMapping; }
   
   public com.dreambean.ejx.ejb.Entity addEntity()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Entity)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.Entity");
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
      throws Exception
   {
      Element enterprisebeans = super.exportXml(doc);
      
      XMLManager.addElement(enterprisebeans,"datasource",dataSource);
      XMLManager.addElement(enterprisebeans,"type-mapping",getTypeMapping());
      
      return enterprisebeans;
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
            
            if (name.equals("entity"))
            {
               try
               {
                  NodeList enl = ((Element)n).getElementsByTagName("ejb-name");
                  String ejbName = XMLManager.getString(enl.item(0));
                  Entity bean = (Entity)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
//                  e.printStackTrace();
                  // Does not exist anymore...
               }
            } else if (name.equals("datasource"))
            {
               setDataSource(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("type-mapping"))
            {
               setTypeMapping(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 
         }
      } else
      {
         super.importXml(elt);
         
         // Remove BMP beans
         Iterator enum = getEntities();
         while(enum.hasNext())
         {
            Entity entity = (Entity)enum.next();
            if (entity.getPersistenceType().equals("Bean"))
            {
               remove(entity);
            }
         }
         
         // Remove session beans
         enum = getSessions();
         while(enum.hasNext())
         {
            remove(enum.next());
         }
      }
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
