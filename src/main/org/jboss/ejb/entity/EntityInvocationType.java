/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

/**
 * Type safe enumeration used for to identify the entity invocation types.
 */
public final class EntityInvocationType {
   /**
    * Get the value of a CMP or CMR field.
    */
   public static final EntityInvocationType GET_VALUE = 
         new EntityInvocationType("GET_VALUE");

   /**
    * Set the value of a CMP or CMR field.
    */
   public static final EntityInvocationType SET_VALUE = 
         new EntityInvocationType("SET_VALUE");

   /**
    * Create a new instace of the entity bean implementation class.
    */
   public static final EntityInvocationType CREATE_INSTANCE = 
         new EntityInvocationType("CREATE_INSTANCE");

   /**
    * Create callback.
    */
   public static final EntityInvocationType CREATE = 
         new EntityInvocationType("CREATE");

   /**
    * Post create callback.
    */
   public static final EntityInvocationType POST_CREATE = 
         new EntityInvocationType("POST_CREATE");

   /**
    * Remove an entity.
    */
   public static final EntityInvocationType REMOVE = 
         new EntityInvocationType("REMOVE");
         
   /**
    * Execute a query.
    */
   public static final EntityInvocationType QUERY = 
         new EntityInvocationType("QUERY");

   /**
    * Is the entity modified.
    */
   public static final EntityInvocationType IS_MODIFIED = 
         new EntityInvocationType("IS_MODIFIED");

   /**
    * Load an entity.
    */
   public static final EntityInvocationType LOAD = 
         new EntityInvocationType("LOAD");

   /**
    * Store an entity.
    */
   public static final EntityInvocationType STORE = 
         new EntityInvocationType("STORE");

   /**
    * Activate the entity.
    */
   public static final EntityInvocationType ACTIVATE = 
         new EntityInvocationType("ACTIVATE");

   /**
    * Passivate the entity.
    */
   public static final EntityInvocationType PASSIVATE = 
         new EntityInvocationType("PASSIVATE");

   /**
    * Call the EJB Timeout method when Timer sends a Timed Event
    * is send the entity.
    */
   public static final EntityInvocationType EJB_TIMEOUT = 
         new EntityInvocationType( "EJB_TIMEOUT" );


   /**
    * Name of this type.  Used for toString.
    */
   private final transient String displayName;

   /**
    * Creates an entity invocation type with the specified name.  Identity is 
    * based on the system object identity and the name is only used for 
    * printing.
    */
   private EntityInvocationType(String displayName) {
      this.displayName = displayName;
   }

   public String toString() {
      return displayName;
   }
}


