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
 *   @version $Revision: 1.4 $
 */
public class Mapping
   extends BeanContextServicesSupport
   implements XmlExternalizable, PropertyChangeListener
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String javaType= "";
   String jdbcType= "";
   String sqlType= "";
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public Mapping()
   {
      addPropertyChangeListener(this);
   }
    
   // Public --------------------------------------------------------
   public void setJavaType(String t) { javaType = t; }
   public String getJavaType() { return javaType; }
   
   public void setJdbcType(String t) { String old = jdbcType; jdbcType = t; pcSupport.firePropertyChange("JdbcType", old, jdbcType); }
   public String getJdbcType() { return jdbcType; }
   
   public void setSqlType(String t) { sqlType = t; }
   public String getSqlType() { return sqlType; }
   
   public String toString()
   {
      return javaType.equals("") ? "Mapping" : javaType;
   }
   
   // PropertyChange ------------------------------------------------
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
      pcSupport.addPropertyChangeListener(listener);
   }

   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      pcSupport.removePropertyChangeListener(listener);
   }
   
   // XmlExternalizable implementation ------------------------------
   public Element exportXml(Document doc)
   	throws Exception
   {
      Element mapping = doc.createElement("mapping");
      XMLManager.addElement(mapping,"java-type",getJavaType());
      XMLManager.addElement(mapping,"jdbc-type",getJdbcType());
      XMLManager.addElement(mapping,"sql-type",getSqlType());
      
      return mapping;
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
	         
	         if (name.equals("java-type"))
            {
               setJavaType(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("jdbc-type"))
	         {
               setJdbcType(n.hasChildNodes() ? XMLManager.getString(n) : "");
	         } else if (name.equals("sql-type"))
            {
               setSqlType(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 
	      }
   	}
   }
   
   public void propertyChange(PropertyChangeEvent evt)
   {
      if (evt.getPropertyName().equals("JdbcType"))
      {
         // Copy to SQL
         setSqlType(getJdbcType());
      }
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
