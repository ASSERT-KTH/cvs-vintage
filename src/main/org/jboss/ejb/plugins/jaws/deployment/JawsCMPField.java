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

import org.w3c.dom.*;

import com.dreambean.awt.*;
import com.dreambean.ejx.xml.*;
import org.jboss.logging.Logger;


/**
 *   <description>
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.9 $
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

   	public void setSqlType(String s) {

    	String old = sqlType;

	   	sqlType = s;

	   	pcSupport.firePropertyChange("SqlType", old, sqlType);
   	}

    public String getSqlType() {

	    if (!sqlType.equals("")) {

	        return sqlType;
	    }

		else try {

			// If the sqlType was not specified directly get the sqlType from the default
			// Path is Field->Entity->Beans->Jar(down)->TypeMappings->TMapping->Mapping

		    // Get the type from the Entity stored above
		 	ClassLoader cl = ((JawsFileManager)getBeanContext().getBeanContext().getBeanContext().getBeanContext()).getClassLoader();

			Class clazz = cl.loadClass(((JawsEntity)getBeanContext()).getEjbClass());

			java.lang.reflect.Field type = clazz.getField(getFieldName());

			// Retrieve the sql type from Mapping defaults
			sqlType = ((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMappings().getTypeMapping(((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMapping()).getSqlTypeForJavaType(type.getType(), (JawsEntity)getBeanContext());

			return sqlType;
		}

		catch (Exception e) {

			// not much we can do at this point
			e.printStackTrace(System.err);

			return null;
		}
	}

    public void setJdbcType(String s) {

       String old = jdbcType;

	   jdbcType = s;

	   pcSupport.firePropertyChange("JdbcType", old, jdbcType);
   	}

   	public String getJdbcType() {

		if (!jdbcType.equals("")) {

	    	return jdbcType;
	   	}

	   	else try {

			// If the sqlType was not specified directly get the sqlType from the default
			// Path is Field->Entity->Beans->Jar(down)->TypeMappings->TMapping->Mapping

			// Get the type
		 	ClassLoader cl = ((JawsFileManager)getBeanContext().getBeanContext().getBeanContext().getBeanContext()).getClassLoader();

			Class clazz = cl.loadClass(((JawsEntity)getBeanContext()).getEjbClass());

			java.lang.reflect.Field type = clazz.getField(getFieldName());

		    // Go get the default from jawsDefault
			// If there is no database in JawsEnterpriseBeans we use default Mappings
	   		jdbcType = ((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMappings().getTypeMapping(((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMapping()).getJdbcTypeForJavaType(type.getType(), (JawsEntity)getBeanContext());

	   		return jdbcType;
   		}
		catch (Exception e) {

			// not much we can do at this point
			e.printStackTrace(System.err);

			return null;
		}
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

			String sql = null;
            try {
                sql = ((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMappings().getTypeMapping(((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMapping()).getSqlTypeForJavaType(type.getType(), (JawsEntity)getBeanContext());
            } catch(NullPointerException e) {
                Logger.warning("Caught SQL NPE on JawsCMPField!");
            }
            if (sql != null)
               setSqlType(sql);

            String jdbc = null;
            try {
                jdbc = ((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMappings().getTypeMapping(((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).getTypeMapping()).getJdbcTypeForJavaType(type.getType(), (JawsEntity)getBeanContext());
            } catch(NullPointerException e) {
                Logger.warning("Caught JDBC NPE on JawsCMPField!");
            }

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
	protected void initializeBeanContextResources()
	{
		((JawsEjbJar)getBeanContext().getBeanContext().getBeanContext()).addPropertyChangeListener("TypeMapping", this);
	}
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
