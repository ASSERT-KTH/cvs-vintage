/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Iterator;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JAWSPersistenceManager;
import org.jboss.ejb.plugins.jaws.JPMCreateEntityCommand;
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.MetaInfo;
import org.jboss.ejb.plugins.jaws.PkFieldInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * JAWSPersistenceManager JDBCCreateEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public class JDBCCreateEntityCommand
   extends JDBCUpdateCommand
   implements JPMCreateEntityCommand
{
   // Attributes ----------------------------------------------------
   
   private JDBCBeanExistsCommand beanExistsCommand;
   
   // Constructors --------------------------------------------------
   
   public JDBCCreateEntityCommand(JDBCCommandFactory factory)
   {
      super(factory, "Create");
      
      beanExistsCommand = factory.createBeanExistsCommand();
      
      // Insert SQL
      
      String sql = "INSERT INTO " + metaInfo.getTableName();
      String fieldSql = "";
      String valueSql = "";
      
      Iterator it = metaInfo.getCMPFieldInfos();
      boolean first = true;
      
      while (it.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)it.next();
         
         if (fieldInfo.isEJBReference())
         {
            JawsCMPField[] pkFields = fieldInfo.getForeignKeyCMPFields();
            
            for (int i = 0; i < pkFields.length; i++)
            {
               fieldSql += (first ? "" : ",") +
                           fieldInfo.getColumnName() + "_" +
                           pkFields[i].getColumnName();
               valueSql += first ? "?" : ",?";
               first = false;
            }
         } else
         {
            fieldSql += (first ? "" : ",") +
                        fieldInfo.getColumnName();
            valueSql += first ? "?" : ",?";
            first = false;
         }
      }
      
      sql += " ("+fieldSql+") VALUES ("+valueSql+")";
      
      setSQL(sql);
   }
   
   // JPMCreateEntityCommand implementation -------------------------
   
   public Object execute(Method m,
                       Object[] args,
                       EntityEnterpriseContext ctx)
      throws RemoteException, CreateException
   {
      try
      {
         // Extract pk
         Object id = null;
         Iterator it = metaInfo.getPkFieldInfos();
         
         if (metaInfo.hasCompositeKey())
         {
            try
            {
               id = metaInfo.getPrimaryKeyClass().newInstance();
            } catch (InstantiationException e)
            {
               throw new ServerException("Could not create primary key",e);
            }
            
            while (it.hasNext())
            {
               PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
               Field from = pkFieldInfo.getCMPField();
               Field to = pkFieldInfo.getPkField();
               to.set(id, from.get(ctx.getInstance()));
            }
         } else
         {
            PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
            Field from = pkFieldInfo.getCMPField();
            id = from.get(ctx.getInstance());
         }
         
         if (debug)
         {
            log.debug("Create, id is "+id);
         }
         
         // Check duplicate
         if (beanExistsCommand.execute(id))
         {
            throw new DuplicateKeyException("Entity with key "+id+" already exists");
         }
         
         // Insert in db
         
         try
         {
            jdbcExecute(ctx);
         } catch (Exception e)
         {
            log.exception(e);
            throw new CreateException("Could not create entity:"+e);
         }
         
         return id;
         
      } catch (IllegalAccessException e)
      {
         log.exception(e);
         throw new CreateException("Could not create entity:"+e);
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected void setParameters(PreparedStatement stmt, Object argOrArgs) 
      throws Exception
   {
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)argOrArgs;
      int idx = 1; // Parameter-index
      
      Iterator iter = metaInfo.getCMPFieldInfos();
      while (iter.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)iter.next();
         Object value = getCMPFieldValue(ctx.getInstance(), fieldInfo);
         
         if (fieldInfo.isEJBReference())
         {
            idx = setForeignKey(stmt, idx, fieldInfo, value);
         } else
         {
            setParameter(stmt, idx++, fieldInfo.getJDBCType(), value);
         }
      }
   }
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) 
      throws Exception
   {
      // arguably should check one row went in!!!
      
      EntityEnterpriseContext ctx = (EntityEnterpriseContext)argOrArgs;
      
      // Store state to be able to do tuned updates
      JAWSPersistenceManager.PersistenceContext pCtx =
         new JAWSPersistenceManager.PersistenceContext();
      
      // If read-only, set last read to now
      if (metaInfo.isReadOnly()) pCtx.lastRead = System.currentTimeMillis();
      
      // Save initial state for tuned updates
      pCtx.state = getState(ctx);
      
      ctx.setPersistenceContext(pCtx);
      
      return null;
   }
}
