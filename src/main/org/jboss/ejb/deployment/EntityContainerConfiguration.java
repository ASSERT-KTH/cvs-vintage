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
import com.dreambean.ejx.xml.XMLManager;
import com.dreambean.ejx.xml.XmlExternalizable;
import com.dreambean.ejx.Util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.4 $
 */
public class EntityContainerConfiguration
   extends ContainerConfiguration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String commitOption = "A";

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------
   public void setCommitOption(String co) { commitOption = co; }
   public String getCommitOption() { return commitOption; }

   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element containerconfiguration = super.exportXml(doc);
      XMLManager.addElement(containerconfiguration,"commit-option",getCommitOption());
         
      return containerconfiguration;
   }
   
   public void importXml(Element elt)
      throws Exception
   {
		super.importXml(elt);
	
   	if (elt.getOwnerDocument().getDocumentElement().getTagName().equals("jboss"))
   	{
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("commit-option"))
            {
               setCommitOption(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 
			}
   	}
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
