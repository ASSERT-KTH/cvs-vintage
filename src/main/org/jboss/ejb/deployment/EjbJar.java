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
public class EjbJar
   extends com.dreambean.ejx.ejb.EjbJar
{
   // Constants -----------------------------------------------------
   public static final String JBOSS_DOCUMENT="jboss";
    
   // Attributes ----------------------------------------------------
   ResourceManagers rm;
   ContainerConfigurations cc;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public EjbJar()
   {
      super();
      
      rm = new ResourceManagers();
      add(rm);
      
      cc = new ContainerConfigurations();
      add(cc);
   }
   
   // Public --------------------------------------------------------
   
   public ResourceManagers getResourceManagers() { return rm; }
   public ContainerConfigurations getContainerConfigurations() { return cc; }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element ejbjar = doc.createElement("jboss");

      ejbjar.appendChild(eb.exportXml(doc));
      ejbjar.appendChild(rm.exportXml(doc));
      ejbjar.appendChild(cc.exportXml(doc));
      
      return ejbjar;
   }
   
   public void importXml(Element elt)
   	throws Exception
   {
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(JBOSS_DOCUMENT))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("enterprise-beans"))
            {
               eb.importXml((Element)n);
            } else if (name.equals("resource-managers"))
            {
               rm.importXml((Element)n);
            } else if (name.equals("container-configurations"))
            {
               cc.importXml((Element)n);
            }
         }
      } else
      {
         super.importXml(elt);
         remove(ad);
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected void createEnterpriseBeans()
   {
      eb = new EnterpriseBeans();
      add(eb);
   }
   
   protected void createAssemblyDescriptor()
   {
      ad = new com.dreambean.ejx.ejb.AssemblyDescriptor();
      add(ad);
   }
   
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
