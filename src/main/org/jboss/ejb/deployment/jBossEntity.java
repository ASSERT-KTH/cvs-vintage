/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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
import com.dreambean.ejx.ejb.EnterpriseBeans;
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.6 $
 */
public class jBossEntity
   extends com.dreambean.ejx.ejb.Entity
   implements jBossEnterpriseBean
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String jndiName= "";
   String configurationName = "";
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setJndiName(String n) { String old = jndiName; jndiName = n; firePropertyChange("JndiName",old,jndiName); }
   public String getJndiName() { return jndiName; }
   
   public void setConfigurationName(String n) { configurationName = n; }
   public String getConfigurationName() { return configurationName; }
   
   public EnterpriseBeans getEnterpriseBeans()
   {
      return (EnterpriseBeans)getBeanContext();
   }
   
   public ContainerConfiguration getContainerConfiguration() 
   { 
      jBossEjbJar jar = (jBossEjbJar)getEnterpriseBeans().getBeanContext();
      return jar.getContainerConfigurations().getContainerConfiguration(configurationName);
   }
   
   public com.dreambean.ejx.ejb.ResourceReference addResourceReference()
      throws Exception
   {
      return (com.dreambean.ejx.ejb.ResourceReference)instantiateChild("org.jboss.ejb.deployment.jBossResourceReference");
   }
   
   public com.dreambean.ejx.ejb.EjbReference addEjbReference()
      throws Exception
   {
      return (com.dreambean.ejx.ejb.EjbReference)instantiateChild("org.jboss.ejb.deployment.jBossEjbReference");
   }
    
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
    throws Exception
   {
      Element entity = doc.createElement("entity");
      XMLManager.addElement(entity,"ejb-name",getEjbName());
      
      XMLManager.addElement(entity,"jndi-name",getJndiName());
      XMLManager.addElement(entity,"configuration-name",getConfigurationName());
      
      for (Iterator enum = getEjbReferences(); enum.hasNext();)
      {
         entity.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }

      for (Iterator enum = getResourceReferences(); enum.hasNext();)
      {
         entity.appendChild(((XmlExternalizable)enum.next()).exportXml(doc));
      }
      
      return entity;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
    if (elt.getOwnerDocument().getDocumentElement().getTagName().equals("jboss"))
    {
          NodeList nl = elt.getChildNodes();
          for (int i = 0; i < nl.getLength(); i++)
          {
             Node n = nl.item(i);
             String name = n.getNodeName();
             
             if (name.equals("jndi-name"))
             {
                setJndiName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("configuration-name"))
            {
               setConfigurationName(n.hasChildNodes() ? XMLManager.getString(n) : "");
             } else if (name.equals("resource-ref"))
            {
               NodeList rnl = ((Element)n).getElementsByTagName("res-ref-name");
               String resName = XMLManager.getString(rnl.item(0));
               Iterator enum = getResourceReferences();
               while(enum.hasNext())
               {
                  jBossResourceReference ref = (jBossResourceReference)enum.next();
                  if (ref.getName().equals(resName))
                  {
                     ref.importXml((Element)n);
                     break;
                  }
               }
            } else if (name.equals("ejb-ref"))
            {
               NodeList rnl = ((Element)n).getElementsByTagName("ejb-ref-name");
               String resName = XMLManager.getString(rnl.item(0));
               Iterator enum = getEjbReferences();
               while(enum.hasNext())
               {
                  jBossEjbReference ref = (jBossEjbReference)enum.next();
                  if (ref.getName().equals(resName))
                  {
                     ref.importXml((Element)n);
                     break;
                  }
               }
            }
          }
    } else // EJB-JAR XML
    {
         super.importXml(elt);
         setJndiName(getEjbName());
    }
   }
   
   public void propertyChange(PropertyChangeEvent evt)
   {
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
    class AttributeAggregate
        extends BeanContextSupport
        implements BeanContextContainerProxy
    {
        Container com = new GenericCustomizer(this);
    
        public Container getContainer()
        {
            return com;
        }
    
        public Iterator iterator()
        {
            return getEjbReferences();
        }
        
        public String toString() 
        {
             return "EJB references";
        }
    }
}
