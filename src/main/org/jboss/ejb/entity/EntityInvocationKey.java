/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

/**
 * Type safe enumeration used for keys in the EntityInvocation object.
 */
public final class EntityInvocationKey 
{
   /** 
    * The entity invocation type.
    */
   public static final EntityInvocationKey TYPE = 
         new EntityInvocationKey("TYPE");
   
   /**
    * Name of this key.  Used for toString.
    */
   private final String displayName;

   /**
    * Creates an entity invocation key with the specified name.  Identity is 
    * based on the system object identity and the name is only used for 
    * printing.
    */
   private EntityInvocationKey(String displayName) {
      this.displayName = displayName;
   }

   public String toString() {
      return displayName;
   }
}


