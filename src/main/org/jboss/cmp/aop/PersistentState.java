package org.jboss.cmp.aop;

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * Enumeration of persitent states.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class PersistentState
   implements Serializable, StateAware
{
   // Static -----------------------------------------
   private static int nextOrdinal = 0;

   // Constants --------------------------------------
   public static final PersistentState VALUES[] = new PersistentState[7];

   /**
    * Transient state.
    * Instances that do not represent the state in the physical store
    * are in the Transient state and are treated the same way as instances
    * that do not implement Persistable.
    * Transient instance is not associated with the identity.
    * The call to Persistable.cmpGetIdentity() returns null.
    * Transient instance transitions to Persistent-new after the call
    * to Persistable.cmpMakePersistent().
    */
   public static final PersistentState TRANSIENT = new Transient("Transient");

   /**
    * Persistent-new.
    * Transient instances that were makred as persistent with the call to
    * Persistable.cmpMakePersistent() are in the Persistent-new state.
    * For instances in Persistent-new state, method PersistentState.cmpGetIdentity()
    * returns non null identity.
    * FIXME: spec for physical store generated identity
    */
   public static final PersistentState PERSISTENT_NEW = new PersistentNew("Persistent-new");

   /**
    * Hollow.
    * Instances in the Hollow state represent persistent data in the physical store.
    * The method Persistable.cmpGetIdentity() returns non null identity.
    * If there are managed fields that represent instance’s identity, they contain
    * non null values. Other managed fields are in undefined state.
    */
   public static final PersistentState HOLLOW = new Hollow("Hollow");

   /**
    * Persistent-clean.
    * Hollow instances, managed fields of which were accessed but not modified in
    * the current transaction, are in the Persistent-clean state.
    */
   public static final PersistentState PERSISTENT_CLEAN = new PersistentClean("Persistent-clean");

   /**
    * Persistent-dirty.
    * Instance that represents persistent data that was changed in the current transaction
    * is in the Persistent-dirty state.
    */
   public static final PersistentState PERSISTENT_DIRTY = new PersistentDirty("Persistent-dirty");

   /**
    * Persistent-deleted.
    * Instances that represent persistent data in the physical store and were marked
    * as deleted in the current transaction with the call to Persistable.cmpDeletePersistent()
    * are in the Persistent-deleted state.
    */
   public static final PersistentState PERSISTENT_DELETED = new PersistentDeleted("Persistent-deleted");

   /**
    * Persistent-new-deleted.
    * Instances that were makred persistent in the current transaction with the call
    * to Persistable.cmpMakePersistent and later, in the same transaction, marked as deleted
    * with the call to Persistable.cmpDeletePersistent are in the Persistent-new-deleted state.
    * Persistent-new-deleted instances transition to Transient state at commit or rollback.
    * Instances in Persistent-new-deleted state retain their identity.
    */
   public static final PersistentState PERSISTENT_NEW_DELETED = new PersistentNewDeleted("Persistent-new-deleted");

   // Attributes -------------------------------------
   private final int ordinal;
   private final transient String name;

   // Constructor ------------------------------------
   private PersistentState(String name)
   {
      this.name = name;
      this.ordinal = nextOrdinal++;
      VALUES[ordinal] = this;
   }

   // Package ----------------------------------------
   Object readResolve()
      throws ObjectStreamException
   {
      return VALUES[ordinal];
   }

   // Public -----------------------------------------
   public String toString()
   {
      return name;
   }

   // Inner ------------------------------------------
   /**
    * Transient state implementation
    */
   private static final class Transient
      extends PersistentState
   {
      // Constructor ---------------------------------
      public Transient(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return false;
      }

      public boolean cmpIsPersistent()
      {
         return false;
      }

      public boolean cmpIsNew()
      {
         return false;
      }

      public boolean cmpIsDeleted()
      {
         return false;
      }
   }

   /**
    * Persistent-new state implementation
    */
   private static final class PersistentNew
      extends PersistentState
   {
      // Constructor ---------------------------------
      public PersistentNew(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return true;
      }

      public boolean cmpIsPersistent()
      {
         return true;
      }

      public boolean cmpIsNew()
      {
         return true;
      }

      public boolean cmpIsDeleted()
      {
         return false;
      }
   }

   /**
    * Hollow state implementation
    */
   private static final class Hollow
      extends PersistentState
   {
      // Constructor ---------------------------------
      public Hollow(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return false;
      }

      public boolean cmpIsPersistent()
      {
         return true;
      }

      public boolean cmpIsNew()
      {
         return false;
      }

      public boolean cmpIsDeleted()
      {
         return false;
      }
   }

   /**
    * Persistent-clean state implementation
    */
   private static final class PersistentClean
      extends PersistentState
   {
      // Constructor ---------------------------------
      public PersistentClean(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return false;
      }

      public boolean cmpIsPersistent()
      {
         return true;
      }

      public boolean cmpIsNew()
      {
         return false;
      }

      public boolean cmpIsDeleted()
      {
         return false;
      }
   }

   /**
    * Persistent-dirty state implementation
    */
   private static final class PersistentDirty
      extends PersistentState
   {
      // Constructor ---------------------------------
      public PersistentDirty(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return true;
      }

      public boolean cmpIsPersistent()
      {
         return true;
      }

      public boolean cmpIsNew()
      {
         return false;
      }

      public boolean cmpIsDeleted()
      {
         return false;
      }
   }

   /**
    * Persistent-deleted state implementation
    */
   private static final class PersistentDeleted
      extends PersistentState
   {
      // Constructor ---------------------------------
      public PersistentDeleted(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return true;
      }

      public boolean cmpIsPersistent()
      {
         return true;
      }

      public boolean cmpIsNew()
      {
         return false;
      }

      public boolean cmpIsDeleted()
      {
         return true;
      }
   }

   /**
    * Persistent-new-deleted state implementation
    */
   private static final class PersistentNewDeleted
      extends PersistentState
   {
      // Constructor ---------------------------------
      public PersistentNewDeleted(String name)
      {
         super(name);
      }

      // StateAware implementation -------------------
      public boolean cmpIsDirty()
      {
         return true;
      }

      public boolean cmpIsPersistent()
      {
         return true;
      }

      public boolean cmpIsNew()
      {
         return true;
      }

      public boolean cmpIsDeleted()
      {
         return true;
      }
   }
}
