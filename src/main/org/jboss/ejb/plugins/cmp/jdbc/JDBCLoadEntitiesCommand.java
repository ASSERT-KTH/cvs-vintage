/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;



import java.rmi.RemoteException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.plugins.cmp.LoadEntitiesCommand;
import org.jboss.util.FinderResults;

/**
 * JDBCLoadEntityCommand 
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.4 $
 */
public class JDBCLoadEntitiesCommand
   extends JDBCLoadEntityCommand
   implements LoadEntitiesCommand
{
   String selectClause;
   // Constructors --------------------------------------------------

   public JDBCLoadEntitiesCommand(JDBCStoreManager manager) {
      super(manager);

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(entity.getJDBCCMPFields()));
      sql.append(" FROM ").append(entityMetaData.getTableName());

      selectClause = sql.toString();
   }

   // LoadEntitiesCommand implementation.
   
   public void execute(FinderResults keys)
      throws RemoteException
   {
/*
      JDBCFinderCommand finder = (JDBCFinderCommand)keys.getFinder();
      
      if(finder != null) {
         FinderMetaData metaData = finder.getFinderMetaData();
         if(metaData != null && metaData.hasReadAhead() && keys.getQueryData() != null) {
            try {
               Object[] args = {keys};
               jdbcExecute(args);
            } catch (Exception e) {
               throw new ServerException("Load failed", e);
            }
         }
      }
*/
   }
   
   // JDBCQueryCommand overrides ------------------------------------

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception {
      FinderResults finderResults = (FinderResults)((Object[])argOrArgs)[0];
      
      // get a map between the primary keys and its context 
/*      Map instances = finderResults.getEntityMap();
      while(rs.next()) {
         int index = 1;

         // load the primary key, from the result set
         Object[] pkRef = new Object[1];
         pkRef[0] = entity.getPrimaryKeyClass().newInstance();         
         index = entity.loadPrimaryKeyResults(rs, index, pkRef);
         
           // get the context for this primary key
         EntityEnterpriseContext ctx = (EntityEnterpriseContext)instances.get(pkRef[0]);
         if (ctx != null) {
            // if the context is not valid load the data.
            if (!ctx.isValid()) {
               entity.injectPrimaryKeyIntoInstance(ctx, pkRef[0]);
               index = entity.loadNonPrimaryKeyResults(rs, index, ctx);
               ctx.setValid(true);
               entity.setClean(ctx);
            }
         } else {
            // if ctx was null, the CMPPersistenceManager doesn't want us to try
            // to pre-load it due to a transaction issue.
         }
      }
*/
      return null;
   }
   
   protected void setParameters(PreparedStatement ps, Object args) throws Exception {
      FinderResults finderResults = (FinderResults)((Object[])args)[0];
      
      // get the original finder
      JDBCFinderCommand finder = (JDBCFinderCommand)finderResults.getFinder();
      
      // get the arguments passed to the original finder
      Object[] queryArgs = finderResults.getQueryArgs();
      
      // delegate to the original finder to set the parameters
      finder.setParameters(ps, queryArgs);
   }
   
   // JDBCommand ovverrides -----------------------------------------
   protected String getSQL(Object args) throws Exception {
      // append the where clause from the original query
      FinderResults finderResults = (FinderResults)((Object[])args)[0];
      return selectClause + " " + finderResults.getQueryData().toString();
   }
   
   // protected -----------------------------------------------------
}
