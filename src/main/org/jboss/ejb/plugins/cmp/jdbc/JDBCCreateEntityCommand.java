/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.sql.DataSource;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCCreateEntityCommand executes an INSERT INTO query.
 * This command will only insert non-read-only columns.  If a primary key
 * column is read-only this command will throw a CreateException.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.12 $
 */
public class JDBCCreateEntityCommand {
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;
   private List insertFields;
   private String entityExistsSQL;
   private String insertEntitySQL;
   private boolean createAllowed;
   
   public JDBCCreateEntityCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());

      // set create allowed
      createAllowed = true;
      List fields = entity.getFields();
      for(Iterator iter = fields.iterator(); iter.hasNext(); ) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         if(field.isPrimaryKeyMember() && field.isReadOnly()) {
            createAllowed = false;
            break;
         }
      }
      
      if(createAllowed) {
         insertFields = getInsertFields();
         entityExistsSQL = createEntityExistsSQL();
         insertEntitySQL = createInsertEntitySQL();
         log.debug("Entity Exists SQL: " + entityExistsSQL);
         log.debug("Insert Entity SQL: " + insertEntitySQL);
      } else {
         log.debug("Create will not be allowed.");
      }
   }
  
   private List getInsertFields() {
      List fields = entity.getFields();
      List insertFields = new ArrayList(fields.size());

      for(Iterator iter = fields.iterator(); iter.hasNext(); ) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         if(!field.isReadOnly()) {
            insertFields.add(field);
         } 
      }
      return insertFields;
   }

   private String createEntityExistsSQL() {
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT COUNT(*)");
      sql.append(" FROM ").append(entity.getTableName());
      sql.append(" WHERE ");
      sql.append(SQLUtil.getWhereClause(entity.getPrimaryKeyFields()));
      
      return sql.toString();
   }

   private String createInsertEntitySQL() {
      StringBuffer sql = new StringBuffer();
      sql.append("INSERT INTO ").append(entity.getTableName());      
      sql.append(" (");
            sql.append(SQLUtil.getColumnNamesClause(insertFields));
      sql.append(")");

      sql.append(" VALUES (");
            sql.append(SQLUtil.getValuesClause(insertFields));
      sql.append(")");      
      return sql.toString();
   }

   public Object execute(Method m,
               Object[] args,
               EntityEnterpriseContext ctx) throws CreateException {

      if(!createAllowed) {
         throw new CreateException("Creation is not allowed because a " +
               "primary key field is read only.");
      }

      Object pk = entity.extractPrimaryKeyFromInstance(ctx);
      log.debug("Create: pk="+pk);

      if(entityExists(pk)) {
         throw new DuplicateKeyException("Entity with primary key " + pk + 
               " already exists");
      }
      insertEntity(ctx);

      entity.setCreated(ctx);
      
      return pk;         
   }

   private boolean entityExists(Object pk)
         throws CreateException {

      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         DataSource dataSource = entity.getDataSource();
         con = dataSource.getConnection();
         
         // create the statement
         log.debug("Executing SQL: " + entityExistsSQL);
         ps = con.prepareStatement(entityExistsSQL);
         
         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, pk);

         // execute statement
         ResultSet rs = ps.executeQuery();
         if(!rs.next()) {
            throw new CreateException("Error checking if entity exists: " +
                  "result set contains no rows");
         }
      
         // did any rows mathch
         return rs.getInt(1) > 0;
      } catch(CreateException e) {
         throw e;
      } catch(Exception e) {
         log.error("Error checking if entity exists", e);
         throw new CreateException("Error checking if entity exists:" + e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }
   
   private void insertEntity(EntityEnterpriseContext ctx)
         throws CreateException{

      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected  = 0;
      try {
         // get the connection
         DataSource dataSource = entity.getDataSource();
         con = dataSource.getConnection();
         
         // create the statement
         log.debug("Executing SQL: " + insertEntitySQL);
         ps = con.prepareStatement(insertEntitySQL);
         
         // set the parameters
         int index = 1;
         for(Iterator iter = insertFields.iterator(); iter.hasNext(); ) {
            JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
            index = field.setInstanceParameters(ps, index, ctx);
         }

         // execute statement
         rowsAffected = ps.executeUpdate();
      } catch(Exception e) {
         log.error("Could not create entity", e);
         throw new CreateException("Could not create entity:" + e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected != 1) {
         throw new CreateException("Insertion failed. Expected one " +
               "affected row: rowsAffected=" + rowsAffected +
               "id=" + ctx.getId());
      }
      log.debug("Create: Rows affected = " + rowsAffected);

      // Mark the inserted fields as clean.
      for(Iterator iter = insertFields.iterator(); iter.hasNext(); ) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         field.setClean(ctx);
      }
   }
}
