/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCLoadFieldCommand loads the data for a single field in responce to
 * a lazy load. Lazy load groups can be thought of as the other half of
 * eager loading.  Any field that is not eager loaded must be lazy loaded.
 * In the jbosscmp-jdbc.xml file the bean developer can create groups of
 * fields to load together.  This command finds all groups of which the
 * field is a member, performs a union of the groups, and loads all the
 * fields.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.8 $
 */
public class JDBCLoadFieldCommand {
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private Logger log;

   public JDBCLoadFieldCommand(JDBCStoreManager manager) {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }

   public void execute(JDBCCMPFieldBridge field, EntityEnterpriseContext ctx) {
      execute(getFieldGroupsUnion(field), ctx);
   }

   public JDBCCMPFieldBridge[] getFieldGroupsUnion(JDBCCMPFieldBridge field) {
      // start with a set with containing just the field
      ArrayList fields = new ArrayList(entity.getCMPFields().length);
      fields.add(field);

      // union all the groups of which field is a member
      Iterator groups = entity.getLazyLoadGroups();
      while(groups.hasNext()) {
         ArrayList group = (ArrayList)groups.next();
         if(group.contains(field)) {
            fields.addAll(group);
         }
      }
      // pass this info on
      return (JDBCCMPFieldBridge[]) fields.toArray(
            new JDBCCMPFieldBridge[fields.size()]);
   }

   public void execute(
         JDBCCMPFieldBridge[] fields,
         EntityEnterpriseContext ctx) {

      StringBuffer sql = new StringBuffer();
      sql.append("SELECT ").append(SQLUtil.getColumnNamesClause(fields));
      sql.append(" FROM ").append(entity.getTableName());
      sql.append(" WHERE ").append(
            SQLUtil.getWhereClause(entity.getJDBCPrimaryKeyFields()));

      if(entity.getMetaData().hasSelectForUpdate()) {
         sql.append(" FOR UPDATE");
      }

      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         con = manager.getDataSource().getConnection();
         
         // create the statement
         ps = con.prepareStatement(sql.toString());
         
         // set the parameters
         entity.setPrimaryKeyParameters(ps, 1, ctx.getId());

         // execute statement
         ResultSet rs = ps.executeQuery();

         // did we get results
         if(!rs.next()) {
            throw new NoSuchEntityException("Entity " + ctx.getId() + 
                  " not found");
         }
      
         // load each field and mark it clean
         int index = 1;
         for(int i=0; i<fields.length; i++) {
            index = fields[i].loadInstanceResults(rs, index, ctx);
            fields[i].setClean(ctx);
         }
      } catch(EJBException e) {
         throw e;
      } catch(Exception e) {
         throw new EJBException("Could not load field value: " + fields[0], e);
      } finally {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }
}
