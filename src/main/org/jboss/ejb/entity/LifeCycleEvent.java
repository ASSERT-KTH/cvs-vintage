/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.io.Serializable;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.PayloadKey;

/**
 * Type safe enumeration used for to identify entity life cycle events.
 */
public final class LifeCycleEvent 
{
   /** 
    * The key for the entity life cycle event in the invocation.
    */
   public static final LifeCycleEventKey KEY = new LifeCycleEventKey();

   /**
    * Helper method that gets the life cycle event from the invocation.
    * @param invocation the invocation from which the life cycle event it to 
    * be retrieved
    * @return the life cycle event in the invocation or null if the invocation
    * does not contain a life cycle event
    */
   public static LifeCycleEvent get(Invocation invocation)
   {
      return (LifeCycleEvent)invocation.getValue(KEY);
   }

   /**
    * Helper method that sets the life cycle event from the invocation.
    * @param invocation the invocation into which the life cycle event will 
    * be set
    * @param event the life cycle event to set into the invocation
    */
   public static void set(Invocation invocation, LifeCycleEvent event)
   {
      invocation.setValue(KEY, event, PayloadKey.TRANSIENT);
   }

   /**
    * Create a new instace of the entity bean implementation class.
    */
   public static final LifeCycleEvent CREATE_INSTANCE = 
         new LifeCycleEvent("CREATE_INSTANCE");

   /** 
    * The create life cycle event.
    */
   public static final LifeCycleEvent CREATE = 
         new LifeCycleEvent("CREATE");

   /**
    * Post create callback.
    */
   public static final LifeCycleEvent POST_CREATE = 
         new LifeCycleEvent("POST_CREATE");

   /**
    * Remove an entity.
    */
   public static final LifeCycleEvent REMOVE = 
         new LifeCycleEvent("REMOVE");
         
   /**
    * Execute a query.
    */
   public static final LifeCycleEvent QUERY = 
         new LifeCycleEvent("QUERY");

   /**
    * Is the entity modified.
    */
   public static final LifeCycleEvent IS_MODIFIED = 
         new LifeCycleEvent("IS_MODIFIED");

   /**
    * Load an entity.
    */
   public static final LifeCycleEvent LOAD = 
         new LifeCycleEvent("LOAD");

   /**
    * Store an entity.
    */
   public static final LifeCycleEvent STORE = 
         new LifeCycleEvent("STORE");

   /**
    * Activate the entity.
    */
   public static final LifeCycleEvent ACTIVATE = 
         new LifeCycleEvent("ACTIVATE");

   /**
    * Passivate the entity.
    */
   public static final LifeCycleEvent PASSIVATE = 
         new LifeCycleEvent("PASSIVATE");
   
   /**
    * Name of this key. Used for toString.
    */
   private final String displayName;

   /**
    * Creates an entity life cycle key with the specified name.  Identity is 
    * based on the system object identity and the name is only used for 
    * printing.
    */
   private LifeCycleEvent(String displayName) {
      this.displayName = displayName;
   }

   public String toString() {
      return displayName;
   }

   private static final class LifeCycleEventKey implements Serializable
   {
      private LifeCycleEventKey()
      {
      }

      public String toString()
      {
         return "LIFE_CYCLE_EVENT_KEY";
      }

      Object readResolve()
      {
         return LifeCycleEvent.KEY;
      }
   }
}

