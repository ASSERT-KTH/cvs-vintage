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

import org.w3c.dom.*;

import com.dreambean.awt.*;
import com.dreambean.ejx.xml.*;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
 */
public class JawsCMPField
   extends com.dreambean.ejx.ejb.CMPField
   implements PropertyChangeListener
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String columnName = "";
   String sqlType = "";
   String jdbcType = "";

   Customizer c;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public JawsCMPField()
   {
      addPropertyChangeListener(this);
   }
    
   // Public --------------------------------------------------------
   public void setColumnName(String n) { columnName = n; }
   public String getColumnName() { return columnName; }
   
   // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   public void setSqlType(String s) { String old = sqlType; sqlType = s; pcSupport.firePropertyChange("SqlType", old, sqlType);}
    // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  
   public String getSqlType() { return sqlType; }
  
   // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   public void setJdbcType(String s) { String old = jdbcType; jdbcType = s; pcSupport.firePropertyChange("JdbcType", old, jdbcType);}
    // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  
   public String getJdbcType() { return jdbcType; }
  
   
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
   {
      Element cmpfield = doc.createElement("cmp-field");
      XMLManager.addElement(cmpfield,"field-name",getFieldName());
      
      XMLManager.addElement(cmpfield,"column-name",getColumnName());
      XMLManager.addElement(cmpfield,"sql-type",getSqlType());
      XMLManager.addElement(cmpfield,"jdbc-type",getJdbcType());
      
      return cmpfield;
   }
   
   public void importXml(Element elt)
   {
      ((JawsEnterpriseBeans)getBeanContext().getBeanContext()).addPropertyChangeListener("TypeMapping", this);
      
      if (elt.getOwnerDocument().getDocumentElement().getTagName().equals(JawsEjbJar.JAWS_DOCUMENT))
      {
         NodeList nl = elt.getChildNodes();
         for (int i = 0; i < nl.getLength(); i++)
         {
            Node n = nl.item(i);
            String name = n.getNodeName();
            
            if (name.equals("column-name"))
            {
               setColumnName(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("sql-type"))
            {
               setSqlType(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } else if (name.equals("jdbc-type"))
            {
               setJdbcType(n.hasChildNodes() ? XMLManager.getString(n) : "");
            } 

         }
      } else // EJB-JAR XML
      {
         super.importXml(elt);
         setColumnName(getFieldName());
      }
   }
   
   public String toString()
   {
      return (getFieldName().equals("")) ? "CMP field mapping" : getFieldName();
   }
    
   public void propertyChange(PropertyChangeEvent evt)
   {
      if (evt.getPropertyName().equals("TypeMapping"))
      {
         // Try to find out SQL
         try
         {
            ClassLoader cl = ((JawsFileManager)getBeanContext().getBeanContext().getBeanContext().getBeanContext()).getClassLoader();
            Class clazz = cl.loadClass(((JawsEntity)getBeanContext()).getEjbClass());
            java.lang.reflect.Field type = clazz.getField(getFieldName());
            
			 // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            String sql = ((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMappings().getTypeMapping(((JawsEnterpriseBeans)getBeanContext().getBeanContext()).getTypeMapping()).getSqlTypeForJavaType(type.getType(), (JawsEntity)getBeanContext());
            if (sql != null)
               setSqlType(sql);
           
		     // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
              
            String jdbc = ((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMappings().getTypeMapping(((JawsEnterpriseBeans)getBeanContext().getBeanContext()).getTypeMapping()).getJdbcTypeForJavaType(type.getType(), (JawsEntity)getBeanContext());
           
		     // UNREADABLE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
           
			if (jdbc != null)
               setJdbcType(jdbc);
            
         } catch (Throwable e)
         {
            // Didn't work..
            e.printStackTrace(System.err);
         }
      }
   }
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
