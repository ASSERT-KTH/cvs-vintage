/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.jdbc.bridge;

import java.lang.reflect.Field;
import java.util.Map;

import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.cmp.jdbc.JDBCContext;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCType;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCCMPFieldMetaData;

/**
 * JDBCCMP2xFieldBridge is a concrete implementation of JDBCCMPFieldBridge for 
 * CMP version 2.x. Instance data is stored in the entity persistence context.
 * Whenever a field is changed it is compared to the current value and sets
 * a dirty flag if the value has changed.
 *
 * Life-cycle:
 *      Tied to the EntityBridge.
 *
 * Multiplicity:   
 *      One for each entity bean cmp field.       
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.15 $
 */                            
public class JDBCCMP2xFieldBridge extends JDBCAbstractCMPFieldBridge {

   public JDBCCMP2xFieldBridge(
         JDBCStoreManager manager,
         JDBCCMPFieldMetaData metadata) throws DeploymentException {

      super(manager, metadata);
   }

   public JDBCCMP2xFieldBridge(
         JDBCStoreManager manager,
         JDBCCMPFieldMetaData metadata,
         JDBCType jdbcType) throws DeploymentException {

      super(manager, metadata, jdbcType);
   }

   /**
    * This constructor is used to create a foreign key field that is
    * a part of primary key field. See JDBCCMRFieldBridge.
    */
   public JDBCCMP2xFieldBridge(
      JDBCStoreManager manager,
      String fieldName,
      Class fieldType,
      JDBCType jdbcType,
      boolean readOnly,
      long readTimeOut,
      boolean primaryKeyMember,
      Class primaryKeyClass,
      Field primaryKeyField,
      boolean isUnknownPk)
   throws DeploymentException {
      super(
         manager,
         fieldName,
         fieldType,
         jdbcType,
         readOnly,
         readTimeOut,
         primaryKeyMember,
         primaryKeyClass,
         primaryKeyField,
         isUnknownPk
      );
   }

   public Object getInstanceValue(EntityEnterpriseContext ctx) {
      FieldState fieldState = getFieldState(ctx);
      if(!fieldState.isLoaded) {
         getManager().loadField(this, ctx);
         if(!fieldState.isLoaded) {
            throw new EJBException("Could not load field value: " +
               getFieldName());
         }

         // notify the optimistic lock
         // not used
         // getManager().fieldStateEventCallback(ctx, CMPMessage.LOADED, this, fieldState.value);
      }

      // notify optimistic lock
      getManager().fieldStateEventCallback(ctx, CMPMessage.ACCESSED, this, fieldState.value);

      return fieldState.value;
   }
   
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value) {
      FieldState fieldState = getFieldState(ctx);

      // short-circuit to avoid repetive comparisons
      // if it is not currently loaded or it is already dirty or 
      // if it has changed
      fieldState.isDirty = !fieldState.isLoaded || fieldState.isDirty || 
            changed(fieldState.value, value);

      // notify optimistic lock, but only if the bean is created
      if(ctx.getId() != null) {
         if(fieldState.isLoaded()) {
            getManager().fieldStateEventCallback(ctx, CMPMessage.CHANGED, this, fieldState.value);
         }
         else {
            // not used
            //getManager().fieldStateEventCallback(ctx, CMPMessage.LOADED, this, value);
         }
      }

      // we are loading the field right now so it isLoaded
      fieldState.isLoaded = true;

      // update current value
      fieldState.value = value;
   }

   public boolean isLoaded(EntityEnterpriseContext ctx) {
      return getFieldState(ctx).isLoaded;
   }
   
   /**
    * Has the value of this field changes since the last time clean was called.
    */
   public boolean isDirty(EntityEnterpriseContext ctx) {
      // read only and primary key fields are never dirty
      if(isReadOnly() || isPrimaryKeyMember()) {
         return false; 
      }
      return getFieldState(ctx).isDirty;
   }
   
   /**
    * Mark this field as clean. Saves the current state in context, so it 
    * can be compared when isDirty is called.
    */
   public void setClean(EntityEnterpriseContext ctx) {
      FieldState fieldState = getFieldState(ctx);
      fieldState.isDirty = false;

      // update last read time
      if(isReadOnly()) {
         fieldState.lastRead = System.currentTimeMillis();
      }
   }
   
   public void resetPersistenceContext(EntityEnterpriseContext ctx) {
      if(isReadTimedOut(ctx)) {
         JDBCContext jdbcCtx = (JDBCContext)ctx.getPersistenceContext();
         jdbcCtx.put(this, new FieldState());

         // notify optimistic lock
         // not used
         //getManager().fieldStateEventCallback(ctx, CMPMessage.RESETTED, this, null);
      }
   }
   
   public boolean isReadTimedOut(EntityEnterpriseContext ctx) {
      // if we are read/write then we are always timed out
      if(!isReadOnly()) {
         return true;
      }

      // if read-time-out is -1 then we never time out.
      if(getReadTimeOut() == -1) {
         return false;
      }

      long readInterval = System.currentTimeMillis() - 
            getFieldState(ctx).lastRead; 
      return readInterval >= getReadTimeOut();
   }

   public FieldState getFieldState(EntityEnterpriseContext ctx) {
      JDBCContext jdbcCtx = (JDBCContext)ctx.getPersistenceContext();
      FieldState fieldState = (FieldState)jdbcCtx.get(this);
      if(fieldState == null) {
         fieldState = new FieldState();
         jdbcCtx.put(this, fieldState);
      }
      return fieldState;
   }

   public static class FieldState {
      private Object value;
      private boolean isLoaded = false;
      private boolean isDirty = false;
      private long lastRead = -1;
      
      public Object getValue() {
         return value;
      }
      public boolean isLoaded() {
         return isLoaded;
      }
      public boolean isDirty() {
         return isDirty;
      }
   }
}
