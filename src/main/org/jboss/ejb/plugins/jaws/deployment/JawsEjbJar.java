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
 *   @version $Revision: 1.2 $
 */
public class JawsEjbJar
   extends com.dreambean.ejx.ejb.EjbJar
{
   // Constants -----------------------------------------------------
   public static final String JAWS_DOCUMENT="jaws";
    
   // Attributes ----------------------------------------------------
   TypeMappings tm;

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public JawsEjbJar()
   {
      super();
      
      tm = new TypeMappings();
      add(tm);
   }
   
   // Public --------------------------------------------------------
   public TypeMappings getTypeMappings() { return tm; }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
        throws Exception
   {
      Element ejbjar = doc.createElement(JAWS_DOCUMENT);

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