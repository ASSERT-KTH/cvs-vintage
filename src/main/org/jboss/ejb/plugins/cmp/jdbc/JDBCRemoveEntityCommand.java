/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;
import org.jboss.metadata.ConfigurationMetaData;


/**
 * JDBCRemoveEntityCommand executes a DELETE FROM table WHERE command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="mailto:alex@jboss.org">Alexey Loubyansky</a>
 * @version $Revision: 1.26 $
 */
public final class JDBCRemoveEntityCommand
{
   private final JDBCStoreManager manager;
   private final JDBCEntityBridge entity;
   private final Logger log;
   private final String removeEntitySQL;
   private final boolean syncOnCommitOnly;
   private final JDBCCMRFieldBridge[] cascadeDeleteFields;

   public JDBCRemoveEntityCommand(JDBCStoreManager manager)
   {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
         this.getClass().getName() +
         "." +
         manager.getMetaData().getName());

      StringBuffer sql = new StringBuffer();
      sql.append(SQLUtil.DELETE_FROM)
         .append(entity.getTableName())
         .append(SQLUtil.WHERE);
      SQLUtil.getWhereClause(entity.getPrimaryKeyFields(), sql);

      removeEntitySQL = sql.toString();
      if(log.isDebugEnabled())
         log.debug("Remove SQL: " + removeEntitySQL);

      ConfigurationMetaData containerConfig = manager.getContainer().
         getBeanMetaData().getContainerConfiguration();
      syncOnCommitOnly = containerConfig.getSyncOnCommitOnly();

      JDBCCMRFieldBridge[] cmrFields = entity.getCMRFields();
      List cascadeDeleteList = new ArrayList();
      for(int i = 0; i < cmrFields.length; ++i)
      {
         JDBCCMRFieldBridge cmrField = cmrFields[i];
         if(cmrField.getMetaData().getRelatedRole().isCascadeDelete())
            cascadeDeleteList.add(cmrField);
      }

      if(cascadeDeleteList.isEmpty())
         cascadeDeleteFields = null;
      else
         cascadeDeleteFields = (JDBCCMRFieldBridge[]) cascadeDeleteList.
            toArray(new JDBCCMRFieldBridge[cascadeDeleteList.size()]);
   }

   public void execute(EntityEnterpriseContext ctx)
      throws RemoveException
   {
      // remove entity from all relations
      Object[] oldRelationsRef = new Object[1];
      boolean needsSync = removeFromRelations(ctx, oldRelationsRef);

      // update the related entities (stores the removal from relationships)
      // if one of the store fails an EJBException will be thrown
      if(!syncOnCommitOnly && needsSync)
      {
         manager.getContainer().synchronizeEntitiesWithinTransaction(ctx.getTransaction());
      }

      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected = 0;
      try
      {
         if(log.isDebugEnabled())
            log.debug("Executing SQL: " + removeEntitySQL);

         // get the connection
         con = entity.getDataSource().getConnection();
         ps = con.prepareStatement(removeEntitySQL);

         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, ctx.getId());

         // execute statement
         rowsAffected = ps.executeUpdate();
      }
      catch(Exception e)
      {
         log.error("Could not remove " + ctx.getId(), e);
         throw new RemoveException("Could not remove " + ctx.getId());
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected == 0)
      {
         throw new RemoveException("Could not remove entity");
      }

      if(log.isDebugEnabled())
         log.debug("Remove: Rows affected = " + rowsAffected);

      // cascate-delete to old relations, if relation uses cascade.
      if(cascadeDeleteFields != null && oldRelationsRef[0] != null)
         cascadeDelete((Map) oldRelationsRef[0]);
      manager.getReadAheadCache().removeCachedData(ctx.getId());
   }

   private boolean removeFromRelations(EntityEnterpriseContext ctx, Object[] oldRelationsRef)
   {
      boolean removed = false;

      // remove entity from all relations before removing from db
      JDBCCMRFieldBridge[] cmrFields = entity.getCMRFields();
      for(int i = 0; i < cmrFields.length; ++i)
      {
         JDBCCMRFieldBridge cmrField = cmrFields[i];
         Object value = cmrField.getInstanceValue(ctx);
         boolean cascadeDelete = cmrField.getMetaData().getRelatedRole().isCascadeDelete();
         if(cmrField.isCollectionValued())
         {
            Set c = (Set) value;
            if(!c.isEmpty())
            {
               removed = true;
               if(cascadeDelete)
                  addToCascadeDeleteList(oldRelationsRef, cmrField, new ArrayList(c));
               // c.clear() is not allowed if fk is part of the pk
               cmrField.setInstanceValue(ctx, null);
            }
         }
         else
         {
            Object o = value;
            if(o != null)
            {
               removed = true;
               if(cascadeDelete)
                  addToCascadeDeleteList(oldRelationsRef, cmrField, Collections.singletonList(o));
               cmrField.setInstanceValue(ctx, null);
            }
         }
      }
      return removed;
   }

   private void cascadeDelete(Map oldRelations) throws RemoveException
   {
      boolean debug = log.isDebugEnabled();
      for(int cmrInd = 0; cmrInd < cascadeDeleteFields.length; ++cmrInd)
      {
         JDBCCMRFieldBridge cmrField = cascadeDeleteFields[cmrInd];
         List oldValues = (List) oldRelations.get(cmrField);
         if(oldValues != null)
         {
            for(int i = 0; i < oldValues.size(); ++i)
            {
               EJBLocalObject oldValue = (EJBLocalObject) oldValues.get(i);
               if(cmrField.getRelatedManager().doCascadeDelete(oldValue))
               {
                  if(debug)
                     log.debug("Deleteing: " + oldValue);
                  oldValue.remove();
               }
               else
               {
                  if(debug)
                     log.debug("Already deleted: " + oldValue);
               }
            }
         }
      }
   }

   private void addToCascadeDeleteList(Object[] oldRelationsRef, JDBCCMRFieldBridge cmrField, List values)
   {
      Map oldRelations = (Map) oldRelationsRef[0];
      if(oldRelations == null)
      {
         oldRelations = new HashMap(cascadeDeleteFields.length);
         oldRelationsRef[0] = oldRelations;
      }
      oldRelations.put(cmrField, values);
      cmrField.getRelatedManager().addCascadeDelete(values);
   }
}
