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
import java.util.Collection;
import java.util.HashMap;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.sql.DataSource;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMP2xFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * This command establishes relationships for CMR fields that have
 * all foreign key fields mapped to primary keys columns.
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.3 $
 */
public class JDBCPostCreateEntityCommand
{
   // Attributes ------------------------------------
   private JDBCStoreManager manager;
   private JDBCEntityBridge entity;
   private List fkPartOfPkCmrFields = new ArrayList();
   private List relatedFkPartOfPkCmrFields = new ArrayList();
   private HashMap relatedEntityExistsSqlByField = new HashMap();
   private HashMap pkFieldsByFkField = new HashMap();
   private Logger log;

   // Constructors ----------------------------------
   public JDBCPostCreateEntityCommand(JDBCStoreManager manager)
   {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(
            this.getClass().getName() +
            "." +
            manager.getMetaData().getName());

      // get CMR fields that have fk that is a part of the pk
      for(Iterator iter=entity.getCMRFields().iterator(); iter.hasNext();)
      {
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)iter.next();
         if(cmrField.allFkFieldsMappedToPkFields())
         {
            // look for the pk field this cmr field is mapped to
            for(Iterator fkIter = cmrField.getForeignKeyFields().iterator();
               fkIter.hasNext();)
            {
               JDBCCMP2xFieldBridge fkField = (JDBCCMP2xFieldBridge)fkIter.next();

               for(Iterator pkIter = cmrField.getEntity().
                  getPrimaryKeyFields().iterator();
                  pkIter.hasNext();) {

                  JDBCCMP2xFieldBridge pkField = (JDBCCMP2xFieldBridge)pkIter.next();

                  // fk field mapped to pk field have the same JDBCType
                  // see JDBCCMRFieldBridge
                  if(fkField.getJDBCType() == pkField.getJDBCType())
                  {
                     log.debug(
                        "foreign key field " + entity.getEntityName() + "." + fkField.getFieldName()
                        + " is mapped to primary key field " + cmrField.getRelatedEntity().getEntityName()
                        + "." + pkField.getFieldName()
                     );
                     pkFieldsByFkField.put(fkField, pkField);
                  }
               }

               // add the field to the list
               fkPartOfPkCmrFields.add(cmrField);
               // create related-entity-exists-sql
               relatedEntityExistsSqlByField.put(cmrField, createRelatedEntityExistsSql(cmrField));
            }
         }
         else if(cmrField.getRelatedCMRField().allFkFieldsMappedToPkFields())
         {
            log.debug("found related CMR field mapped to the PK: "
               + cmrField.getFieldName());
            relatedFkPartOfPkCmrFields.add(cmrField);
         }
      }
   }

   // Public ----------------------------------------
   public Object execute(Method m,
                         Object[] args,
                         EntityEnterpriseContext ctx)
      throws CreateException
   {
      // check native CMR fields mapped to the pk
      for(Iterator iter=fkPartOfPkCmrFields.iterator(); iter.hasNext();)
      {
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)iter.next();

         // construct related pk value from this pk value
         Class relatedPkClass = cmrField.getRelatedJDBCEntity().getPrimaryKeyClass();
         Object relatedPkValue = null;

         for(Iterator fkIter = cmrField.getForeignKeyFields().iterator();
            fkIter.hasNext();)
         {
            JDBCCMP2xFieldBridge fkField = (JDBCCMP2xFieldBridge)fkIter.next();
            JDBCCMP2xFieldBridge pkField =
               (JDBCCMP2xFieldBridge)pkFieldsByFkField.get(fkField);

            Object pkFieldValue = pkField.getInstanceValue(ctx);
            // fkField.setInstanceValue(ctx, pkFieldValue);
            relatedPkValue = fkField.setPrimaryKeyValue(relatedPkValue, pkFieldValue);
         }

         // create relationship if related entity exists
         if(relatedEntityExists(
            cmrField.getRelatedJDBCEntity(),
            (String)relatedEntityExistsSqlByField.get(cmrField),
            relatedPkValue))
         {
            cmrField.createRelationLinks(ctx, relatedPkValue);
         }
      }

      // check related CMR fields mapped to the pk
      for(Iterator iter=relatedFkPartOfPkCmrFields.iterator(); iter.hasNext();)
      {
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)iter.next();

         // load the value from the database
         Collection rel = manager.loadRelation(cmrField, ctx);

         for(Iterator relIter = rel.iterator(); relIter.hasNext();) {
            Object relatedId = relIter.next();
            log.debug("JDBCPostCreateEntityCommand> execute: "
               + "creating relationship with id=" + relatedId);
            cmrField.createRelationLinks(ctx, relatedId);
         }
      }
      return null;
   }

   // Private ---------------------------------------
   private String createRelatedEntityExistsSql(JDBCCMRFieldBridge cmrField)
   {
      JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
      StringBuffer sql = new StringBuffer();
      sql.append("SELECT COUNT(*)");
      sql.append(" FROM ").append(relatedEntity.getTableName());
      sql.append(" WHERE ");
      sql.append(SQLUtil.getWhereClause(relatedEntity.getPrimaryKeyFields()));

      log.debug("Exists SQL for CMR " + cmrField.getFieldName()
         + ": " + sql.toString());

      return sql.toString();
   }

   private boolean relatedEntityExists(JDBCEntityBridge relatedEntity,
                                       String sql,
                                       Object pk)
      throws CreateException
   {
      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         // get the connection
         DataSource dataSource = relatedEntity.getDataSource();
         con = dataSource.getConnection();

         // create the statement
         log.debug("Executing SQL: " + sql);
         ps = con.prepareStatement(sql);

         // set the parameters
         relatedEntity.setPrimaryKeyParameters(ps, 1, pk);

         // execute statement
         ResultSet rs = ps.executeQuery();
         if(!rs.next())
         {
            throw new CreateException("Error checking if entity exists: " +
                  "result set contains no rows");
         }

         // did any rows match
         return rs.getInt(1) > 0;
      }
      catch(CreateException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         log.error("Error checking if entity exists", e);
         throw new CreateException("Error checking if entity exists:" + e);
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }
   }
}
