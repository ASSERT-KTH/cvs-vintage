/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.util.Map;
import java.util.Iterator;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JAWSPersistenceManager;
import org.jboss.ejb.plugins.jaws.JPMLoadEntitiesCommand;
import org.jboss.ejb.plugins.jaws.metadata.CMPFieldMetaData;
import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;
import org.jboss.util.FinderResults;

/**
 * JAWSPersistenceManager JDBCLoadEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:dirk@jboss.de">Dirk Zimmermann</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.1 $
 */
public class JDBCLoadEntitiesCommand
   extends JDBCLoadEntityCommand
   implements JPMLoadEntitiesCommand
{
   String selectClause;
   // Constructors --------------------------------------------------

   public JDBCLoadEntitiesCommand(JDBCCommandFactory factory)
   {
      super(factory);

      selectClause = super.createSelectClause();
   }

   // JPMLoadEntitiesCommand implementation.
   
   public void execute(FinderResults keys)
      throws RemoteException
   {
      JDBCFinderCommand finder = (JDBCFinderCommand)keys.getFinder();
      FinderMetaData metaData = null;
      if (finder != null)
         metaData = finder.getFinderMetaData();
      if (metaData != null && metaData.hasReadAhead() && keys.getQueryData() != null)
      {
         try
         {
            Object[] args = {keys};
            jdbcExecute(args);
         } catch (Exception e)
         {
            throw new ServerException("Load failed", e);
         }
      }
   }
   
   // JDBCQueryCommand overrides ------------------------------------

   protected Object handleResult(ResultSet rs, Object argOrArgs) throws Exception
   {
      FinderResults keys = (FinderResults)((Object[])argOrArgs)[0];
      Map instances = keys.getEntityMap();
      while (rs.next())
      {
         Object key = createKey(rs);
         
         //find the context
         EntityEnterpriseContext ctx = (EntityEnterpriseContext)instances.get(key);
         if (ctx != null) {
            //if the context says it's already valid, don't load it.
            if (!ctx.isValid()) {
               loadOneEntity(rs, ctx);
               ctx.setValid(true);
            }
         } else {
            //if ctx was null, the CMPPersistenceManager doesn't want us to try
            // to load it due to a transaction issue.
         }
      }
      return null;
   }
   
   protected void setParameters(PreparedStatement stmt, Object argOrArgs)
      throws Exception
   {
      FinderResults keys = (FinderResults)((Object[])argOrArgs)[0];
      JDBCFinderCommand finder = (JDBCFinderCommand)keys.getFinder();
      Object[] args = keys.getQueryArgs();
      finder.setParameters(stmt, args);
   }
      
   // JDBCommand ovverrides -----------------------------------------
   protected String getSQL(Object argOrArgs) throws Exception
   {
      FinderResults keys = (FinderResults)((Object[])argOrArgs)[0];
      return selectClause + " " + keys.getQueryData().toString();
   }
   
   // protected -----------------------------------------------------
   
   protected Object createKey(ResultSet rs) throws Exception {
   
      if (jawsEntity.hasCompositeKey())
      {
         // Compound key
         try
         {
            Object pk = jawsEntity.getPrimaryKeyClass().newInstance();
            int i = 1;   // parameter index
            Iterator it = jawsEntity.getPkFields();
            
            while (it.hasNext())
            {
               PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
               Field pkField = pkFieldMetaData.getPkField();
               String colName = pkFieldMetaData.getColumnName();
               pkField.set(pk, getResultObject(rs, 
                                               i++, 
                                               pkField.getType()));
            }
            return pk;
         } catch (Exception e)
         {
            return null;
         }
      } else
      {
         // Primitive key
         Iterator it = jawsEntity.getPkFields();
         PkFieldMetaData pkFieldMetaData = (PkFieldMetaData)it.next();
         return getResultObject(rs, 1, pkFieldMetaData.getCMPField().getType());
      }
   }
   
}
