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
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCCreateEntityCommand;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;
import org.jboss.logging.Logger;

/**
 * JDBCAbstractVendorCreateCommand executes an INSERT INTO query.
 * Subclasses command must provide implementation
 * to fetch the generated key.
 * It works under JDK versions 1.3 and 1.4.
 *
 * @author <a href="mailto:loubyansky@hotmail.com">Alex Loubyansky</a>
 * @author <a href="mailto:julien_viet@yahoo.fr">Julien Viet</a>
 *
 * @version $Revision: 1.1 $
 */
public abstract class JDBCAbstractVendorCreateCommand
   extends JDBCCreateEntityCommand
{

   /**
    * Subclass must return the PK associed to the statement.
    * @param statement the statement that executed the INSERT INTO query.
    */
   protected abstract Object fetchPK(PreparedStatement statement) throws Exception;

   /**
    * This method includes only non primary key fields
    */
   protected List getInsertFields() {
      List fields = entity.getFields();
      List insertFields = new ArrayList(fields.size());

      for(Iterator iter = fields.iterator(); iter.hasNext(); ) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         if(!field.isReadOnly() && !field.isPrimaryKeyMember()) {
            insertFields.add(field);
         } 
      }
      return insertFields;
   }

   public Object execute(Method m,
                         Object[] args,
                         EntityEnterpriseContext ctx)
      throws CreateException {

      // primary key to return
      Object pk = null;

      if(!createAllowed) {
         throw new CreateException("Creation is not allowed because a " +
               "primary key field is read only.");
      }

      // the key is generated by the database
      insertEntity(ctx);
      pk = entity.extractPrimaryKeyFromInstance(ctx);
      log.debug("Created: pk="+pk);

      return pk;         
   }

   /**
    * Assume vendor won't generate duplicate keys
    */
   protected boolean entityExists(Object pk)
      throws CreateException
   {
      return false;
   }

   /**
    * This method executes vendor specific code
    */
   protected void insertEntity(EntityEnterpriseContext ctx)
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

         // fetch the pk
         Object pk = fetchPK(ps);

         if( pk == null ) {
            throw new Exception( "Primary key isn't generated." );
         }

         // write pk fields to the context
         for( Iterator iter = entity.getPrimaryKeyFields().iterator();
            iter.hasNext(); ) {
            JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge) iter.next();
            cmpField.setInstanceValue( ctx, pk );
            break;
         }
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
      log.debug("Rows affected = " + rowsAffected);

      // Mark the inserted fields as clean.
      for(Iterator iter = insertFields.iterator(); iter.hasNext(); ) {
         JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
         field.setClean(ctx);
      }
   }
}
