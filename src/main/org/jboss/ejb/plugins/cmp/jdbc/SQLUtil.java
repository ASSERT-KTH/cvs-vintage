/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;

/**
 * SQLUtil helps with building sql statements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */
public class SQLUtil {
   // =======================================================================
   //  Create Table Columns Clause
   //    columnName0 sqlType0 
   //    [, columnName1 sqlType0 
   //    [, columnName2 sqlType0 [...]]]            
   // =======================================================================
   public static String getCreateTableColumnsClause(List fields) {
      StringBuffer buf = new StringBuffer();

      List types = getJDBCTypes(fields);
      for(Iterator iter = types.iterator(); iter.hasNext();) {
         JDBCType type = (JDBCType)iter.next();
         buf.append(getCreateTableColumnsClause(type));
         if(iter.hasNext()) {
            buf.append(", ");
         }
      }
      return buf.toString();
   }   

   /**
    * Returns columnName0 sqlType0 
    *    [, columnName1 sqlType0 
    *    [, columnName2 sqlType0 [...]]] 
    */
   public static String getCreateTableColumnsClause(JDBCFieldBridge field) {
      return getCreateTableColumnsClause(field.getJDBCType());
   }

   /**
    * Returns columnName0 sqlType0 
    *    [, columnName1 sqlType0 
    *    [, columnName2 sqlType0 [...]]] 
    */
   public static String getCreateTableColumnsClause(JDBCType type) {
      String[] columnNames = type.getColumnNames();
      String[] sqlTypes = type.getSQLTypes();

      StringBuffer buf = new StringBuffer();
      for(int i=0; i<columnNames.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(columnNames[i]).append(" ").append(sqlTypes[i]);
      }
      return buf.toString();
   }

   // =======================================================================
   //  Column Names Clause
   //    columnName0 [, columnName1 [AND columnName2 [...]]]            
   // =======================================================================

   /**
    * Returns columnName0 [, columnName1 [AND columnName2 [...]]] 
    */
   public static String getColumnNamesClause(List fields) {
      return getColumnNamesClause(fields, "");
   }
   
   /**
    * Returns columnName0 [, columnName1 [AND columnName2 [...]]] 
    */
   public static String getColumnNamesClause(List fields, String identifier) {
      StringBuffer buf = new StringBuffer();

      List types = getJDBCTypes(fields);
      for(Iterator iter = types.iterator(); iter.hasNext();) {
         JDBCType type = (JDBCType)iter.next();
         buf.append(getColumnNamesClause(type, identifier));
         if(iter.hasNext()) {
            buf.append(", ");
         }
      }
      return buf.toString();
   }   
 
   /**
    * Returns columnName0 [, columnName1 [, columnName2 [...]]] 
    */ 
   public static String getColumnNamesClause(JDBCFieldBridge field) {
      return getColumnNamesClause(field, "");
   }

   /**
    * Returns identifier.columnName0 
    *    [, identifier.columnName1 
    *    [, identifier.columnName2 [...]]] 
    */ 
   public static String getColumnNamesClause(
         JDBCFieldBridge field, String identifier) {

      return getColumnNamesClause(field.getJDBCType(), identifier);
   }
      
   /**
    * Returns identifier.columnName0 
    *    [, identifier.columnName1 
    *    [, identifier.columnName2 [...]]] 
    */ 
   public static String getColumnNamesClause(JDBCType type, String identifier) {
      if(identifier.length() > 0) {
         identifier += ".";
      }

      String[] columnNames = type.getColumnNames();
      
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<columnNames.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(identifier).append(columnNames[i]);
      }
      return buf.toString();
   }
   
   // =======================================================================
   //  Set Clause
   //    columnName0=? [, columnName1=? [, columnName2=? [...]]]           
   // =======================================================================

   /**
    * Returns columnName0=? [, columnName1=? [, columnName2=? [...]]] 
    */ 
   public static String getSetClause(List fields) {
      StringBuffer buf = new StringBuffer();

      List types = getJDBCTypes(fields);
      for(Iterator iter = types.iterator(); iter.hasNext();) {
         JDBCType type = (JDBCType)iter.next();
         buf.append(getSetClause(type));
         if(iter.hasNext()) {
            buf.append(", ");
         }
      }
      return buf.toString();
   }

   /**
    * Returns columnName0=? [, columnName1=? [, columnName2=? [...]]] 
    */ 
   public static String getSetClause(JDBCFieldBridge field) {
      return getSetClause(field.getJDBCType());
   }
      
   /**
    * Returns columnName0=? [, columnName1=? [, columnName2=? [...]]] 
    */ 
   public static String getSetClause(JDBCType type) {
      String[] columnNames = type.getColumnNames();

      StringBuffer buf = new StringBuffer();
      for(int i=0; i<columnNames.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(columnNames[i]).append("=?");
      }
      return buf.toString();
   }
   
   // =======================================================================
   //  Values Clause
   //    ? [, ? [, ? [...]]]           
   // =======================================================================

   /**
    * Returns ? [, ? [, ? [...]]] 
    */
   public static String getValuesClause(List fields) {
      StringBuffer buf = new StringBuffer();

      List types = getJDBCTypes(fields);
      for(Iterator iter = types.iterator(); iter.hasNext();) {
         JDBCType type = (JDBCType)iter.next();
         buf.append(getValuesClause(type));
         if(iter.hasNext()) {
            buf.append(", ");
         }
      }
      return buf.toString();
   }   
 
   /**
    * Returns ? [, ? [, ? [...]]] 
    */
   public static String getValuesClause(JDBCFieldBridge field) {
      return getValuesClause(field.getJDBCType());
   }

   /**
    * Returns ? [, ? [, ? [...]]] 
    */
   public static String getValuesClause(JDBCType type) {
      int columnCount = type.getColumnNames().length;

      StringBuffer buf = new StringBuffer();
      for(int i=0; i<columnCount; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append("?");
      }
      return buf.toString();
   }   
         
   // =======================================================================
   //  Where Clause
   //    columnName0=? [AND columnName1=? [AND columnName2=? [...]]]           
   // =======================================================================
   
   /**
    * Returns columnName0=? [AND columnName1=? [AND columnName2=? [...]]] 
    */
   public static String getWhereClause(List fields) {
      return getWhereClause(fields, "");
   }   

   /**
    * Returns identifier.columnName0=? 
    *    [AND identifier.columnName1=? 
    *    [AND identifier.columnName2=? [...]]] 
    */
   public static String getWhereClause(List fields, String identifier) {
      StringBuffer buf = new StringBuffer();

      List types = getJDBCTypes(fields);
      for(Iterator iter = types.iterator(); iter.hasNext();) {
         JDBCType type = (JDBCType)iter.next();
         buf.append(getWhereClause(type, identifier));
         if(iter.hasNext()) {
            buf.append(" AND ");
         }
      }
      return buf.toString();
   }   
 
   /**
    * Returns columnName0=? [AND columnName1=? [AND columnName2=? [...]]] 
    */
   public static String getWhereClause(JDBCFieldBridge field) {
      return getWhereClause(field, "");
   }

   /**
    * Returns identifier.columnName0=? 
    *    [AND identifier.columnName1=? 
    *    [AND identifier.columnName2=? [...]]] 
    */
   public static String getWhereClause(
         JDBCFieldBridge field, String identifier) {

      return getWhereClause(field.getJDBCType(), identifier);
   }

   /**
    * Returns identifier.columnName0=? 
    *    [AND identifier.columnName1=? 
    *    [AND identifier.columnName2=? [...]]] 
    */
   public static String getWhereClause(JDBCType type, String identifier) {
      if(identifier.length() > 0) {
         identifier += ".";
      }

      String[] columnNames = type.getColumnNames();

      StringBuffer buf = new StringBuffer();
      for(int i=0; i<columnNames.length; i++) {
         if(i!=0) {
            buf.append(" AND ");
         }
         buf.append(identifier).append(columnNames[i]).append("=?");
      }
      return buf.toString();
   }   

   /**
    * Returns parent.pkColumnName0=child.fkColumnName0 
    *    [AND parent.pkColumnName1=child.fkColumnName1 
    *    [AND parent.pkColumnName2=child.fkColumnName2 [...]]] 
    */
   public static String getJoinClause(
         JDBCFieldBridge pkField, String parent, 
         JDBCFieldBridge fkField, String child) {

      return getJoinClause(
            pkField.getJDBCType(), parent,
            fkField.getJDBCType(), child);
   }

   public static String getJoinClause(
         List pkFields, String parent, List fkFields, String child) {

      if(pkFields.size() != fkFields.size()) {
         throw new IllegalArgumentException(
               "Error createing theta join clause:" +
               " pkField.size()="+pkFields.size() +
               " fkField.size()="+fkFields.size());
      }

      StringBuffer buf = new StringBuffer();

      List pkTypes = getJDBCTypes(pkFields);
      List fkTypes = getJDBCTypes(fkFields);
      Iterator fkIter = fkTypes.iterator();
      for(Iterator pkIter = pkTypes.iterator(); pkIter.hasNext();) {
         JDBCType pkType = (JDBCType)pkIter.next();
         JDBCType fkType = (JDBCType)fkIter.next();

         buf.append(getJoinClause(pkType, parent, fkType, child));
         if(pkIter.hasNext()) {
            buf.append(" AND ");
         }
      }
      return buf.toString();
   }
 
   /**
    * Returns parent.pkColumnName0=child.fkColumnName0 
    *    [AND parent.pkColumnName1=child.fkColumnName1 
    *    [AND parent.pkColumnName2=child.fkColumnName2 [...]]] 
    */
   public static String getJoinClause(
         JDBCType pkType, String parent, JDBCType fkType, String child) {
      if(parent.length() > 0) {
         parent += ".";
      }
      if(child.length() > 0) {
         child += ".";
      }

      String[] pkColumnNames = pkType.getColumnNames();
      String[] fkColumnNames = fkType.getColumnNames();
      if(pkColumnNames.length != fkColumnNames.length) {
         throw new IllegalArgumentException(
               "PK and FK have different number of columns");
      }

      StringBuffer buf = new StringBuffer();
      for(int i=0; i<pkColumnNames.length; i++) {
         if(i!=0) {
            buf.append(" AND ");
         }
         buf.append(parent).append(pkColumnNames[i]);
         buf.append("=");
         buf.append(child).append(fkColumnNames[i]);
      }
      return buf.toString();
   }   

   public static String getSelfCompareWhereClause(
         List fields, String fromIdentifier, String toIdentifier) {
      
      StringBuffer buf = new StringBuffer();

      List types = getJDBCTypes(fields);
      for(Iterator iter = types.iterator(); iter.hasNext();) {
         JDBCType type = (JDBCType)iter.next();
         buf.append(getSelfCompareWhereClause(
                  type, fromIdentifier, toIdentifier));
         if(iter.hasNext()) {
            buf.append(" AND ");
         }
      }
      return buf.toString();
   }   
 
   public static String getSelfCompareWhereClause(
         JDBCType type, String fromIdentifier, String toIdentifier) {

      if(fromIdentifier.length() > 0) {
         fromIdentifier += ".";
      }
      if(toIdentifier.length() > 0) {
         toIdentifier += ".";
      }
      
      String[] columnNames = type.getColumnNames();

      StringBuffer buf = new StringBuffer();
      for(int i=0; i<columnNames.length; i++) {
         if(i!=0) {
            buf.append(" AND ");
         }
         buf.append(fromIdentifier).append(columnNames[i]);
         buf.append(" = ");
         buf.append(toIdentifier).append(columnNames[i]);
      }
      return buf.toString();
   }   

   private static List getJDBCTypes(List fields) {
      ArrayList types = new ArrayList(fields.size());

      for(Iterator iter = fields.iterator(); iter.hasNext();) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         JDBCType type = field.getJDBCType();
         if(type != null && type.getColumnNames().length > 0) {
            types.add(type);
         }
      }
      return Collections.unmodifiableList(types);
   }
}
