/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import java.sql.Connection;   
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.FinderException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.LocalContainerInvoker;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.logging.Logger;
import org.jboss.util.FinderResults;

/**
 * Abstract superclass of finder commands that return collections.
 * Provides the handleResult() implementation that these all need.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public abstract class JDBCAbstractQueryCommand implements JDBCQueryCommand {
   private JDBCStoreManager manager;
   private JDBCQueryMetaData queryMetaData;
   private Logger log;

   private JDBCEntityBridge selectEntity;
   private JDBCCMPFieldBridge selectField;
   private String sql;
   private List parameters = new ArrayList();

   public JDBCAbstractQueryCommand(
         JDBCStoreManager manager, JDBCQueryMetaData q) {

      this.manager = manager;
      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName() +
            "." + 
            q.getMethod().getName());

      queryMetaData = q;
      selectEntity = manager.getEntityBridge();
   }

   public Collection execute(
         Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws FinderException {

      Collection results = new ArrayList();

      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = manager.getEntityBridge().getDataSource().getConnection();
         
         // create the statement
         log.debug("Executing SQL: " + sql);
         ps = con.prepareStatement(sql);
         
         // set the parameters
         for(int i=0; i<parameters.size(); i++) {
            QueryParameter parameter = (QueryParameter)parameters.get(i);
            parameter.set(log, ps, i+1, args);
         }

         // execute statement
         ResultSet rs = ps.executeQuery();

         // load the results
         if(selectEntity != null) {
            // load the pks
            Object[] pkRef = new Object[1];
            while(rs.next()) {
               pkRef[0] = null;
               selectEntity.loadPrimaryKeyResults(rs, 1, pkRef);
               results.add(pkRef[0]);
            }
         } else {
            // load the field
            Object[] valueRef = new Object[1];
            while(rs.next()) {
               valueRef[0] = null;
               selectField.loadArgumentResults(rs, 1, valueRef);
               results.add(valueRef[0]);
            }
         }   
      } catch(Exception e) {
         log.debug(e);
         throw new FinderException("Find failed: " + e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // If we were just selecting a field, we're done.
      if(selectField != null) {
         return results;
      }

      // Convert the pk collection into finder results
      boolean readAheadOnLoad = queryMetaData.getReadAhead().isOnLoadUsed();
      FinderResults finderResults = new FinderResults(
            results, null, null, null, readAheadOnLoad);

      // If read ahead is on, store the finder results for optimized loading.
      if(readAheadOnLoad) {
         // add to the cache
         manager.getReadAheadCache().insert(
               new Long(finderResults.getListId()), finderResults);
      }

      // If this is a finder, we're done.
      if(queryMetaData.getMethod().getName().startsWith("find")) {
         return finderResults;
      }

      // This is an ejbSelect, so we need to convert the pks to real ejbs.
      EntityContainer container = manager.getContainer();
      if(queryMetaData.isResultTypeMappingLocal()) {
         JDBCStoreManager selectManager = selectEntity.getManager();

         LocalContainerInvoker localInvoker;
         localInvoker = selectManager.getContainer().getLocalContainerInvoker();

         return localInvoker.getEntityLocalCollection(finderResults);
      } else {
         ContainerInvoker invoker;
         invoker = container.getContainerInvoker();
            return invoker.getEntityCollection(finderResults);
      }
   }
   
   protected Logger getLog() {
      return log;
   }

   protected void setSQL(String sql) {
      this.sql = sql;
      log.debug("SQL: " + sql);
   }

   protected void setParameterList(List p) {
      for(int i=0; i<p.size(); i++) {
         if( !(p.get(i) instanceof QueryParameter)) {
            throw new IllegalArgumentException("Element " + i + " of list " +
                  "is not an instance of QueryParameter, but " + 
                  p.get(i).getClass().getName());
         }
      }
      parameters = new ArrayList(p);
   }
   
   protected JDBCEntityBridge getSelectEntity() {
      return selectEntity;
   }

   protected void setSelectEntity(JDBCEntityBridge selectEntity) {
      this.selectField = null;
      this.selectEntity = selectEntity;
   }
   
   protected JDBCCMPFieldBridge getSelectField() {
      return selectField;
   }

   protected void setSelectField(JDBCCMPFieldBridge selectField) {
      this.selectEntity = null;
      this.selectField = selectField;
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
   protected String parseParameters(String sql) throws DeploymentException {
      StringBuffer sqlBuf = new StringBuffer();
      ArrayList params = new ArrayList();      
      
      // Replace placeholders {0} with ?
      if(sql != null) {
         sql = sql.trim();

         StringTokenizer tokens = new StringTokenizer(sql,"{}", true);
         while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if(token.equals("{")) {
               
               token = tokens.nextToken();
               if(Character.isDigit(token.charAt(0))) {
                  QueryParameter parameter = new QueryParameter(
                        manager,
                        queryMetaData.getMethod(),
                        token);
                  
                  // of if we are here we can assume that we have 
                  // a parameter and not a function
                  sqlBuf.append("?");
                  params.add(parameter);
                     
                  
                  if(!tokens.nextToken().equals("}")) {
                     throw new DeploymentException("Invalid parameter - " +
                           "missing closing '}' : " + sql);
                  }
               } else {
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

      parameters = params;
      
      return sqlBuf.toString().trim();
   }
}
