/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.reflect.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil;

import org.jboss.logging.Logger;

/**
 * JDBCAbstractCMPFieldBridge is the default implementation of 
 * JDBCCMPFieldBridge. Most of the heavy lifting of this command is handled
 * by JDBCUtil. It is left to subclasses to implement the logic for getting
 * and setting instance values and dirty checking, as this is dependent on 
 * the CMP version used.
 *
 * Life-cycle:
 *      Tied to the EntityBridge.
 *
 * Multiplicity:   
 *      One for each entity bean cmp field.       
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:loubyansky@ua.fm">Alex Loubyansky</a>
 * @version $Revision: 1.16 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>20021023 Steve Coy:</b>
 * <ul>
 * <li>Changed {@link #loadArgumentResults} so that it passes the jdbc type to
 *     {@link org.jboss.ejb.plugins.cmp.jdbc.JDBCUtil.getResult}.
 * </ul>
 */                            
public abstract class JDBCAbstractCMPFieldBridge implements JDBCCMPFieldBridge {
   private final JDBCStoreManager manager;
   private final Logger log;
   private final JDBCType jdbcType;
   private final String fieldName;
   private final Class fieldType;
   private final boolean readOnly;
   private final long readTimeOut;
   private final boolean primaryKeyMember;
   private final Class primaryKeyClass;
   private final Field primaryKeyField;
   private final boolean unknownPk;
   
   public JDBCAbstractCMPFieldBridge(
         JDBCStoreManager manager, 
         JDBCCMPFieldMetaData metadata) throws DeploymentException {      

      this(manager, 
            metadata, 
            manager.getJDBCTypeFactory().getJDBCType(metadata));
   }
   
   public JDBCAbstractCMPFieldBridge(
         JDBCStoreManager manager,
         JDBCCMPFieldMetaData metadata,
         JDBCType jdbcType) throws DeploymentException {

      this(
            manager,
            metadata.getFieldName(),
            metadata.getFieldType(),
            jdbcType,
            metadata.isReadOnly(),
            metadata.getReadTimeOut(),
            metadata.isPrimaryKeyMember(),
            metadata.getEntity().getPrimaryKeyClass(),
            metadata.getPrimaryKeyField(),
            metadata.isUnknownPkField());
   }

   public JDBCAbstractCMPFieldBridge(
         JDBCStoreManager manager,
         String fieldName,
         Class fieldType,
         JDBCType jdbcType,
         boolean readOnly,
         long readTimeOut,
         boolean primaryKeyMember,
         Class primaryKeyClass,
         Field primaryKeyField,
         boolean unknownPk) {

      this.manager = manager;

      this.fieldName = fieldName;
      this.fieldType = fieldType;
      this.jdbcType = jdbcType;

      this.readOnly = readOnly;
      this.readTimeOut = readTimeOut;

      this.primaryKeyMember = primaryKeyMember;
      this.primaryKeyClass = primaryKeyClass;
      this.primaryKeyField = primaryKeyField;
      
      this.unknownPk = unknownPk;

      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName() +
            "." +
            fieldName);

   }

   public JDBCStoreManager getManager() {
      return manager;
   }

   public String getFieldName() {
      return fieldName;
   }
   
   public JDBCType getJDBCType() {
      return jdbcType;
   }

   public Class getFieldType() {
      return fieldType;
   }

   public boolean isPrimaryKeyMember() {
      return primaryKeyMember;
   }

   public Class getPrimaryKeyClass() {
      return primaryKeyClass;
   }

   public Field getPrimaryKeyField() {
      return primaryKeyField;
   }

   public boolean isReadOnly() {
      return readOnly;
   }

   public long getReadTimeOut() {
      return readTimeOut;
   }

   public boolean isUnknownPk() {
      return unknownPk;
   }

   public abstract boolean isReadTimedOut(EntityEnterpriseContext ctx);
   
   public Object getValue(EntityEnterpriseContext ctx) {
      // no user checks yet, but this is where they would go
      return getInstanceValue(ctx);
   }

   public void setValue(EntityEnterpriseContext ctx, Object value) {
      if(isReadOnly()) {
         throw new EJBException("Field is read-only: " +
               "fieldName=" + getFieldName());
      }
      if(isPrimaryKeyMember() && manager.getEntityBridge().isCreated(ctx)) {
         throw new IllegalStateException("A CMP field that is a member " +
               "of the primary key can only be set in ejbCreate " +
               "[EJB 2.0 Spec. 10.3.5].");
      }
      setInstanceValue(ctx, value);      
   }

   public Object getPrimaryKeyValue(Object primaryKey) 
         throws IllegalArgumentException {

      try {
         if(getPrimaryKeyField() != null) {
            if(primaryKey == null) {
               return null;
            }
            
            // Extract this field's value from the primary key.
            return getPrimaryKeyField().get(primaryKey);
         } else {
            // This field is the primary key, so no extraction is necessary.
            return primaryKey;
         }
      } catch(Exception e) {
         // Non recoverable internal exception
         throw new EJBException("Internal error getting primary key " +
               "field member " + getFieldName(), e);
      }
   }

   public Object setPrimaryKeyValue(Object primaryKey, Object value)
         throws IllegalArgumentException {

      try {
         if(getPrimaryKeyField() != null) {
            // if we are tring to set a null value 
            // into a null pk, we are already done.
            if(value == null && primaryKey == null) {
               return null;
            }
            
            // if we don't have a pk object yet create one
            if(primaryKey == null) {
               primaryKey = getPrimaryKeyClass().newInstance();
            }
            
            // Set this field's value into the primary key object.
            getPrimaryKeyField().set(primaryKey, value);
            return primaryKey;
         } else {
            // This field is the primary key, so no extraction is necessary.
            return value;
         }
      } catch(Exception e) {
         // Non recoverable internal exception
         throw new EJBException("Internal error setting instance field " +
               getFieldName(), e);
      }
   }

   public abstract void resetPersistenceContext(EntityEnterpriseContext ctx);
   
   /**
    * Set CMPFieldValue to Java default value (i.e., 0 or null).
    */
   public void initInstance(EntityEnterpriseContext ctx) {
      if(!isReadOnly()) {
         Object value;
         if(getFieldType().equals(boolean.class))  {
            value = Boolean.FALSE;
         } else if(getFieldType().equals(byte.class))  {
            value = new Byte((byte)0);
         } else if (getFieldType().equals(int.class))  {
            value = new Integer(0);
         } else if (getFieldType().equals(long.class))  {
            value = new Long(0L);
         } else if (getFieldType().equals(short.class))  {
            value = new Short((short)0);
         } else if (getFieldType().equals(char.class))  {
            value = new Character('\u0000');
         } else if (getFieldType().equals(double.class))  {
            value = new Double(0d); 
         } else if (getFieldType().equals(float.class))  {
            value = new Float(0f);
         } else  {
            value = null;
         }
   
         setInstanceValue(ctx, value);
      }
   }      

   public int setInstanceParameters(
         PreparedStatement ps,
         int parameterIndex, 
         EntityEnterpriseContext ctx) {

      Object instanceValue = getInstanceValue(ctx);
      return setArgumentParameters(ps, parameterIndex, instanceValue);
   }   

   public int setPrimaryKeyParameters(
         PreparedStatement ps, 
         int parameterIndex, 
         Object primaryKey) throws IllegalArgumentException {

      Object primaryKeyValue = getPrimaryKeyValue(primaryKey);
      return setArgumentParameters(ps, parameterIndex, primaryKeyValue);
   }
   
   public int setArgumentParameters(
         PreparedStatement ps, 
         int parameterIndex, 
         Object arg) {

      try {
         int[] jdbcTypes = getJDBCType().getJDBCTypes();
         for(int i=0; i<jdbcTypes.length; i++) {
            Object columnValue = getJDBCType().getColumnValue(i, arg);
            JDBCUtil.setParameter(log, ps, parameterIndex++, jdbcTypes[i], 
                  columnValue);
         }
         return parameterIndex;
      } catch(SQLException e) {
         // Non recoverable internal exception
         throw new EJBException("Internal error setting parameters for field " +
               getFieldName(), e);
      }
   }   

   public int loadInstanceResults(
         ResultSet rs, 
         int parameterIndex, 
         EntityEnterpriseContext ctx) {

      try {
         // value of this field,  will be filled in below
         Object[] argumentRef = new Object[1];
         
         // load the cmp field value from the result set
         parameterIndex = loadArgumentResults(rs, parameterIndex, argumentRef);

         // set the value into the context
         setInstanceValue(ctx, argumentRef[0]);
         return parameterIndex;
      } catch(EJBException e) {
         // to avoid double wrap of EJBExceptions
         throw e;
      } catch(Exception e) {
         // Non recoverable internal exception
         throw new EJBException("Internal error getting results for field " +
               getFieldName(), e);
      }
   }      
   
   public int loadPrimaryKeyResults(
         ResultSet rs,
         int parameterIndex,
         Object[] pkRef) throws IllegalArgumentException {

      // value of this field,  will be filled in below
      Object[] argumentRef = new Object[1];
      
      // load the cmp field value from the result set
      parameterIndex = loadArgumentResults(rs, parameterIndex, argumentRef);
               
      // set the value of this field into the pk
      pkRef[0] = setPrimaryKeyValue(pkRef[0], argumentRef[0]);
      
      // retrun the updated parameterIndex
      return parameterIndex;
   }      

   public int loadArgumentResults(
         ResultSet rs,
         int parameterIndex, 
         Object[] argumentRef) throws IllegalArgumentException {

      try {
         // value of this field,  will be filled in below
         // set the value of this field into the pk
         argumentRef[0] = null;
         
         // update the value from the result set
         Class[] javaTypes = getJDBCType().getJavaTypes();
         int[] jdbcTypes = getJDBCType().getJDBCTypes();
         for(int i=0; i<javaTypes.length; i++) {
            Object columnValue = 
                  JDBCUtil.getResult(log, rs, parameterIndex++, jdbcTypes[i], javaTypes[i]);
            argumentRef[0] = 
                  getJDBCType().setColumnValue(i, argumentRef[0], columnValue);
         }
                  
         // retrun the updated parameterIndex
         return parameterIndex;
      } catch(SQLException e) {
         // Non recoverable internal exception
         throw new EJBException("Internal error getting results " +
               "for field member " + getFieldName(), e);
      }
   }
   
   protected final boolean changed(Object current, Object old) {
      return   
         // TRUE if I am null and I wasn't before   
         (current == null && old != null) ||   
         
         // TRUE if I was null and now I'm not
         (current != null &&
            ( old == null || 
               // TRUE if i'm not equal to my oldstate tied to last 
               // check to assure that current is not null 
              (!current.equals(old)) )
         );                                           
            
   }
}
                                         
