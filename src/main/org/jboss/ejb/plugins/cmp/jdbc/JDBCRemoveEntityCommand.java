/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.rmi.RemoteException;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.RemoveEntityCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;

/**
 * JDBCRemoveEntityCommand executes a DELETE FROM table WHERE command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.7 $
 */
public class JDBCRemoveEntityCommand
   extends JDBCUpdateCommand
   implements RemoveEntityCommand
{
   // Constructors --------------------------------------------------
   
   public JDBCRemoveEntityCommand(JDBCStoreManager manager) {
      super(manager, "Remove");
      
      StringBuffer sql = new StringBuffer();
      sql.append("DELETE ");
      sql.append("FROM ").append(entityMetaData.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));
      
      setSQL(sql.toString());
   }
   
   // RemoveEntityCommand implementation -------------------------
   
   public void execute(EntityEnterpriseContext context)
         throws RemoteException, RemoveException {
      
      HashMap oldRelationMap = new HashMap();
      
      // remove entity from all relations before removing from db
      JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
      for(int i=0; i<cmrFields.length; i++) {
         if(cmrFields[i].isCollectionValued()) {
            Collection oldValue = (Collection)cmrFields[i].getValue(context);
            oldRelationMap.put(cmrFields[i], new HashSet(oldValue));
             oldValue.clear();
         } else {
            Object oldValue = cmrFields[i].getValue(context);
            oldRelationMap.put(cmrFields[i], oldValue);
            cmrFields[i].setValue(context, null);
         }
      }

      if(cmrFields.length > 0) {
         manager.getContainer().synchronizeEntitiesWithinTransaction(
               context.getTransaction());
      }
      
      try {
         jdbcExecute(context.getId());
      } catch (Exception e) {
         throw new RemoveException("Could not remove " + context.getId());
      }
      
      HashSet deletedEntities = new HashSet();
      for(int i=0; i<cmrFields.length; i++) {
         if(cmrFields[i].getMetaData().getRelatedRole().isCascadeDelete()) {
            Object oldRelation = oldRelationMap.get(cmrFields[i]);
            if(oldRelation instanceof Collection) {
               Iterator oldValues = ((Collection)oldRelation).iterator();
               while(oldValues.hasNext()) {
                  EJBLocalObject oldValue = (EJBLocalObject)oldValues.next(); 
                  if(!deletedEntities.contains(oldValue)) {
                     deletedEntities.add(oldValue);
                     oldValue.remove();
                  }
               }
            } else {
               EJBLocalObject oldValue = (EJBLocalObject)oldRelation;
               if(oldValue != null) {
                  if(!deletedEntities.contains(oldValue)) {
                     deletedEntities.add(oldValue);
                     oldValue.remove();
                  }
               }
            }
         }
      }
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected void setParameters(PreparedStatement ps, Object primaryKey) throws Exception {
      entity.setPrimaryKeyParameters(ps, 1, primaryKey);
   }
   
   protected Object handleResult(int rowsAffected, Object argOrArgs) throws Exception {
      if(rowsAffected == 0) {
         throw new RemoveException("Could not remove entity");
      }
      return null;
   }
}
