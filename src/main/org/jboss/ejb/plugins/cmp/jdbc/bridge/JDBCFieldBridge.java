/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.bridge.FieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;

public interface JDBCFieldBridge extends FieldBridge {
   /**
    * Gets the JDBC type of this field.
    */
   public JDBCType getJDBCType();

   /**
    * Is this field a member of the primary key.
    * @return true if this field is a member of the primary key
    */
   public boolean isPrimaryKeyMember();

   /**
    * Has current data read timed out?
    */
   public boolean isReadTimedOut(EntityEnterpriseContext ctx);
   
   /**
    * Has the data been loaded?
    */
   public boolean isLoaded(EntityEnterpriseContext ctx);

   /**
    * Set CMPFieldValue to Java default value (i.e., 0 or null).
    */
   public void initInstance(EntityEnterpriseContext ctx);

   /**
    * Resets any persistence data maintained in the context.
    */
   public void resetPersistenceContext(EntityEnterpriseContext ctx);

   /**
    * Sets the prepared statement parameters with the data from the 
    * instance associated with the context.
    */
   public int setInstanceParameters(
         PreparedStatement ps, 
         int parameterIndex,
         EntityEnterpriseContext ctx);

   /**
    * Loads the data from result set into the instance associated with 
    * the specified context.
    */
   public int loadInstanceResults(
         ResultSet rs,
         int parameterIndex,
         EntityEnterpriseContext ctx);

   /**
    * Loads the value of this cmp field from result set into argument referance.
    */
   public int loadArgumentResults(
         ResultSet rs,
         int parameterIndex,
         Object[] argumentRef);

   /**
    * Has the value of this field changes since the last time clean was called.
    */
   public boolean isDirty(EntityEnterpriseContext ctx);   

   /**
    * Mark this field as clean.
    */
   public void setClean(EntityEnterpriseContext ctx);
}
