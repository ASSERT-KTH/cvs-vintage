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
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.6 $
 */
public class JawsEnterpriseBeans
   extends com.dreambean.ejx.ejb.EnterpriseBeans
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   
   public com.dreambean.ejx.ejb.Entity addEntity()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Entity)instantiateChild("org.jboss.ejb.plugins.jaws.deployment.JawsEntity");
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
      throws Exception
   {
      Element enterprisebeans = super.exportXml(doc);
      
      // No settings
		
      return enterprisebeans;
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
            
            if (name.equals("entity"))
            {
               try
               {
                  NodeList enl = ((Element)n).getElementsByTagName("ejb-name");
                  String ejbName = XMLManager.getString(enl.item(0));
                  JawsEntity bean = (JawsEntity)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
//                  Logger.exception(e);
                  // Does not exist anymore...
               }
            } 
         }
      } else
      {
         super.importXml(elt);
         
         // Remove BMP beans
         Iterator enum = getEntities();
         while(enum.hasNext())
         {
            JawsEntity entity = (JawsEntity)enum.next();
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
