package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.Iterator;
import java.util.Set;
import org.jboss.ejb.plugins.cmp.jdbc.SQLUtil;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;

public class SQLGenerator {
   private IdentifierManager idManager;
   
   public SQLGenerator(IdentifierManager idManager) {
      this.idManager = idManager;
   }

   public String getSQL(boolean isSelectDistinct, String selectPath, String userWhereClause) {
      String selectClause = getSelectClause(isSelectDistinct, selectPath);
      String fromClause = getFromClause();
      String whereClause = getWhereClause(userWhereClause);
      
      StringBuffer buf = new StringBuffer(selectClause.length()+fromClause.length()+whereClause.length()+2);
      buf.append(selectClause);
      buf.append(" ");
      buf.append(fromClause);
      if(whereClause.length()>0) {
         buf.append(" ");
         buf.append(whereClause);
      }
      return buf.toString();
   }
   
   public String getSelectClause(boolean isSelectDistinct, String selectPath) {
      StringBuffer buf = new StringBuffer();

      buf.append("SELECT ");
      if(isSelectDistinct) {
         buf.append("DISTINCT ");
      }
      
      PathElement selectPathElement = idManager.getExistingPathElement(selectPath);
      if(selectPathElement instanceof AbstractSchema) {
         AbstractSchema schema = (AbstractSchema)selectPathElement;
         buf.append(getSelectClause(schema));
      } else if(selectPathElement instanceof CMRField) {
         CMRField cmrField = (CMRField)selectPathElement;
         buf.append(getSelectClause(cmrField));
      } else if(selectPathElement instanceof CMPField) {
         CMPField cmpField = (CMPField)selectPathElement;
         buf.append(getSelectClause(cmpField));
      } else {
         // should never happen
         throw new IllegalStateException("Path element is instance of unknown type: " +
               "selectPath=" + selectPath + " selectPathElement=" + selectPathElement);
      }      
      return buf.toString();
   }

   private String getSelectClause(AbstractSchema schema) {
      String identifier = idManager.getTableAlias(schema);
      return SQLUtil.getColumnNamesClause(schema.getEntityBridge().getJDBCPrimaryKeyFields(), identifier);
   }
   
   private String getSelectClause(CMRField cmrField) {
      String identifier = idManager.getTableAlias(cmrField);
      return SQLUtil.getColumnNamesClause(
            cmrField.getEntityBridge().getJDBCPrimaryKeyFields(), identifier);
   }

   private String getSelectClause(CMPField cmpField) {
      String identifier = idManager.getTableAlias(cmpField.getParent());
      return SQLUtil.getColumnNamesClause(cmpField.getCMPFieldBridge(), identifier);
   }


   public String getFromClause() {
      StringBuffer buf = new StringBuffer();

      buf.append("FROM ");

      for(Iterator i = idManager.getUniqueEntityPathElements().iterator(); i.hasNext(); ) {
         EntityPathElement pathElement = (EntityPathElement)i.next();
         buf.append(getTableDeclarations(pathElement));
         if(i.hasNext()) {
            buf.append(", ");
         }
      }
      
      return buf.toString();
   }   

   public String getTableDeclarations(EntityPathElement pathElement) {
      StringBuffer buf = new StringBuffer();

      buf.append(pathElement.getEntityBridge().getMetaData().getTableName());
      buf.append(" ");
      buf.append(idManager.getTableAlias(pathElement));

      if(pathElement instanceof CMRField) {
         CMRField cmrField = (CMRField)pathElement;
         JDBCRelationMetaData relationMetaData = cmrField.getCMRFieldBridge().getMetaData().getRelationMetaData();
         if(relationMetaData.isTableMappingStyle()) {
            buf.append(", ");
            buf.append(relationMetaData.getTableName());
            buf.append(" ");
            buf.append(idManager.getRelationTableAlias(cmrField));
         }
      }
      
      return buf.toString();
   }

   public String getWhereClause(String whereClause) {
      StringBuffer buf = new StringBuffer();

      Set cmrFields = idManager.getUniqueCMRFields();
      if(whereClause.length() > 0 || cmrFields.size() > 0) {
         buf.append("WHERE ");
         
         if(whereClause.length() > 0) {
            if(cmrFields.size() > 0) {
               buf.append("(");
            }
            buf.append(whereClause);
            if(cmrFields.size() > 0) {
               buf.append(") AND ");
            }
         }

         for(Iterator i = cmrFields.iterator(); i.hasNext(); ) {
            CMRField cmrField = (CMRField)i.next();
            buf.append(getTableWhereClause(cmrField));
            if(i.hasNext()) {
               buf.append(" AND ");
            }
         }
      }
      return buf.toString();
   }

   public String getTableWhereClause(CMRField cmrField) {
      JDBCCMRFieldBridge cmrFieldBridge = cmrField.getCMRFieldBridge();
      EntityPathElement parent = cmrField.getParent();
      String childTableAlias = idManager.getTableAlias(cmrField);
      String parentTableAlias = idManager.getTableAlias(parent);
      
      
      StringBuffer buf = new StringBuffer();
      
      
      if(cmrFieldBridge.getMetaData().getRelationMetaData().isForeignKeyMappingStyle()) {
         
         if(cmrFieldBridge.hasForeignKey()) {            
            JDBCCMPFieldBridge[] parentFkKeyFields = cmrFieldBridge.getForeignKeyFields();
            for(int i=0; i < parentFkKeyFields.length; i++) {
               if(i > 0) {
                  buf.append(" AND ");
               }
               JDBCCMPFieldBridge parentFkField = parentFkKeyFields[i];
               JDBCCMPFieldBridge childPkField = cmrFieldBridge.getRelatedEntity().getCMPFieldByName(parentFkField.getFieldName());
               buf.append(SQLUtil.getWhereClause(parentFkField, parentTableAlias, childPkField, childTableAlias));
            }   
         } else {
            JDBCCMPFieldBridge[] childFkKeyFields = cmrFieldBridge.getRelatedCMRField().getForeignKeyFields();
            for(int i=0; i < childFkKeyFields.length; i++) {
               if(i > 0) {
                  buf.append(" AND ");
               }
               JDBCCMPFieldBridge childFkKeyField = childFkKeyFields[i];
               JDBCCMPFieldBridge parentPkField = parent.getCMPFieldBridge(childFkKeyField.getFieldName());
               buf.append(SQLUtil.getWhereClause(parentPkField, parentTableAlias, childFkKeyField, childTableAlias));
            }   
         }
      } else {
         String relationTableAlias = idManager.getRelationTableAlias(cmrField);

         JDBCCMPFieldBridge[] parentTableKeyFields = cmrFieldBridge.getTableKeyFields();
         for(int i=0; i < parentTableKeyFields.length; i++) {
            if(i > 0) {
               buf.append(" AND ");
            }
            JDBCCMPFieldBridge fkField = parentTableKeyFields[i];
            JDBCCMPFieldBridge pkField = parent.getCMPFieldBridge(fkField.getFieldName());
            buf.append(SQLUtil.getWhereClause(pkField, parentTableAlias, fkField, relationTableAlias));
         }   

         buf.append(" AND ");

         JDBCCMPFieldBridge[] childTableKeyFields = cmrFieldBridge.getRelatedCMRField().getTableKeyFields();
         for(int i=0; i < childTableKeyFields.length; i++) {
            if(i > 0) {
               buf.append(" AND ");
            }
            JDBCCMPFieldBridge fkField = childTableKeyFields[i];
            JDBCCMPFieldBridge pkField = cmrFieldBridge.getRelatedEntity().getCMPFieldByName(fkField.getFieldName());
            buf.append(SQLUtil.getWhereClause(pkField, childTableAlias, fkField, relationTableAlias));
         }   
      }   
      return buf.toString();
   }
}
