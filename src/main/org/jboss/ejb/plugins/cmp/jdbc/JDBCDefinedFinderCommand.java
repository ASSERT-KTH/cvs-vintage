/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.ArrayList;
import java.util.StringTokenizer;

import java.sql.PreparedStatement;

import org.jboss.ejb.DeploymentException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCDeclaredQueryMetaData;

/**
 * JDBCDefinedFinderCommand finds entities based on an xml sql specification.
 * This class needs more work and I will clean it up in CMP 2.x phase 3.
 * The only thing to to note is the seperation of query into a from and where
 * clause. This code has been cleaned up to improve readability.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.4 $
 */
public class JDBCDefinedFinderCommand extends JDBCFinderCommand {
   
   private JDBCDeclaredQueryMetaData metadata;
   private int[] parameterArray;

   public JDBCDefinedFinderCommand(JDBCStoreManager manager, 
         JDBCQueryMetaData q) throws DeploymentException {

      super(manager, q);

      metadata = (JDBCDeclaredQueryMetaData) q;
      
      initSelectObject();

      String sql = buildSQL();

      setSQL(parseParameters(sql));

    }
 
   protected void setParameters(PreparedStatement ps, Object argOrArgs) 
         throws Exception {

      Object[] args = (Object[])argOrArgs;
   
      for(int i = 0; i < parameterArray.length; i++) {
         Object arg = args[parameterArray[i]];
         int jdbcType = manager.getJDBCTypeFactory().getJDBCTypeForJavaType(
               arg.getClass());
         JDBCUtil.setParameter(log, ps, i+1, jdbcType, arg);
      }
   }
   
   /**
    * Initializes the entity or field to be selected.
    * @throws DeploymentException if the specified object is invalid or
    *    non-existant
    */
   private void initSelectObject() throws DeploymentException {
      if(metadata.getEJBName() != null) {
         Container c = manager.getContainer().getApplication().getContainer(
               metadata.getEJBName());


         if( !(c instanceof EntityContainer)) {
            throw new DeploymentException("declared sql can only select an " +
                  "entity bean");
         }      
         EntityContainer entityContainer = (EntityContainer) c;
      
         // check that the entity is managed by a CMPPersistenceManager
         if( !(entityContainer.getPersistenceManager() instanceof 
                  CMPPersistenceManager)) {
            throw new DeploymentException("declared-sql can only select " +
                  "cmp entities.");
         }                  
         CMPPersistenceManager cmpPM = (CMPPersistenceManager)
               entityContainer.getPersistenceManager();
         if( !(cmpPM.getPersistenceStore() instanceof JDBCStoreManager)) {
            throw new DeploymentException("declared-sql can only select " +
                  "a JDBC cmp entity.");
         }
         JDBCStoreManager storeManager = (JDBCStoreManager)
               cmpPM.getPersistenceStore();
      
         if(storeManager.getEntityBridge() == null) {
             throw new DeploymentException("The entity to select has not be " +
                   "properly initialized");
         }
         
         if(metadata.getFieldName() != null) {
            selectEntity = null;
            selectCMPField = storeManager.getEntityBridge()
                  .getCMPFieldByName(metadata.getFieldName());
            if(selectCMPField == null) {
               throw new DeploymentException("Unknown cmp field: " +
                     metadata.getFieldName());
            }
         } else {
            selectEntity = storeManager.getEntityBridge();
         }
      }
   }
   
   /**
    * Builds the sql statement based on the delcared-sql metadata specification.
    * @return the sql statement for this query
    */
   private String buildSQL() {
      StringBuffer sql = new StringBuffer();

      sql.append("SELECT ");
      if(metadata.isSelectDistinct()) {
         sql.append("DISTINCT ");
      }
      
      String from = metadata.getFrom();
      if(from != null && from.trim().length()>0) {
         if(selectCMPField == null) {
            sql.append(SQLUtil.getColumnNamesClause(
                  selectEntity.getJDBCPrimaryKeyFields(), 
                  selectEntity.getMetaData().getTableName()));
            sql.append(" FROM ");
            sql.append(selectEntity.getMetaData().getTableName());
         } else {
            sql.append(SQLUtil.getColumnNamesClause(
                  selectCMPField,
                  selectCMPField.getMetaData().getEntity().getTableName()));
            sql.append(" FROM ");
            sql.append(selectCMPField.getMetaData().getEntity().getTableName());
         }

         sql.append(" ").append(from);
      } else {
         if(selectCMPField == null) {
            sql.append(SQLUtil.getColumnNamesClause(
                  selectEntity.getJDBCPrimaryKeyFields()));
            sql.append(" FROM ");
            sql.append(selectEntity.getMetaData().getTableName());
         } else {
            sql.append(SQLUtil.getColumnNamesClause(selectCMPField));
            sql.append(" FROM ");
            sql.append(selectCMPField.getMetaData().getEntity().getTableName());
         }
      }
      
      String where = metadata.getWhere();
      if(where != null && where.trim().length() > 0) {
         sql.append(" WHERE ").append(where);
      }
      
      String order = metadata.getOrder();
      if(order != null && order.trim().length() > 0) {
         sql.append(" ORDER BY ").append(order);
      }

      String other = metadata.getOther();
      if(other != null && other.trim().length() > 0) {
         sql.append(other);
      }
      return sql.toString();
   }

   /**
    * Replaces the parameters in the specifiec sql with question marks, and 
    * initializes the parameter setting code. Parameters are encoded in curly
    * brackets use a zero based index.
    * @param sql the sql statement that is parsed for parameters
    * @return the original sql statement with the parameters replaced with a 
    *    question mark
    * @throws DeploymentException if a error occures while parsing the sql
    */
   private String parseParameters(String sql) throws DeploymentException {
      StringBuffer sqlBuf = new StringBuffer();
      ArrayList parameters = new ArrayList();      
      
      // Replace placeholders {0} with ?
      if(sql != null) {
         sql = sql.trim();

         StringTokenizer tokens = new StringTokenizer(sql,"{}", true);
         while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if(token.equals("{")) {
               
               token = tokens.nextToken();
               try {
                  Integer parameterIndex = new Integer(token);
                  
                  // of if we are here we can assume that we have 
                  // a parameter and not a function
                  sqlBuf.append("?");
                  parameters.add(parameterIndex);
                  
                  if(!tokens.nextToken().equals("}")) {
                     throw new DeploymentException("Invalid parameter - " +
                           "missing closing '}' : " + sql);
                  }
               } catch(NumberFormatException e) {
                  // ok we don't have a parameter, we have a function
                  // push the tokens on the buffer and continue
                  sqlBuf.append("{").append(token);                  
               }   
            } else {
               // not parameter... just append it
               sqlBuf.append(token);
            }
         }
      }

      // save out the parameter order
      parameterArray = new int[parameters.size()];
      for(int i=0; i<parameterArray.length; i++) {
         parameterArray[i] = ((Integer)parameters.get(i)).intValue();
      }
      
      return sqlBuf.toString().trim();
   }
}
