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

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;

import org.jboss.ejb.plugins.cmp.bridge.CMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;


/**
 * JDBCCMPFieldBridge represents one CMP field. This implementations of 
 * this interface handles setting are responsible for setting statement
 * parameters and loading results for instance values and primary
 * keys.
 *
 * Life-cycle:
 *      Tied to the EntityBridge.
 *
 * Multiplicity:
 *      One for each entity bean cmp field.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.8 $
 */
public interface JDBCCMPFieldBridge extends CMPFieldBridge {

   /**
    * Get metadata for the field.
    */
   public JDBCCMPFieldMetaData getMetaData();

   /**
    * Has current data read timed out?
    */
   public boolean isReadTimedOut(EntityEnterpriseContext ctx);
   
   /**
    * Resets any persistence data maintained in the context.
    */
   public void resetPersistenceContext(EntityEnterpriseContext ctx);
   
   /**
    * Gets the JDBC type of this field.
    */
   public JDBCType getJDBCType();

   /**
    * Sets the prepared statement parameters with the data from the 
    * instance associated with the context.
    */
   public int setInstanceParameters(PreparedStatement ps, int parameterIndex, EntityEnterpriseContext ctx);

   /**
    * Sets the prepared statement parameters with the data from the 
    * primary key.
    */
   public int setPrimaryKeyParameters(PreparedStatement ps, int parameterIndex, Object primaryKey) throws IllegalArgumentException;
   
   /**
    * Sets the prepared statement parameters with the data from the 
    * object. The object must be the type of this field.
    */
   public int setArgumentParameters(PreparedStatement ps, int parameterIndex, Object arg);

   /**
    * Loads the data from result set into the instance associated with the context.
    */
   public int loadInstanceResults(ResultSet rs, int parameterIndex, EntityEnterpriseContext ctx);
   
   /**
    * Loads the data from result set into the primary key object.
    */
   public int loadPrimaryKeyResults(ResultSet rs, int parameterIndex, Object[] pkRef) throws IllegalArgumentException;

   /**
    * Loads the value of this cmp field from result set into argument referance.
    */
   public int loadArgumentResults(ResultSet rs, int parameterIndex, Object[] argumentRef) throws IllegalArgumentException;
}                                         