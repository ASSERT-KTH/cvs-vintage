/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
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
 *   @version $Revision: 1.1 $
 */
public class EnterpriseBeans
   extends com.dreambean.ejx.ejb.EnterpriseBeans
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   boolean secure = true;
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setSecure(boolean s) { secure = s; }
   public boolean isSecure() { return secure; }
   
   public com.dreambean.ejx.ejb.Entity addEntity()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Entity)instantiateChild("org.jboss.ejb.deployment.Entity");
   }

   public com.dreambean.ejx.ejb.Session addSession()
      throws IOException, ClassNotFoundException
   {
      return (com.dreambean.ejx.ejb.Session)instantiateChild("org.jboss.ejb.deployment.Session");
   }
   
	public void addJndiPrefix(String prefix)
	{
		Iterator enum = super.iterator();
		while(enum.hasNext())
		{
			EnterpriseBean bean = (EnterpriseBean)enum.next();
			bean.setJndiName(prefix + bean.getJndiName());
		}
	}

   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
      throws Exception
   {
      Element enterprisebeans = super.exportXml(doc);
      
      XMLManager.addElement(enterprisebeans,"secure",new Boolean(isSecure()).toString());
      
      return enterprisebeans;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(EjbJar.JBOSS_DOCUMENT))
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
                  e.printStackTrace();
                  // Does not exist anymore...
               }
            } else if (name.equals("session"))
            {
               try
               {
                  NodeList enl = ((Element)n).getElementsByTagName("ejb-name");
                  String ejbName = XMLManager.getString(enl.item(0));
                  Session bean = (Session)getEjb(ejbName);
                  bean.importXml((Element)n);
               } catch (IllegalArgumentException e)
               {
                  e.printStackTrace();
                  // Does not exist anymore...
               }
            } else if (name.equals("secure"))
            {
               setSecure(new Boolean(XMLManager.getString(n)).booleanValue());
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
