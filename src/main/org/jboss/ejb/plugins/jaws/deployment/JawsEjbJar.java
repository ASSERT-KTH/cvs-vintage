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
 *   @version $Revision: 1.7 $
 */
public class JawsEjbJar
   extends com.dreambean.ejx.ejb.EjbJar
{
   // Constants -----------------------------------------------------
   public static final String JAWS_DOCUMENT="jaws";
    
   // Attributes ----------------------------------------------------
   String dataSource = "DefaultDS";
   String typeMapping = "Hypersonic SQL";
	
   TypeMappings tm;
	
	Customizer c;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public JawsEjbJar()
   {
      super();
      
      tm = new TypeMappings();
      add(tm);
   }
   
   // Public --------------------------------------------------------
   public void setDataSource(String ds) 
   { 
   	dataSource = ds; 
      
   	// Make sure it is prefixed with java:
   	if (!dataSource.startsWith("java:/"))
   	   dataSource = "java:/"+dataSource;
   }
   
   public String getDataSource() 
   { 
   	return dataSource; 
   }
   
   public void setTypeMapping(String tm) 
   { 
   	String old = typeMapping; 
   	typeMapping = tm; 
   	firePropertyChange("TypeMapping", old, typeMapping); 
   }
   
   public String getTypeMapping() 
   {
   	return typeMapping;
   }
	
   public TypeMappings getTypeMappings() { return tm; }
   
   // BeanContextChildComponentProxy implementation -----------------
   public java.awt.Component getComponent()
   {
      if (c == null)
      {
         c = new JawsEjbJarViewer();
         c.setObject(this);
      }
   	
      return (java.awt.Component)c;
   }
	
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
        throws Exception
   {
      Element ejbjar = doc.createElement(JAWS_DOCUMENT);

      XMLManager.addElement(ejbjar,"datasource",dataSource);
      XMLManager.addElement(ejbjar,"type-mapping",getTypeMapping());
		
      ejbjar.appendChild(tm.exportXml(doc));
      ejbjar.appendChild(eb.exportXml(doc));
      
      return ejbjar;
   }
   
   public void importXml(Element elt)
        throws Exception
   {
	  if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(JAWS_DOCUMENT))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("enterprise-beans"))
            {
               eb.importXml((Element)n);
            } else if (name.equals("type-mappings"))
            {
	           tm.importXml((Element)n);
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
         remove(ad);
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected void createEnterpriseBeans()
   {
      eb = new JawsEnterpriseBeans();
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