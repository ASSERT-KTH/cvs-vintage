/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JAWSPersistenceManager;
import org.jboss.ejb.plugins.jaws.JPMLoadEntityCommand;
import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;

/**
 * JAWSPersistenceManager JDBCLoadEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</a>
 * @version $Revision: 1.12 $
 */
public class JDBCLoadEntityCommand
   extends JDBCQueryCommand
   implements JPMLoadEntityCommand
{
   /**what is the position of each cmp field in the generated select statement?
    * this simply maps the position of the field in the CMP list to its position
    * in the generated select statement. This is neccessary because of the variable
    * number of key columns (which are skipped in a load) and because there can
    * be overlap between the two: pkfields and cmpfields are neither disjoint sets
    * nor is the cmpfields a subset of pkfields (not that that makes sense to
    * me right now, but I'll roll with it until I have more chance to analyse - danch)
    */
   int [] cmpFieldPositionInSelect = null;

   /** This const is used in places where I need to add an offset to a count
    *  to account for the fact that JDBC counts from one whilst every other
    *  damn thing in the languase starts at 0, the way God intended!
    */
   private static final int JDBC_WART_OFFSET = 1;   
   // Constructors --------------------------------------------------

   public JDBCLoadEntityCommand(JDBCCommandFactory factory)
   {
      super(factory, "Load");

      String sql = createSelectClause() + " FROM " + jawsEntity.getTableName() 
                   + " WHERE " + getPkColumnWhereList();
      if (jawsEntity.hasSelectForUpdate())
      {
         sql += " FOR UPDATE";
      }

      setSQL(sql);
   }

   protected String createSelectClause() {
      // Select SQL
      String sql = "SELECT ";
      HashMap alreadyListed = new HashMap();
      // put the key fields in first 
      // we'll stash the column names here so that we can later map an overlapped
      // column (overlap between PK and CMP) into its spot in the select statement.
      String[] pkColumnNames = new String[jawsEntity.getNumberOfPkFields()];
      Iterator keyIt = jawsEntity.getPkFields();
      int fieldCount = 0;
      while (keyIt.hasNext())
      {
         PkFieldMetaData pkField = (PkFieldMetaData)keyIt.next();
         
         sql += ((fieldCount==0) ? "" : ",") + 
                jawsEntity.getTableName() + "." + pkField.getColumnName();
         alreadyListed.put(pkField.getColumnName().toUpperCase(), pkField);
         pkColumnNames[fieldCount]=pkField.getColumnName();
         fieldCount++;
      }
      
      cmpFieldPositionInSelect = new int[jawsEntity.getNumberOfCMPFields()];
      Iterator it = jawsEntity.getCMPFields();
      int cmpFieldCount = 0;
      while (it.hasNext())
      {
         CMPFieldMetaData cmpField = (CMPFieldMetaData)it.next();
         if (alreadyListed.get(cmpField.getColumnName().toUpperCase()) == null) {
            sql += "," + jawsEntity.getTableName() + "." + cmpField.getColumnName();
            cmpFieldPositionInSelect[cmpFieldCount] = fieldCount+JDBC_WART_OFFSET;
            fieldCount++;//because this was another field in the select
         } else {
            //DO NOT increment field count, this isn't another in the select.
            //linear search (yech!) of the pkColumnNames - we only do this once per bean, however
            for (int i=0;i<pkColumnNames.length;i++) {
               if (pkColumnNames[i].equalsIgnoreCase(cmpField.getColumnName())) {
                  cmpFieldPositionInSelect[cmpFieldCount] = i+JDBC_WART_OFFSET;
                  break;
               }
            }
            if (cmpFieldPositionInSelect[cmpFieldCount] < 1) {
               log.error("Error: Can't find first occurence of repeated column "+
                         cmpField.getName()+" when building CMP load SQL for "+
                         jawsEntity.getName());
            }
         }
         cmpFieldCount++;
      }
      
      
      
      return sql;
   }
   
   // JPMLoadEntityCommand implementation ---------------------------

   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      if ( !jawsEntity.isReadOnly() || isTimedOut(ctx) )
      {
         try
         {
            //first check to see if the data was preloaded
            Object[] data = factory.getPreloadData(ctx.getId());
            if (data != null) {
               loadFromPreload(data, ctx);
            } else {
               jdbcExecute(ctx);
            }
         } catch (Exception e)
         {
            throw new ServerException("Load failed", e);
         }
      }
   }

   // JDBCQueryCommand overrides ------------------------------------

   protected void setParameters(PreparedStatement stmt, Object argOrArgs)
      throws Exception
   {
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)argOrArgs;

      setPrimaryKeyParameters(stmt, 1, ctx.getId());
   }

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)argOrArgs;

      if (!rs.next())
      {
         throw new NoSuchObjectException("Entity "+ctx.getId()+" not found");
      }

      // Set values
      loadOneEntity(rs, ctx);
      
      return null;
   }

   protected void loadFromPreload(Object[] data, EntityEnterpriseContext ctx) throws Exception {
//log.debug("PRELOAD: Loading from preload - entity "+ctx.getId());   
      int fieldCount = 0;
      Iterator iter = jawsEntity.getCMPFields();
      while (iter.hasNext())
      {
         CMPFieldMetaData cmpField = (CMPFieldMetaData)iter.next();
         
         setCMPFieldValue(ctx.getInstance(),
                          cmpField,
                          data[fieldCount]);
         fieldCount++;
      }

      // Store state to be able to do tuned updates
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctx.getPersistenceContext();
      if (jawsEntity.isReadOnly()) pCtx.lastRead = System.currentTimeMillis();
      pCtx.state = getState(ctx);
   }
   
   protected void loadOneEntity(ResultSet rs, EntityEnterpriseContext ctx) throws Exception { 
      int idx = 1;
      // skip the PK fields at the beginning of the select.
      Iterator keyIt = jawsEntity.getPkFields();
      while (keyIt.hasNext()) {
         keyIt.next();
         idx++;
      }

      int fieldCount = 0;
      Iterator iter = jawsEntity.getCMPFields();
      while (iter.hasNext())
      {
         CMPFieldMetaData cmpField = (CMPFieldMetaData)iter.next();
         
         setCMPFieldValue(ctx.getInstance(),
                          cmpField,
                          getResultObject(rs, cmpFieldPositionInSelect[fieldCount], cmpField));
         fieldCount++;
      }

      // Store state to be able to do tuned updates
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctx.getPersistenceContext();
      if (jawsEntity.isReadOnly()) pCtx.lastRead = System.currentTimeMillis();
      pCtx.state = getState(ctx);


   }
   
   // Protected -----------------------------------------------------

   protected boolean isTimedOut(EntityEnterpriseContext ctx)
   {
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctx.getPersistenceContext();
		 
      return (System.currentTimeMillis() - pCtx.lastRead) > jawsEntity.getReadOnlyTimeOut();
   }
}
