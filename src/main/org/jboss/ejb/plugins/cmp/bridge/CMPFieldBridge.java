/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp.bridge;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * CMPFieldBridge represents one cmp field for one entity. 
 *
 * Life-cycle:
 *      Tied to the EntityBridge.
 *
 * Multiplicity:   
 *      One for each entity bean cmp field.       
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */                            
public interface CMPFieldBridge {
   /**
    * Gets the name of this cmp.
    * @return the name of this field
    */
   public String getFieldName();
   
   /**
    * Gets the java class type of the field.
    * @return the java class type of this field
    */
   public Class getFieldType();

   /**
    * Is this field a member of the primary key.
    * @return true if this field is a member of the primary key
    */
   public boolean isPrimaryKeyMember();

   /**
    * Is this field read only.
    * @return true if this field is read only
    */ 
   public boolean isReadOnly();
      
   /**
    * Gets the value of this field for the specified intance context.
    * @param ctx the context for which this field's value should be fetched
    * @return the value of this field
    */
   public Object getInstanceValue(EntityEnterpriseContext ctx);
      
   /**
    * Sets the value of this field for the specified intance context.
    * @param ctx the context for which this field's value should be set
    * @param value the new value of this field
    */
   public void setInstanceValue(EntityEnterpriseContext ctx, Object value);

   /**
    * Gets the value of this field in the specified primaryKey object.
    * @param primaryKey the primary key object from which this fields value 
    *    will be extracted
    * @return the value of this field in the primaryKey object
    */
   public Object getPrimaryKeyValue(Object primaryKey)
         throws IllegalArgumentException;

   /**
    * Sets the value of this field to the specified value in the 
    * specified primaryKey object.
    * @param primaryKey the primary key object which the value 
    *    will be inserted
    * @param value the value for field that will be set in the pk
    * @return the updated primary key object; the actual object may 
    *    change not just the value
    */
    public Object setPrimaryKeyValue(Object primaryKey, Object value)
         throws IllegalArgumentException;

   /**
    * Set CMPFieldValue to Java default value (i.e., 0 or null).
    */
   public void initInstance(EntityEnterpriseContext ctx);

   /**
    * Has the value of this field changes since the last time clean was called.
    */
   public boolean isDirty(EntityEnterpriseContext ctx);   

   /**
    * Mark this field as clean.
    */
   public void setClean(EntityEnterpriseContext ctx);
}
