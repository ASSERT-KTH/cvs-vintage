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

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JAWSPersistenceManager;
import org.jboss.ejb.plugins.jaws.JPMLoadEntityCommand;
import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;

/**
 * JAWSPersistenceManager JDBCLoadEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @version $Revision: 1.8 $
 */
public class JDBCLoadEntityCommand
   extends JDBCQueryCommand
   implements JPMLoadEntityCommand
{
   // Constructors --------------------------------------------------

   public JDBCLoadEntityCommand(JDBCCommandFactory factory)
   {
      super(factory, "Load");

      // Select SQL
      String sql = "SELECT ";
      Iterator it = jawsEntity.getCMPFields();
      boolean first = true;

      while (it.hasNext())
      {
         CMPFieldMetaData cmpField = (CMPFieldMetaData)it.next();
         
         sql += (first ? "" : ",") +
                cmpField.getColumnName();
         first = false;
      }
      
      sql += " FROM " + jawsEntity.getTableName() +
             " WHERE " + getPkColumnWhereList();
      if (jawsEntity.hasSelectForUpdate())
      {
         sql += " FOR UPDATE";
      }

      setSQL(sql);
   }

   // JPMLoadEntityCommand implementation ---------------------------

   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      if ( !jawsEntity.isReadOnly() || isTimedOut(ctx) )
      {
         try
         {
            jdbcExecute(ctx);
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
      int idx = 1;
      
      Iterator iter = jawsEntity.getCMPFields();
      while (iter.hasNext())
      {
         CMPFieldMetaData cmpField = (CMPFieldMetaData)iter.next();
         
         setCMPFieldValue(ctx.getInstance(), 
                          cmpField, 
                          getResultObject(rs, idx++, cmpField));
      }

      // Store state to be able to do tuned updates
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctx.getPersistenceContext();
      if (jawsEntity.isReadOnly()) pCtx.lastRead = System.currentTimeMillis();
      pCtx.state = getState(ctx);

      return null;
   }

   // Protected -----------------------------------------------------

   protected boolean isTimedOut(EntityEnterpriseContext ctx)
   {
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctx.getPersistenceContext();
		 
      return (System.currentTimeMillis() - pCtx.lastRead) > jawsEntity.getReadOnlyTimeOut();
   }
}
