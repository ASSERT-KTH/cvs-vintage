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
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.ejb.FinderException;

import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.LocalContainerInvoker;
import org.jboss.ejb.DeploymentException;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;
import org.jboss.logging.Logger;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge; 
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
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
 * @version $Revision: 1.10 $
 */
public abstract class JDBCFinderCommand
   extends JDBCQueryCommand
   implements FindEntitiesCommand
{
   private List parameters = new ArrayList();
   protected JDBCQueryMetaData queryMetaData;
   protected JDBCEntityBridge selectEntity;
   protected JDBCCMPFieldBridge selectCMPField;

   // Constructors --------------------------------------------------

   public JDBCFinderCommand(JDBCStoreManager manager, JDBCQueryMetaData q) {
      super(manager, q.getMethod().getName());

      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName() +
            "." + 
            q.getMethod().getName());

      queryMetaData = q;
      selectEntity = entity;
   }

   public JDBCQueryMetaData getQueryMetaData() {
      return queryMetaData;
   }

   // FindEntitiesCommand implementation -------------------------

   public FinderResults execute(Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      FinderResults result = null;

      try {
         //
         // Execute the find... will return a collection of pks
         Collection keys = (Collection)jdbcExecute(args);
         boolean readAheadOnLoad = queryMetaData.getReadAhead().isOnLoadUsed();

         result = new FinderResults(keys, null, null, null, readAheadOnLoad);
         if (readAheadOnLoad) {
            // add to the cache
            manager.getReadAheadCache().insert(
                  new Long(result.getListId()), result);
         }
      } catch (Exception e) {
         log.error("Find error: " + e);
         throw new FinderException("Find failed");
      }
      return result;
   }

   // JDBCQueryCommand overrides ------------------------------------
   protected List getParameters() {
      return Collections.unmodifiableList(parameters);
   }

   protected void setParameters(List p) {
      for(int i=0; i<p.size(); i++) {
         if( !(p.get(i) instanceof QueryParameter)) {
            throw new IllegalArgumentException("Element " + i + " of list " +
                  "is not an instance of QueryParameter, but " + 
                  p.get(i).getClass().getName());
         }
      }
      parameters = new ArrayList(p);
   }
 
   protected void setParameters(PreparedStatement ps, Object argOrArgs) 
         throws Exception {

      Object[] args = (Object[])argOrArgs;

      for(int i=0; i<parameters.size(); i++) {
         QueryParameter parameter = (QueryParameter)parameters.get(i);
         parameter.set(log, ps, i+1, args);
      }
   }
   
   protected Object handleResult(ResultSet rs, Object argOrArgs)
         throws Exception {
      
      Collection result = new ArrayList();   
      try {
         // are we selecting an entity (or just a field)
         if(selectEntity != null) {
            
            // load the pks into the result list
            Object[] pkRef = new Object[1];
            while(rs.next()) {
               pkRef[0] = null;
               selectEntity.loadPrimaryKeyResults(rs, 1, pkRef);
               result.add(pkRef[0]);
            }
            
            // is this an ejb select command
            if(queryMetaData.getMethod().getName().startsWith("ejbSelect")) {
               // convert the list of pks into real ejbs
               EntityContainer container = manager.getContainer();
               if(queryMetaData.isResultTypeMappingLocal()) {
                  LocalContainerInvoker localInvoker;
                  localInvoker = container.getLocalContainerInvoker();
                  result = localInvoker.getEntityLocalCollection(result);
               } else {
                  ContainerInvoker invoker;
                  invoker = container.getContainerInvoker();
                  result = invoker.getEntityCollection(result);
               }
            }
         } else {
            // this is a select for field
            Object[] valueRef = new Object[1];
            while(rs.next()) {
               valueRef[0] = null;
               selectCMPField.loadArgumentResults(rs, 1, valueRef);
               result.add(valueRef[0]);
            }
         }   
      } catch(Exception e) {
         throw new ServerException("Finder failed: ", e);
      }

      return result;
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
