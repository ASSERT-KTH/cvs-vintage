/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsEntity;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * JAWSPersistenceManager JDBCLoadEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public class JDBCLoadEntityCommand
   extends JDBCQueryCommand
   implements JPMLoadEntityCommand
{
   // Attributes ----------------------------------------------------
   
   private EntityEnterpriseContext ctxArgument;
   
   // Constructors --------------------------------------------------
   
   public JDBCLoadEntityCommand(JDBCCommandFactory factory)
   {
      super(factory, "Load");
      
      // Select SQL
      String sql = "SELECT ";
      
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
               sql += (first ? "" : ",") +
                      fieldInfo.getColumnName() + "_" +
                      pkFields[i].getColumnName();
               first = false;
            }
         } else
         {
            sql += (first ? "" : ",") +
                   fieldInfo.getColumnName();
            first = false;
         }
      }
      
      sql += " FROM " + metaInfo.getTableName() +
             " WHERE " + getPkColumnWhereList();
      
      setSQL(sql);
   }
   
   // JPMLoadEntityCommand implementation ---------------------------
   
   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Save the argument for use by setParameters() and handleResult()
      ctxArgument = ctx;
      
      if ( !metaInfo.isReadOnly() || isTimedOut() )
      {
         try
         {
            jdbcExecute();
         } catch (Exception e)
         {
            throw new ServerException("Load failed", e);
         }
      }
   }
   
   // JDBCQueryCommand overrides ------------------------------------
   
   protected void setParameters(PreparedStatement stmt) throws Exception
   {
      setPrimaryKeyParameters(stmt, 1, ctxArgument.getId());
   }
   
   protected void handleResult(ResultSet rs) throws Exception
   {
      if (!rs.next())
         throw new NoSuchObjectException("Entity "+ctxArgument.getId()+" not found");
      
      // Set values
      int idx = 1;
      
      Iterator iter = metaInfo.getCMPFieldInfos();
      while (iter.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)iter.next();
         int jdbcType = fieldInfo.getJDBCType();
         
         if (fieldInfo.isEJBReference())
         {
            // Create pk
            JawsCMPField[] pkFields = fieldInfo.getForeignKeyCMPFields();
            JawsEntity referencedEntity = (JawsEntity)pkFields[0].getBeanContext();
            Object pk;
            if (referencedEntity.getPrimaryKeyField().equals(""))
            {
               // Compound key
               pk = factory.getContainer().getClassLoader().loadClass(referencedEntity.getPrimaryKeyClass()).newInstance();
               Field[] fields = pk.getClass().getFields();
               for(int j = 0; j < fields.length; j++)
               {
                  Object val = getResultObject(rs, idx++, jdbcType);
                  fields[j].set(pk, val);
                  
                  if (debug)
                  {
                     log.debug("Referenced pk field:" + val);
                  }
               }
            } else
            {
               // Primitive key
               pk = getResultObject(rs, idx++, jdbcType);
               
               if (debug)
               {
                  log.debug("Referenced pk:" + pk);
               }
            }
            
            // Find referenced entity
            try
            {
               Object home = factory.getJavaCtx().lookup(fieldInfo.getSQLType());
               Method[] homeMethods = home.getClass().getMethods();
               Method finder = null;
               
               // We have to locate fBPK iteratively since we don't
               // really know the pk-class
               for (int j = 0; j < homeMethods.length; j++)
               {
                  if (homeMethods[j].getName().equals("findByPrimaryKey"))
                  {
                     finder = homeMethods[j];
                     break;
                  }
               }
               
               if (finder == null)
               {
                  throw new NoSuchMethodException(
                     "FindByPrimaryKey method not found in home interface");
               }
               
               log.debug("PK=" + pk);
               Object ref = finder.invoke(home, new Object[] { pk });
               
               // Set found entity
               setCMPFieldValue(ctxArgument.getInstance(), fieldInfo, ref);
            } catch (Exception e)
            {
               throw new ServerException("Could not restore reference", e);
            }
         } else
         {
            // Load primitive
            
            // TODO: this probably needs to be fixed for BLOB's etc.
            setCMPFieldValue(ctxArgument.getInstance(), 
                             fieldInfo, 
                             getResultObject(rs, idx++, jdbcType));
         }
      }
      
      // Store state to be able to do tuned updates
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctxArgument.getPersistenceContext();
      if (metaInfo.isReadOnly()) pCtx.lastRead = System.currentTimeMillis();
      pCtx.state = getState(ctxArgument);
   }
   
   // Protected -----------------------------------------------------
   
   protected boolean isTimedOut()
   {
      JAWSPersistenceManager.PersistenceContext pCtx =
         (JAWSPersistenceManager.PersistenceContext)ctxArgument.getPersistenceContext();
      
      return (System.currentTimeMillis() - pCtx.lastRead) > metaInfo.getReadOnlyTimeOut();
   }
}
