/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Field;

import java.util.Iterator;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JAWSPersistenceManager;
import org.jboss.ejb.plugins.jaws.JPMStoreEntityCommand;
import org.jboss.ejb.plugins.jaws.CMPFieldInfo;
import org.jboss.ejb.plugins.jaws.PkFieldInfo;
import org.jboss.ejb.plugins.jaws.deployment.JawsCMPField;

/**
 * JAWSPersistenceManager JDBCStoreEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @version $Revision: 1.2 $
 */
public class JDBCStoreEntityCommand
   extends JDBCUpdateCommand
   implements JPMStoreEntityCommand
{
   // Attributes ----------------------------------------------------
   
   private EntityEnterpriseContext ctxArgument;
   private boolean tuned;
   private Object[] currentState;
   private boolean[] dirtyField;    // only used for tuned updates
   
   // Constructors --------------------------------------------------
   
   public JDBCStoreEntityCommand(JDBCCommandFactory factory)
   {
      super(factory, "Store");
      tuned = metaInfo.hasTunedUpdates();
      
      // If we don't have tuned updates, create static SQL
      if (!tuned)
      {
         setSQL(makeSQL());
      }
   }
   
   // JPMStoreEntityCommand implementation ---------------------------
   
   /**
    * if the readOnly flag is specified in the xml file this won't store.
    * if not a tuned or untuned update is issued.
    */
   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Check for read-only
      // JF: Shouldn't this throw an exception?
      if (metaInfo.isReadOnly())
      {
         return;
      }
      
      ctxArgument = ctx;
      currentState = getState(ctx);
      boolean dirty = false;
      
      // For tuned updates, need to see which fields have changed
      
      if (tuned)
      {
         dirtyField = new boolean[currentState.length];
         Object[] oldState =
            ((JAWSPersistenceManager.PersistenceContext)ctx.getPersistenceContext()).state;
         
         for (int i = 0; i < currentState.length; i++)
         {
            dirtyField[i] = changed(currentState[i], oldState[i]);
            dirty |= dirtyField[i];
         }
      }
      
      if (!tuned || dirty)
      {
         try
         {
            // Update db
            jdbcExecute();
            
         } catch (Exception e)
         {
            throw new ServerException("Store failed", e);
         }
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   /**
    * Returns dynamically-generated SQL if this entity
    * has tuned updates, otherwise static SQL.
    */
   protected String getSQL() throws Exception
   {
      return tuned ? makeSQL() : super.getSQL();
   }
   
   protected void setParameters(PreparedStatement stmt) throws Exception
   {
      int idx = 1;
      Iterator iter = metaInfo.getCMPFieldInfos();
      int i = 0;
      while (iter.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)iter.next();
         
         if (!tuned || dirtyField[i])
         {
            if (fieldInfo.isEJBReference())
            {
               idx = setForeignKey(stmt, idx, fieldInfo, currentState[i]);
            } else
            {
               setParameter(stmt, idx++, fieldInfo.getJDBCType(), currentState[i]);
            }
         }
         
         i++;
      }
      
      // Primary key in WHERE-clause
      Iterator it = metaInfo.getPkFieldInfos();
      while (it.hasNext())
      {
         PkFieldInfo pkFieldInfo = (PkFieldInfo)it.next();
         int jdbcType = pkFieldInfo.getJDBCType();
         Field field = pkFieldInfo.getCMPField();
         Object value = field.get(ctxArgument.getInstance());
         
         // SA had introduced the change below, but it fails
         // for non-composite primary keys.
         // Object value = getPkFieldValue(ctxArgument.getId(), pkFieldInfo);
         
         setParameter(stmt, idx++, jdbcType, value);
      }
   }
   
   protected void handleResult(int rowsAffected) throws Exception
   {
      if (tuned)
      {
         // Save current state for tuned updates
         JAWSPersistenceManager.PersistenceContext pCtx =
            (JAWSPersistenceManager.PersistenceContext)ctxArgument.getPersistenceContext();
         pCtx.state = currentState;
      }
   }
   
   // Protected -----------------------------------------------------
   
   protected final boolean changed(Object current, Object old)
   {
      return (current == null) ? (old != null) : !current.equals(old);
   }
   
   /** 
    * Used to create static SQL (tuned = false) or dynamic SQL (tuned = true).
    */
   protected String makeSQL()
   {
      String sql = "UPDATE "+metaInfo.getTableName()+" SET ";
      Iterator iter = metaInfo.getCMPFieldInfos();
      int i = 0;
      boolean first = true;
      while (iter.hasNext())
      {
         CMPFieldInfo fieldInfo = (CMPFieldInfo)iter.next();
         
         if (!tuned || dirtyField[i++])
         {
            if (fieldInfo.isEJBReference())
            {
               JawsCMPField[] pkFields = fieldInfo.getForeignKeyCMPFields();
               
               for (int j = 0; j < pkFields.length; j++)
               {
                  sql += (first?"":",") + 
                     fieldInfo.getColumnName()+"_"+pkFields[j].getColumnName()+
                     "=?";
                  first = false;
               }
            } else
            {
               sql += (first?"":",") +
                  fieldInfo.getColumnName() + "=?";
               first = false;
            }
         }
      }
      sql += " WHERE "+getPkColumnWhereList();
      return sql;
   }
}
