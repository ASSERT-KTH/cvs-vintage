/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import javax.sql.DataSource;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.RemoveEntityCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCRemoveEntityCommand executes a DELETE FROM table WHERE command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.8 $
 */
public class JDBCRemoveEntityCommand implements RemoveEntityCommand {
   
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;
   private String removeEntitySQL;

   public JDBCRemoveEntityCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());

      StringBuffer sql = new StringBuffer();
      sql.append("DELETE ");
      sql.append("FROM ").append(entity.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(
               entity.getJDBCPrimaryKeyFields()));
      
      removeEntitySQL = sql.toString();
      log.debug("Remove SQL: " + removeEntitySQL);
   }
   
   public void execute(EntityEnterpriseContext context)
         throws RemoveException {
      
      // remove entity from all relations
      HashMap oldRelations = removeFromRelations(context);

      // update the related entities (stores the removal from relationships)
      if(entity.getJDBCCMRFields().length > 0) {
         manager.getContainer().synchronizeEntitiesWithinTransaction(
               context.getTransaction());
      }
      
      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected = 0;
      try {
         // get the connection
         DataSource dataSource = manager.getDataSource();
         con = dataSource.getConnection();
         
         // create the statement
         ps = con.prepareStatement(removeEntitySQL);
         
         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, context.getId());

         // execute statement
         rowsAffected = ps.executeUpdate();
      } catch(Exception e) {
         log.error(e);
         throw new RemoveException("Could not remove " + context.getId());
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected == 0) {
         throw new RemoveException("Could not remove entity");
      }
      log.debug("Remove: Rows affected = " + rowsAffected);

      // cascate-delete to old relations, if relation uses cascade.
      cascadeDelete(oldRelations);
   }

   private HashMap removeFromRelations(EntityEnterpriseContext context) {
      HashMap oldRelations = new HashMap();
      JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();
      
      // remove entity from all relations before removing from db
      for(int i=0; i<cmrFields.length; i++) {
         if(cmrFields[i].isCollectionValued()) {
            Collection oldValue = (Collection)cmrFields[i].getValue(context);
            oldRelations.put(cmrFields[i], new HashSet(oldValue));
            oldValue.clear();
         } else {
            Object oldValue = cmrFields[i].getValue(context);
            oldRelations.put(cmrFields[i], oldValue);
            cmrFields[i].setValue(context, null);
         }
      }
      return oldRelations;
   }
   
   private void cascadeDelete(HashMap oldRelations) throws RemoveException {
      HashSet deletedEntities = new HashSet();
      JDBCCMRFieldBridge[] cmrFields = entity.getJDBCCMRFields();

      for(int i=0; i<cmrFields.length; i++) {
         if(cmrFields[i].getMetaData().getRelatedRole().isCascadeDelete()) {
            Object oldRelation = oldRelations.get(cmrFields[i]);
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
}
