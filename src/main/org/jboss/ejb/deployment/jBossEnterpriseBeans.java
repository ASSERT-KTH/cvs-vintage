/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
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
import com.dreambean.awt.GenericCustomizer;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;
import org.jboss.logging.Logger;


/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.7 $
 */
public class jBossEnterpriseBeans
   extends com.dreambean.ejx.ejb.EnterpriseBeans
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
    Container c;
    Component com;
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------

    
   public com.dreambean.ejx.ejb.Entity addEntity()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Entity)instantiateChild("org.jboss.ejb.deployment.jBossEntity");
   }

   public com.dreambean.ejx.ejb.Session addSession()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Session)instantiateChild("org.jboss.ejb.deployment.jBossSession");
   }
   
    public void addJndiPrefix(String prefix)
    {
        Iterator enum = super.iterator();
        while(enum.hasNext())
        {
            jBossEnterpriseBean bean = (jBossEnterpriseBean)enum.next();
            bean.setJndiName(prefix + bean.getJndiName());
        }
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
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(jBossEjbJar.JBOSS_DOCUMENT))
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
                  
                  jBossEntity bean = (jBossEntity)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
                  Logger.exception(e);;
                  // Does not exist anymore...
               }
            } else if (name.equals("session"))
            {
               try
               {
                  NodeList enl = ((Element)n).getElementsByTagName("ejb-name");
                  String ejbName = XMLManager.getString(enl.item(0));
                  jBossSession bean = (jBossSession)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
                  Logger.exception(e);;
                  // Does not exist anymore...
               }
            } 
         }
      } else
      {
         super.importXml(elt);
      }
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
