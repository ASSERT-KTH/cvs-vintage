/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.CMPStoreManager;
import org.jboss.ejb.plugins.cmp.CreateEntityCommand;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;

/**
 * JDBCCreateEntityCommand executes an INSERT INTO ... VALUES .... query.
 * This command will only insert non-read-only columns.  If a primary key
 * column is read-only this command will throw a CreateException.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.5 $
 */
public class JDBCCreateEntityCommand
   extends JDBCUpdateCommand
   implements CreateEntityCommand
{
   // Attributes ----------------------------------------------------
   
   private JDBCBeanExistsCommand beanExistsCommand;
   
   // Constructors --------------------------------------------------
   
   public JDBCCreateEntityCommand(JDBCStoreManager manager) {
      super(manager, "Create"); 
      
      beanExistsCommand = manager.getCommandFactory().createBeanExistsCommand();
   }
   
   // CreateEntityCommand implementation -------------------------
   
   public Object execute(Method m,
               Object[] args,
               EntityEnterpriseContext ctx)
         throws RemoteException, CreateException
   {
      Object id = null;
      try {
         // Extract pk
         id = entity.extractPrimaryKeyFromInstance(ctx);
         log.debug("Create, id is "+id);
      } catch(Exception e) {
         log.error(e);
         throw new CreateException("Extract primary key from instance:" + e);
      }
         
      // Check duplicate
      try {
         if(beanExistsCommand.execute(id)) {
            throw new DuplicateKeyException("Entity with key " + id + 
                  " already exists");
         }
      } catch(Exception e) {
         log.error(e);
         throw new CreateException("Error while checking if entity already " +
               "exists: " + e);
      }
         
      // pass this info on 
      ExecutionState es = new ExecutionState();
      es.ctx = ctx;
      
      // we don't insert read only fields
      es.fields = getInsertFields();
   
      // Insert in db         
      try {
         jdbcExecute(es);
      } catch(Exception e) {
         log.error(e);
         throw new CreateException("Could not create entity:" + e);
      }

      // mark the entity as created
      entity.setCreated(ctx);
      
      return id;         
   }
   
   // JDBCUpdateCommand overrides -----------------------------------
   
   protected String getSQL(Object argOrArgs) throws Exception {
      ExecutionState es = (ExecutionState)argOrArgs;
      
      StringBuffer sql = new StringBuffer();
      sql.append("INSERT INTO ").append(entityMetaData.getTableName());      

      sql.append(" (");
            sql.append(SQLUtil.getColumnNamesClause(es.fields));
      sql.append(")");

      sql.append(" VALUES (");
            sql.append(SQLUtil.getValuesClause(es.fields));
      sql.append(")");      
      return sql.toString();
   }

   protected void setParameters(PreparedStatement ps, Object arg) 
      throws Exception
   {
      ExecutionState es = (ExecutionState)arg;      
      entity.setInstanceParameters(ps, 1, es.ctx, es.fields);
   }
   
   protected Object handleResult(int rowsAffected, Object arg) 
      throws Exception
   {
      ExecutionState es = (ExecutionState)arg;      

      if(rowsAffected != 1) {
         throw new CreateException("Insertion of " + entity.getEntityName() + " EJB failed id=" + es.ctx.getId() + " rowsAffected=" + rowsAffected);
      }

      // We just created the bean so mark all fields inserted as cleab
      for(int i=0; i<es.fields.length; i++) {
         es.fields[i].setClean(es.ctx);
      }
            
      return null;
   }

   protected JDBCCMPFieldBridge[] getInsertFields() throws CreateException {
      JDBCCMPFieldBridge[] cmpFields = entity.getJDBCCMPFields();
      ArrayList fields = new ArrayList(cmpFields.length);

      for(int i=0; i<cmpFields.length; i++) {
         if(!cmpFields[i].isReadOnly()) {
            fields.add(cmpFields[i]);
         } else if(cmpFields[i].isPrimaryKeyMember()) {
            // can't create because pk is read-only
            throw new CreateException("Creation of a " + entity.getEntityName() + 
                  " EJB is not allowed because the primary key field " + cmpFields[i].getFieldName() + " is read only.");
         }
      }
      return (JDBCCMPFieldBridge[])fields.toArray(new JDBCCMPFieldBridge[fields.size()]);
   }
   
   private class ExecutionState {
      public JDBCCMPFieldBridge[] fields;
      public EntityEnterpriseContext ctx;
   }
}
