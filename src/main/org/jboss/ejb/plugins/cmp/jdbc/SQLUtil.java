/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * SQLUtil helps with building sql statements.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public class SQLUtil {
   // =======================================================================
   //  Create Table Columns Clause
   //    columnName0 sqlType0 [, columnName1 sqlType0 [, columnName2 sqlType0 [...]]]            
   // =======================================================================

   /**
   * Returns columnName0 [, columnName1 [AND columnName2 [...]]] 
   */
   public static String getCreateTableColumnsClause(JDBCCMPFieldBridge[] fields) {
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<fields.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(getCreateTableColumnsClause(fields[i].getJDBCType()));
      }
      return buf.toString();
   }   
         
   /**
   * Returns columnName0 sqlType0 [, columnName1 sqlType0 [, columnName2 sqlType0 [...]]] 
   */
   public static String getCreateTableColumnsClause(JDBCCMPFieldBridge field) {
      return getCreateTableColumnsClause(field.getJDBCType());
   }

   /**
   * Returns columnName0 sqlType0 [, columnName1 sqlType0 [, columnName2 sqlType0 [...]]] 
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
   public static String getColumnNamesClause(JDBCCMPFieldBridge[] fields) {
      return getColumnNamesClause(fields, "");
   }
   
   /**
   * Returns columnName0 [, columnName1 [AND columnName2 [...]]] 
   */
   public static String getColumnNamesClause(JDBCCMPFieldBridge[] fields, String identifier) {
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<fields.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(getColumnNamesClause(fields[i].getJDBCType(), identifier));
      }
      return buf.toString();
   }   
         
   /**
   * Returns columnName0 [, columnName1 [, columnName2 [...]]] 
   */ 
   public static String getColumnNamesClause(JDBCCMPFieldBridge field) {
      return getColumnNamesClause(field, "");
   }

   /**
   * Returns identifier.columnName0 [, identifier.columnName1 [, identifier.columnName2 [...]]] 
   */ 
   public static String getColumnNamesClause(JDBCCMPFieldBridge field, String identifier) {
      return getColumnNamesClause(field.getJDBCType(), identifier);
   }
      
   /**
   * Returns identifier.columnName0 [, identifier.columnName1 [, identifier.columnName2 [...]]] 
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
   public static String getSetClause(JDBCCMPFieldBridge[] fields) {
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<fields.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(getSetClause(fields[i].getJDBCType()));
      }
      return buf.toString();
   }

   /**
   * Returns columnName0=? [, columnName1=? [, columnName2=? [...]]] 
   */ 
   public static String getSetClause(JDBCCMPFieldBridge field) {
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
   public static String getValuesClause(JDBCCMPFieldBridge[] fields) {
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<fields.length; i++) {
         if(i!=0) {
            buf.append(", ");
         }
         buf.append(getValuesClause(fields[i].getJDBCType()));
      }
      return buf.toString();
   }   
         
   /**
   * Returns ? [, ? [, ? [...]]] 
   */
   public static String getValuesClause(JDBCCMPFieldBridge field) {
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
   public static String getWhereClause(JDBCCMPFieldBridge[] fields) {
      return getWhereClause(fields, "");
   }   

   /**
   * Returns identifier.columnName0=? [AND identifier.columnName1=? [AND identifier.columnName2=? [...]]] 
   */
   public static String getWhereClause(JDBCCMPFieldBridge[] fields, String identifier) {
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<fields.length; i++) {
         if(i!=0) {
            buf.append(" AND ");
         }
         buf.append(getWhereClause(fields[i].getJDBCType(), identifier));
      }
      return buf.toString();
   }   
         
   /**
   * Returns columnName0=? [AND columnName1=? [AND columnName2=? [...]]] 
   */
   public static String getWhereClause(JDBCCMPFieldBridge field) {
      return getWhereClause(field, "");
   }

   /**
   * Returns identifier.columnName0=? [AND identifier.columnName1=? [AND identifier.columnName2=? [...]]] 
   */
   public static String getWhereClause(JDBCCMPFieldBridge field, String identifier) {
      return getWhereClause(field.getJDBCType(), identifier);
   }

   /**
   * Returns identifier.columnName0=? [AND identifier.columnName1=? [AND identifier.columnName2=? [...]]] 
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
   * Returns parent.pkColumnName0=child.fkColumnName0 [AND parent.pkColumnName1=child.fkColumnName1 [AND parent.pkColumnName2=child.fkColumnName2 [...]]] 
   */
   public static String getWhereClause(JDBCCMPFieldBridge pkField, String parent, JDBCCMPFieldBridge fkField, String child) {
      return getWhereClause(pkField.getJDBCType(), parent, fkField.getJDBCType(), child);
   }

   /**
   * Returns parent.pkColumnName0=child.fkColumnName0 [AND parent.pkColumnName1=child.fkColumnName1 [AND parent.pkColumnName2=child.fkColumnName2 [...]]] 
   */
   public static String getWhereClause(JDBCType pkType, String parent, JDBCType fkType, String child) {
      if(parent.length() > 0) {
         parent += ".";
      }
      if(child.length() > 0) {
         child += ".";
      }

      String[] pkColumnNames = pkType.getColumnNames();
      String[] fkColumnNames = fkType.getColumnNames();
      if(pkColumnNames.length != fkColumnNames.length) {
         throw new IllegalArgumentException("PK and FK have different number of columns");
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

   public static String getSelfCompareWhereClause(JDBCCMPFieldBridge[] fields, String fromIdentifier, String toIdentifier) {
      
      StringBuffer buf = new StringBuffer();
      for(int i=0; i<fields.length; i++) {
         if(i!=0) {
            buf.append(" AND ");
         }
         buf.append(getSelfCompareWhereClause(fields[i].getJDBCType(), fromIdentifier, toIdentifier));
      }
      return buf.toString();
   }   
         
   public static String getSelfCompareWhereClause(JDBCType type, String fromIdentifier, String toIdentifier) {
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
}
