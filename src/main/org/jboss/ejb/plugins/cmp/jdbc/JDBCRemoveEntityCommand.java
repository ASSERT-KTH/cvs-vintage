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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJBLocalObject;
import javax.ejb.RemoveException;
import javax.sql.DataSource;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCRemoveEntityCommand executes a DELETE FROM table WHERE command.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.20 $
 */
public class JDBCRemoveEntityCommand {
   
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
      sql.append("DELETE");
      sql.append(" FROM ").append(entity.getTableName());
      sql.append(" WHERE ").append(SQLUtil.getWhereClause(
               entity.getPrimaryKeyFields()));
      
      removeEntitySQL = sql.toString();
      log.debug("Remove SQL: " + removeEntitySQL);
   }
   
   public void execute(EntityEnterpriseContext context)
         throws RemoveException {

      // remove entity from all relations
      HashMap oldRelations = removeFromRelations(context);

      // update the related entities (stores the removal from relationships)
      if(!oldRelations.isEmpty()) {
         if (!manager.getContainer().getBeanMetaData().getContainerConfiguration().getSyncOnCommitOnly())
         {
            manager.getContainer().synchronizeEntitiesWithinTransaction(
               context.getTransaction());
         }
      }
      
      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected = 0;
      try {
         // get the connection
         DataSource dataSource = entity.getDataSource();
         con = dataSource.getConnection();
         
         // create the statement
         log.debug("Executing SQL: " + removeEntitySQL);
         ps = con.prepareStatement(removeEntitySQL);
         
         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, context.getId());

         // execute statement
         rowsAffected = ps.executeUpdate();
      } catch(Exception e) {
         log.error("Could not remove " + context.getId(), e);
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

      manager.getPrefetchCache().removePrefetchData(context.getId());
   }

   private HashMap removeFromRelations(EntityEnterpriseContext context) {
      HashMap oldRelations = new HashMap();
      
      // remove entity from all relations before removing from db
      for(Iterator cmrFields = entity.getCMRFields().iterator(); 
            cmrFields.hasNext();) { 

         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)cmrFields.next();

         if(cmrField.isCollectionValued()) {
            Collection c = 
                  (Collection)cmrField.getInstanceValue(context);
            if(!c.isEmpty()) {
               oldRelations.put(cmrField, new ArrayList(c));

               // clear unless the related CMR is a foreign key
               // that is a part of the primary key
               if(!cmrField.getRelatedCMRField().isFkPartOfPk()) {
                  c.clear();
               }
            }
         } else {
            Object o = cmrField.getInstanceValue(context);
            if(o != null) {
               oldRelations.put(cmrField, Collections.singletonList(o));

               // set to null unless the related CMR is a foreign key
               // that is a part of the primary key
               if(!cmrField.isFkPartOfPk()) {
                  cmrField.setInstanceValue(context, null);
               }
            }
         }
      }
      return oldRelations;
   }
   
   private void cascadeDelete(HashMap oldRelations) throws RemoveException {
      HashMap deletedPksByEntity = new HashMap();

      for(Iterator cmrFields = oldRelations.keySet().iterator(); 
            cmrFields.hasNext();) { 

         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)cmrFields.next();
         JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
         
         if(cmrField.getMetaData().getRelatedRole().isCascadeDelete()) {
            List oldValues = (List)oldRelations.get(cmrField);

            Set deletedPks = (Set)deletedPksByEntity.get(relatedEntity);
            if(deletedPks == null) {
               deletedPks = new HashSet();
               deletedPksByEntity.put(relatedEntity, deletedPks);
            }

            for(Iterator iter = oldValues.iterator(); iter.hasNext();) {
               EJBLocalObject oldValue = (EJBLocalObject)iter.next(); 
               Object oldValuePk = oldValue.getPrimaryKey();

               log.info("Checking if already deleted: " + oldValuePk);
               if(!deletedPks.contains(oldValuePk)) {
                  deletedPks.add(oldValuePk);
                  log.info("Deleteing: " + oldValuePk);
                  oldValue.remove();
               }
            }
         }
      }
   }
}
