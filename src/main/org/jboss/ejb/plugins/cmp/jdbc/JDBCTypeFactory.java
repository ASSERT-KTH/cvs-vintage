/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.ejb.EJBException;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCApplicationMetaData; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldPropertyMetaData; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityMetaData; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCValueClassMetaData; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCValuePropertyMetaData; 

/**
 * JDBCTypeFactory mapps Java Classes to JDBCType objects.  The main job of 
 * this class is to flatten the JDBCValueClassMetaData into columns.
 * 
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */
public class JDBCTypeFactory {
   // the type mapping to use with the specified database
   private JDBCTypeMappingMetaData typeMapping;
   
   // all the available dependent value classes (by javaType)
   private HashMap valueClasses = new HashMap();

   public JDBCTypeFactory(JDBCTypeMappingMetaData typeMapping, Collection dependentValueClasses) {
      this.typeMapping = typeMapping;
      for(Iterator i = dependentValueClasses.iterator(); i.hasNext(); ) {
         JDBCValueClassMetaData valueClass = (JDBCValueClassMetaData)i.next();
         valueClasses.put(valueClass.getJavaType(), valueClass);
      }
   }
   
   public JDBCType getFieldJDBCType(JDBCCMPFieldMetaData cmpField) {
      if(valueClasses.containsKey(cmpField.getFieldType())) {
         return createComplexType(cmpField);
      } else {
         return createSimpleType(cmpField);
      }
   }

   public int getJDBCTypeForJavaType(Class clazz) {      
      return typeMapping.getJdbcTypeForJavaType(clazz);
   }

   private JDBCType createSimpleType(JDBCCMPFieldMetaData cmpField) {
      String columnName = cmpField.getColumnName();
      Class javaType = cmpField.getFieldType();
      
      int jdbcType;
      String sqlType = cmpField.getSQLType();
      if(sqlType != null) {
         jdbcType = cmpField.getJDBCType();
      } else {
         // get jdbcType and sqlType from typeMapping
         sqlType = typeMapping.getSqlTypeForJavaType(javaType);
         jdbcType = typeMapping.getJdbcTypeForJavaType(javaType);
      }
      return new JDBCTypeSimple(columnName, javaType, jdbcType, sqlType);
   }      

   private JDBCType createComplexType(JDBCCMPFieldMetaData cmpField) {
      JDBCValueClassMetaData valueClass = (JDBCValueClassMetaData)valueClasses.get(cmpField.getFieldType());

      // get the properties
      ArrayList propertyList = createComplexProperties(valueClass, new PropertyStack(cmpField));
      
      // transform properties into an array
      JDBCTypeComplexProperty[] properties = new JDBCTypeComplexProperty[propertyList.size()];
      properties = (JDBCTypeComplexProperty[])propertyList.toArray(properties); 
      

      // create a map between propertyNames and the override
      HashMap overrides = new HashMap();
      Iterator iterator = cmpField.getPropertyOverrides().iterator();
      while(iterator.hasNext()) {
         JDBCCMPFieldPropertyMetaData p = (JDBCCMPFieldPropertyMetaData)iterator.next();
         overrides.put(p.getPropertyName(), p);
      }
            
      // override property default values
      JDBCCMPFieldPropertyMetaData override;
      for(int i=0; i<properties.length; i++) {
         
         // pop off the override, if present
         override = (JDBCCMPFieldPropertyMetaData)overrides.remove(properties[i].getPropertyName());
         
         if(override != null) {   
            // columnName
            if(override.getColumnName() != null) {
               properties[i].setColumnName(override.getColumnName());
            }
            
            // sql and jdbc type
            if(override.getSQLType() != null) {
               properties[i].setSQLType(override.getSQLType());
               properties[i].setJDBCType(override.getJDBCType());
            }
         }   
      }
      
      // did we find all overriden properties
      if(overrides.size() > 0) {
         String propertyName = (String)overrides.keySet().iterator().next();
         throw new EJBException("Property " + propertyName + " in field " + cmpField.getFieldName() +
               " is not a property of value object " + cmpField.getFieldType().getName());
      }
      
      // return the new complex type
      return new JDBCTypeComplex(properties, cmpField.getFieldType());      
   }

   protected ArrayList createComplexProperties(JDBCValueClassMetaData valueClass, PropertyStack propertyStack) {
      ArrayList properties = new ArrayList();
      
      JDBCValuePropertyMetaData propertyMetaData;
      Iterator iterator = valueClass.getProperties().iterator();
      while(iterator.hasNext()) {
         propertyMetaData = (JDBCValuePropertyMetaData) iterator.next();
         properties.addAll(createComplexProperties(propertyMetaData, propertyStack));
      }
      return properties;
   }

   protected ArrayList createComplexProperties(JDBCValuePropertyMetaData propertyMetaData, PropertyStack propertyStack) {
      // push my data 
      propertyStack.pushPropertyMetaData(propertyMetaData);

      ArrayList properties = new ArrayList();
      
      Class javaType = propertyMetaData.getPropertyType();      
      if(!valueClasses.containsKey(javaType)) {
         
         // this is a simple property         
         JDBCTypeComplexProperty property = new JDBCTypeComplexProperty();
         property.setJavaType(javaType);
         property.setPropertyName(propertyStack.getPropertyName());
         property.setColumnName(propertyStack.getColumnName());
         property.setGetters(propertyStack.getGetters());
         property.setSetters(propertyStack.getSetters());

         if(propertyMetaData.getSqlType() != null) {
            property.setSQLType(propertyMetaData.getSqlType());
            property.setJDBCType(propertyMetaData.getJDBCType());
         } else {
            // get jdbcType and sqlType from typeMapping
            property.setSQLType(typeMapping.getSqlTypeForJavaType(javaType));
            property.setJDBCType(typeMapping.getJdbcTypeForJavaType(javaType));
         }
      
         properties.add(property);
         
      } else {
         
         // this property is a value object recures
         JDBCValueClassMetaData valueClass = (JDBCValueClassMetaData)valueClasses.get(javaType);
         properties.addAll(createComplexProperties(valueClass, propertyStack));
         
      }
      
      // pop my data 
      propertyStack.popPropertyMetaData();
      
      return properties;
   }
   
   private static class PropertyStack {
      ArrayList properties = new ArrayList();
      ArrayList propertyNames = new ArrayList();
      ArrayList columnNames = new ArrayList();
      ArrayList getters = new ArrayList();
      ArrayList setters = new ArrayList();
      
      public PropertyStack(JDBCCMPFieldMetaData cmpField) {
         columnNames.add(cmpField.getColumnName());
      }
   
      public void pushPropertyMetaData(JDBCValuePropertyMetaData propertyMetaData) {
         propertyNames.add(propertyMetaData.getPropertyName());
         columnNames.add(propertyMetaData.getColumnName());
         getters.add(propertyMetaData.getGetter());
         setters.add(propertyMetaData.getSetter());

         if(properties.contains(propertyMetaData)) {
            throw new EJBException("Circular reference discoverd at property: " + getPropertyName());
         }
         properties.add(propertyMetaData);
      }

      public void popPropertyMetaData() {
         propertyNames.remove(propertyNames.size()-1);
         columnNames.remove(columnNames.size()-1);
         getters.remove(getters.size()-1);
         setters.remove(setters.size()-1);
         
         properties.remove(properties.size()-1);
      }

      public String getPropertyName() {
         StringBuffer buf = new StringBuffer();
         for(int i=0; i<propertyNames.size(); i++) {
            if(i>0) {
               buf.append(".");
            }
            buf.append((String)propertyNames.get(i));
         }
         return buf.toString();
      }
      
      public String getColumnName() {
         StringBuffer buf = new StringBuffer();
         for(int i=0; i<columnNames.size(); i++) {
            if(i>0) {
               buf.append("_");
            }
            buf.append((String)columnNames.get(i));
         }
         return buf.toString();
      }
      
      public Method[] getGetters() {
         return (Method[])getters.toArray(new Method[getters.size()]);
      }
      
      public Method[] getSetters() {
         return (Method[])setters.toArray(new Method[setters.size()]);
      }
   }
}
