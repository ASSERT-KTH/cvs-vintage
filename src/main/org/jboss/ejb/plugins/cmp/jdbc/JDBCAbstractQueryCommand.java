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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import java.sql.Connection;   
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.FinderException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.logging.Logger;

/**
 * Abstract superclass of finder commands that return collections.
 * Provides the handleResult() implementation that these all need.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.15 $
 */
public abstract class JDBCAbstractQueryCommand implements JDBCQueryCommand {
   private JDBCStoreManager manager;
   private JDBCQueryMetaData queryMetaData;
   private Logger log;

   private JDBCStoreManager selectManager;
   private JDBCEntityBridge selectEntity;
   private JDBCCMPFieldBridge selectField;
   private List preloadFields = new ArrayList(0);
   private String sql;
   private List parameters = new ArrayList(0);

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
      setSelectEntity(manager.getEntityBridge());
   }

   public Collection execute(
         Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws FinderException {

      PrefetchCache selectPrefetchCache = null;
      if(selectEntity != null) {
         selectPrefetchCache = selectManager.getPrefetchCache();
      }

      List results = new ArrayList();

      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = manager.getEntityBridge().getDataSource().getConnection();

         // create the statement
         if(log.isDebugEnabled()) {
            log.debug("Executing SQL: " + sql);
         }  
         ps = con.prepareStatement(sql);

         // Set the fetch size of the statement
         if(manager.getEntityBridge().getFetchSize() > 0) {
            ps.setFetchSize(manager.getEntityBridge().getFetchSize());
         }

         // set the parameters
         bindParameters(ps, args);

         // execute statement
         ResultSet rs = ps.executeQuery();

         // load the results
         if(selectEntity != null) {
            Object[] ref = new Object[1];
            
            while(rs.next()) {
               int index = 1;
               ref[0] = null;
   
               // get the pk
               index = selectEntity.loadPrimaryKeyResults(rs, index, ref);
               Object pk = ref[0];
               results.add(ref[0]);

               // read the preload fields
               for(Iterator iter=preloadFields.iterator(); iter.hasNext();) {
                  JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
                  ref[0] = null;

                  // read the value and store it in the readahead cache
                  index = field.loadArgumentResults(rs, index, ref);
                  selectPrefetchCache.addPrefetchData(pk, field, ref[0]);
               }
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
         log.debug("Find failed", e);
         throw new FinderException("Find failed: " + e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // If we were just selecting a field, we're done.
      if(selectField != null) {
         return results;
      }

      // add the results list to the cache
      JDBCReadAheadMetaData readAhead = queryMetaData.getReadAhead();
      selectPrefetchCache.addFinderResults(results, readAhead);

      // If this is a finder, we're done.
      if(queryMetaData.getMethod().getName().startsWith("find")) {
         return results;
      }

      // This is an ejbSelect, so we need to convert the pks to real ejbs.
      EntityContainer selectContainer = selectManager.getContainer();
      if(queryMetaData.isResultTypeMappingLocal()) {
         LocalProxyFactory localFactory;
         localFactory = selectContainer.getLocalProxyFactory();

         return localFactory.getEntityLocalCollection(results);
      } else {
         EJBProxyFactory factory;
         factory = selectContainer.getProxyFactory();
         return factory.getEntityCollection(results);
      }
   }

   protected void bindParameters(PreparedStatement ps, Object[] args) throws Exception
   {
      for(int i=0; i<parameters.size(); i++) {
         QueryParameter parameter = (QueryParameter)parameters.get(i);
         parameter.set(log, ps, i+1, args);
      }
   }

   protected Logger getLog() {
      return log;
   }

   protected void setSQL(String sql) {
      this.sql = sql;
      if(log.isDebugEnabled()) {
         log.debug("SQL: " + sql);
      }
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
      this.selectManager = selectEntity.getManager();
   }
   
   protected JDBCCMPFieldBridge getSelectField() {
      return selectField;
   }

   protected void setSelectField(JDBCCMPFieldBridge selectField) {
      this.selectEntity = null;
      this.selectField = selectField;
      this.selectManager = selectField.getManager();
   }
 
   protected List getPreloadFields() {
      return preloadFields;
   }

   protected void setPreloadFields(List preloadFields) {
      this.preloadFields = preloadFields;
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
                        selectManager,
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
