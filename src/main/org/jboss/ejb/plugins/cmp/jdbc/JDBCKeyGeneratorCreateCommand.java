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
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCEntityCommandMetaData;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;
import org.jboss.ejb.plugins.lock.JDBCOptimisticLock;
import org.jboss.logging.Logger;

/**
 * JDBCKeyGeneratorCreateCommand executes an INSERT INTO query.
 * This command will ask the corresponding key generator for a
 * value for the primary key before inserting the row.
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.3 $
 */
public class JDBCKeyGeneratorCreateCommand
   extends JDBCCreateEntityCommand
{
   // Attributes ---------------------------------------------
   protected KeyGenerator keyGenerator;

   // JDBCCreateEntityCommand overrides ----------------------
   public void init(JDBCStoreManager manager)
      throws DeploymentException
   {
      this.manager = manager;
      entity = manager.getEntityBridge();

      // Create the Log
      log = Logger.getLogger(this.getClass().getName() +
         "." + manager.getMetaData().getName());

      // set create allowed
      createAllowed = true;
      List fields = entity.getFields();
      for(Iterator iter = fields.iterator(); iter.hasNext();)
      {
         JDBCFieldBridge field = (JDBCFieldBridge) iter.next();
         if(field.isPrimaryKeyMember() && field.isReadOnly())
         {
            createAllowed = false;
            break;
         }
      }

      if(createAllowed)
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
      if(entityCommand == null)
      {
         throw new DeploymentException(
            "entity command isn't set for entity " + entity.getEntityName());
      }

      String factoryName = entityCommand.getAttribute("key-generator-factory");
      if(factoryName == null)
      {
         throw new DeploymentException("key-generator-factory attribute "
            + "isn't set for entity " + entity.getEntityName());
      }

      try
      {
         KeyGeneratorFactory keyGeneratorFactory = (KeyGeneratorFactory)
            new InitialContext().lookup(factoryName);
         keyGenerator = keyGeneratorFactory.getKeyGenerator();
      }
      catch(NamingException e)
      {
         throw new DeploymentException(
            "Error: can't find key generator factory: " + factoryName, e);
      }
      catch(Exception e)
      {
         throw new DeploymentException(
            "Error: can't create key generator instance; "
            + " key generator factory: " + factoryName, e);
      }
   }

   public Object execute(Method m,
                         Object[] args,
                         EntityEnterpriseContext ctx)
      throws CreateException
   {
      if(!createAllowed)
      {
         throw new CreateException("Creation is not allowed because a "
            + "primary key field is read only.");
      }

      //KeyGenerator keyGenerator = entity.getKeyGenerator();
      if(keyGenerator == null)
         throw new CreateException("Key generator is not set");

      // generate primary key
      Object pk = keyGenerator.generateKey();
      log.debug("Create: pk=" + pk);

      // check for duplication
      if(entityExists(pk))
      {
         throw new DuplicateKeyException("Entity with primary key "
            + pk + " already exists");
      }

      // set the value for pk in the context
      for(Iterator iter = entity.getPrimaryKeyFields().iterator();
          iter.hasNext();)
      {
         JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge) iter.next();
         if(cmpField.isPrimaryKeyMember())
         {
            cmpField.setInstanceValue(ctx, pk);
            break;
         }
      }

      // insert the raw
      insertEntity(ctx);

      // return pk value
      return pk;
   }

   protected void insertEntity(EntityEnterpriseContext ctx)
      throws CreateException
   {

      Connection con = null;
      PreparedStatement ps = null;
      int rowsAffected = 0;
      try
      {
         // get the connection
         DataSource dataSource = entity.getDataSource();
         con = dataSource.getConnection();

         // create the statement
         log.debug("Executing SQL: " + insertEntitySQL);
         ps = con.prepareStatement(insertEntitySQL);

         // set initial values for optimistic locking version field
         JDBCCMPFieldBridge versionField = entity.getVersionField();
         if(versionField != null)
         {
            versionField.setInstanceValue(
               ctx, JDBCOptimisticLock.getInitialValue(versionField)
            );
         }

         // set the parameters
         int index = 1;
         for(Iterator iter = insertFields.iterator(); iter.hasNext();)
         {
            JDBCFieldBridge field = (JDBCFieldBridge) iter.next();
            index = field.setInstanceParameters(ps, index, ctx);
         }

         // execute statement
         rowsAffected = ps.executeUpdate();
      }
      catch(Exception e)
      {
         log.error("Could not create entity", e);
         throw new CreateException("Could not create entity:" + e);
      }
      finally
      {
         JDBCUtil.safeClose(ps);
         JDBCUtil.safeClose(con);
      }

      // check results
      if(rowsAffected != 1)
      {
         throw new CreateException("Insertion failed. Expected one " +
            "affected row: rowsAffected=" + rowsAffected +
            "id=" + ctx.getId());
      }
      log.debug("Rows affected = " + rowsAffected);

      // Mark the inserted fields as clean
      // and notify the optimistic lock
      for(Iterator iter = insertFields.iterator(); iter.hasNext();)
      {
         JDBCFieldBridge field = (JDBCFieldBridge) iter.next();
         field.setClean(ctx);

         // not used
         // manager.fieldStateEventCallback(ctx, CMPMessage.CREATED, field, field.getInstanceValue(ctx));
      }
   }
}
