/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cmp.aop;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * The enum of state transition events.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public abstract class PersistentStateTransitionEvent
   implements Serializable
{
   // Static ---------------------------------------------------
   private static int nextOrdinal = 0;

   // Constants ------------------------------------------------
   private static final PersistentStateTransitionEvent[] VALUES = new PersistentStateTransitionEvent[8];
   public static final PersistentStateTransitionEvent READ = new ReadStateEvent("Read");
   public static final PersistentStateTransitionEvent WRITE = new WriteStateEvent("Write");
   public static final PersistentStateTransitionEvent READ_IDENTITY = new ReadIdentityEvent("ReadIdentity");
   public static final PersistentStateTransitionEvent WRITE_IDENTITY = new WriteIdentityEvent("WriteIdentity");
   public static final PersistentStateTransitionEvent MAKE_PERSISTENT = new StoreStateEvent("MakePersistent");
   public static final PersistentStateTransitionEvent DELETE_PERSISTENT = new RemoveStateEvent("DeletePersistent");
   public static final PersistentStateTransitionEvent COMMIT = new CommitStateEvent("Commit");
   public static final PersistentStateTransitionEvent ROLLBACK = new RollbackStateEvent("Rollback");

   // Attributes -----------------------------------------------
   private final int ordinal;
   private final transient String name;

   // Constructor ----------------------------------------------
   private PersistentStateTransitionEvent(String name)
   {
      this.name = name;
      this.ordinal = nextOrdinal++;
      VALUES[ordinal] = this;
   }

   // Package --------------------------------------------------
   Object readResolve()
      throws ObjectStreamException
   {
      return VALUES[ordinal];
   }

   // Public ---------------------------------------------------
   public String toString()
   {
      return name;
   }

   // Abstract -------------------------------------------------
   /**
    * The implementations of this method return the state an instance
    * should transition to.
    *
    * @param currentState The current state.
    * @return the state an instance transitions to.
    */
   public abstract PersistentState getNextPersistentState(PersistentState currentState)
      throws PersistentStateTransitionException;

   // Inner ----------------------------------------------------
   /**
    * The class represents read state event.
    */
   private static final class ReadStateEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public ReadStateEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.PERSISTENT_DELETED
            || currentState == PersistentState.PERSISTENT_NEW_DELETED)
            throw new PersistentStateTransitionException("Can't read instance in " + currentState + " state.");

         if(currentState == PersistentState.HOLLOW)
            return PersistentState.PERSISTENT_CLEAN;
         return currentState;
      }
   }

   /**
    * The class represents read identity field event.
    */
   private static final class ReadIdentityEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public ReadIdentityEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.HOLLOW)
            return PersistentState.PERSISTENT_CLEAN;
         return currentState;
      }
   }

   /**
    * The class represents write state event.
    */
   private static final class WriteStateEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public WriteStateEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.PERSISTENT_DELETED
            || currentState == PersistentState.PERSISTENT_NEW_DELETED)
            throw new PersistentStateTransitionException("Can't write instance in " + currentState + " state.");

         if(currentState == PersistentState.HOLLOW
            || currentState == PersistentState.PERSISTENT_CLEAN)
            return PersistentState.PERSISTENT_DIRTY;
         return currentState;
      }
   }

   /**
    * The class represents write identity field event.
    */
   private static final class WriteIdentityEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public WriteIdentityEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState != PersistentState.TRANSIENT)
            throw new PersistentStateTransitionException(
               "Can't change identity for Persistable instance in a persistent state.");
         return currentState;
      }
   }

   /**
    * The class represents store state event.
    */
   private static final class StoreStateEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public StoreStateEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.PERSISTENT_DELETED
            || currentState == PersistentState.PERSISTENT_NEW_DELETED)
            throw new PersistentStateTransitionException("Can't store instance in " + currentState + " state.");

         if(currentState == PersistentState.TRANSIENT)
            return PersistentState.PERSISTENT_NEW;
         return currentState;
      }
   }

   /**
    * The class represents remove state event.
    */
   private static final class RemoveStateEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public RemoveStateEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.PERSISTENT_NEW)
            return PersistentState.PERSISTENT_NEW_DELETED;
         else if(currentState == PersistentState.HOLLOW
            || currentState == PersistentState.PERSISTENT_CLEAN
            || currentState == PersistentState.PERSISTENT_DIRTY)
            return PersistentState.PERSISTENT_DELETED;
         return currentState;
      }
   }

   /**
    * The class represents commit state event.
    */
   private static final class CommitStateEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public CommitStateEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.PERSISTENT_NEW
            || currentState == PersistentState.PERSISTENT_CLEAN
            || currentState == PersistentState.PERSISTENT_DIRTY)
            return PersistentState.HOLLOW;
         else if(currentState == PersistentState.PERSISTENT_DELETED
            || currentState == PersistentState.PERSISTENT_NEW_DELETED)
            return PersistentState.TRANSIENT;
         return currentState;
      }
   }

   /**
    * The class represents rollback state event.
    */
   private static final class RollbackStateEvent
      extends PersistentStateTransitionEvent
   {
      // Constructor -------------------------------------------
      public RollbackStateEvent(String name)
      {
         super(name);
      }

      // PersistentStateTransformer implementation -----------------------
      public PersistentState getNextPersistentState(PersistentState currentState)
         throws PersistentStateTransitionException
      {
         if(currentState == PersistentState.PERSISTENT_DELETED
            || currentState == PersistentState.PERSISTENT_CLEAN
            || currentState == PersistentState.PERSISTENT_DIRTY)
            return PersistentState.HOLLOW;
         else if(currentState == PersistentState.PERSISTENT_NEW
            || currentState == PersistentState.PERSISTENT_NEW_DELETED)
            return PersistentState.TRANSIENT;
         return currentState;
      }
   }
}
