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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityCommandMetaData;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.logging.Logger;

/**
 * JDBCPkSqlCreateCommand executes an INSERT INTO query.
 * This command executes sql statement provided by a user to fetch
 * the next primary key value.
 * It is supposed to be used against databases with sequence support.
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public class JDBCPkSqlCreateCommand
   extends JDBCCreateEntityCommand
{

   // Attributes ---------------------------------------------

   protected String pkSql;

   // JDBCCreateEntityCommand overrides ----------------------

   public void init(JDBCStoreManager manager)
      throws DeploymentException
   {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger( this.getClass().getName() + 
               "." + manager.getMetaData().getName());

      // set create allowed
      createAllowed = true;
      List fields = entity.getFields();
      for( Iterator iter = fields.iterator(); iter.hasNext(); )
      {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         if( field.isPrimaryKeyMember() && field.isReadOnly() )
         {
            createAllowed = false;
            break;
         }
      }
      
      if( createAllowed )
      {
         insertFields = getInsertFields();
         entityExistsSQL = createEntityExistsSQL();
         insertEntitySQL = createInsertEntitySQL();
         log.debug("Entity Exists SQL: " + entityExistsSQL);
         log.debug("Insert Entity SQL: " + insertEntitySQL);
      }
      else
      {
         log.debug("Create will not be allowed.");
      }

      // fetch attributes
      JDBCEntityCommandMetaData entityCommand = manager.
         getMetaData().getEntityCommand();
      if( entityCommand == null )
      {
         throw new DeploymentException(
            "entity command isn't set for entity " + entity.getEntityName() );
      }

      pkSql = entityCommand.getAttribute( "pk-sql" );
      if( pkSql == null )
      {
         throw new DeploymentException( "pk-sql attribute "
            + "isn't set for entity " + entity.getEntityName() );
      }
   }

   public Object execute( Method m,
                          Object[] args,
                          EntityEnterpriseContext ctx )
      throws CreateException
   {
      if( !createAllowed )
      {
         throw new CreateException("Creation is not allowed because a "
            + "primary key field is read only.");
      }

      // fetch primary key value
      Object pk = fetchNextPrimaryKey();
      log.debug("Create: pk="+pk);

      // check for duplication
      if( entityExists(pk) )
      {
         throw new DuplicateKeyException("Entity with primary key "
            + pk + " already exists");
      }

      // set the value for pk in the context
      for( Iterator iter = entity.getPrimaryKeyFields().iterator();
         iter.hasNext(); )
      {
         JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge) iter.next();
         if( cmpField.isUnknownPk() )
         {
            cmpField.setInstanceValue( ctx, pk );
            break;
         }
      }

      // insert the raw
      insertEntity(ctx);

      // return pk value
      return pk;         
   }

   protected Object fetchNextPrimaryKey()
      throws CreateException {

      Object pk = null;
      Connection con = null;
      PreparedStatement ps = null;
      try {
         // get the connection
         DataSource dataSource = entity.getDataSource();
         con = dataSource.getConnection();
         
         // create the statement
         log.debug("Executing SQL: " + pkSql);
         ps = con.prepareStatement( pkSql );
         
         // execute statement
         ResultSet rs = ps.executeQuery();
         if(!rs.next()) {
            throw new CreateException("Error fetching next primary key value: "
               + "result set contains no rows");
         }
      
         pk = rs.getObject( 1 );
      }
      catch( CreateException ce )
      {
        throw ce;
      }
      catch( Exception e )
      {
         log.error("Error fetching the next primary key value", e);
         throw new CreateException("Error checking if entity exists:" + e);
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      return pk;
   }
}
