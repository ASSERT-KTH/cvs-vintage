/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.lock;

import org.jboss.logging.Logger;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCOptimisticLockingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.TransactionLocal;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMPMessage;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.deployment.DeploymentException;
import org.jboss.invocation.Invocation;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;


/**
 * This class is an optmistic lock implementation.
 * It locks fields and their values during transaction.
 * Locked fields and their values are added to the WHERE clause of
 * UPDATE SQL statement when entity is stored.
 * The following strategies supported:
 * - fixed group of fields
 *   Fixed group of fields is used. The fields and their values are
 *   locked at the beginning of a transaction. The group name must match
 *   one of the entity's load-group-name.
 * - modified strategy
 *   The fields that were modified during transaction are used as lock.
 *   All entity's field values are locked at the beginning of the transaction.
 *   The fields are locked only after actual change.
 * - read strategy
 *   The fields that were read/modified during transaction.
 *   All entity's field values are locked at the beginning of the transaction.
 *   The fields are locked only after they were accessed.
 * - version-column strategy
 *   This adds additional version field of type java.lang.Long. Each update
 *   of the entity will increase the version by 1.
 * - timestamp-column strategy
 *   Adds additional timestamp column of type java.util.Date. Each update
 *   of the entity will set the field to the current time.
 * - key-generator column strategy
 *   Adds additional column. The type is defined by user. The key generator
 *   is used to set the next value.
 *
 * Note: all optimistic locking related code should be rewritten when get
 * new CMP design.
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 * @version $Revision: 1.4 $
 */
public class JDBCOptimisticLock
   extends BeanLockSupport
{
   // Attributes -------------------------------------
   private static Logger log = Logger.getLogger( JDBCOptimisticLock.class );

   /** locking metadata per container */
   private static final Map lockMetaDataByContainer = new HashMap();
   /** locking metadata per lock instance. it should not be accessed directly
    but through the corresponding getter method instead */
   private JDBCOptimisticLockingMetaData metadata;

   /** store manager per container */
   private static final Map managerByContainer = new HashMap();
   /** store manager per lock instance. it should not be accessed directly
    but through the corresponding getter method instead */
   private JDBCStoreManager manager;

   /** key generators per container */
   private static final Map keyGeneratorByContainer = new HashMap();
   /** key generator per lock instance. it should not be accessed directly
    but through the corresponding getter method instead */
   private KeyGenerator keyGenerator;

   /** locking strategy implementation per container */
   private static final Map lockingStrategyByContainer = new HashMap();
   /** locking strategy impl per lock instance. it should not be accessed directly
    but through the corresponding getter method instead */
   private LockingStrategy lockingStrategy;

   /** entity container */
   private EntityContainer container;

   // Constructor ------------------------------------
   public JDBCOptimisticLock() { }

   // Static -----------------------------------------
   /**
    * Returns locking strategy implementation depending on metadata.
    * This method is called one time per lock when it is initialzed.
    */
   public static LockingStrategy createLockingStrategy(JDBCStoreManager manager,
                                                       JDBCOptimisticLockingMetaData md)
   {
      if(md.FIELD_GROUP_STRATEGY == md.getLockingStrategy())
         return new FieldGroupLockingStrategy(
            manager.getEntityBridge().getLoadGroup(md.getGroupName())
         );
      else if(md.MODIFIED_STRATEGY == md.getLockingStrategy())
         return new ModifiedLockingStrategy(
            manager.getEntityBridge().getFields()
         );
      else if(md.READ_STRATEGY == md.getLockingStrategy())
         return new ReadLockingStrategy(
            manager.getEntityBridge().getFields()
         );
      else if(md.VERSION_COLUMN_STRATEGY == md.getLockingStrategy())
         return new VersionColumnLockingStrategy(
            manager.getEntityBridge().getVersionField()
         );
      else if(md.TIMESTAMP_COLUMN_STRATEGY == md.getLockingStrategy())
         return new TimestampColumnLockingStrategy(
            manager.getEntityBridge().getVersionField()
         );
      else if(md.KEYGENERATOR_COLUMN_STRATEGY == md.getLockingStrategy())
         return new KeyGeneratorColumnLockingStrategy(
            (KeyGenerator)keyGeneratorByContainer.get(manager.getContainer()),
            manager.getEntityBridge().getVersionField()
         );

      throw new IllegalStateException("Unknown optimistic locking strategy.");
   }

   /**
    * Registers store manager, locking metadata and newly
    * created locking strategy implementation.
    */
   public static void register(JDBCStoreManager manager,
                               JDBCOptimisticLockingMetaData metadata)
      throws DeploymentException
   {
      EntityContainer container = manager.getContainer();
      // register metadata
      lockMetaDataByContainer.put( container, metadata );
      // register store manager
      managerByContainer.put( manager.getContainer(), manager );

      // look up key generator factory
      if(metadata.getKeyGeneratorFactory() != null)
      {
         try
         {
            InitialContext ic = new InitialContext();
            KeyGeneratorFactory factory = (KeyGeneratorFactory)ic.lookup(
               metadata.getKeyGeneratorFactory()
            );
            // register key generator
            keyGeneratorByContainer.put(container, factory.getKeyGenerator());
         }
         catch(NamingException ne)
         {
            throw new DeploymentException (
               "Error: failed to look up key generator factory: "
                  + metadata.getKeyGeneratorFactory(), ne
            );
         }
         catch(Exception e)
         {
            throw new DeploymentException (
               "Error: fialed to create key generator from factory: "
                  + metadata.getKeyGeneratorFactory(), e
            );
         }
      }

      // create and register strategy implementation
      lockingStrategyByContainer.put(container, createLockingStrategy(manager, metadata));
   }

   /**
    * Returns initial value for the version column.
    * It must be static as it's called before the context is
    * associated with id.
    */
   public static Object getInitialValue(JDBCCMPFieldBridge field)
   {
      LockingStrategy lockingStrategy =
         (LockingStrategy)lockingStrategyByContainer.get(field.getManager().getContainer());
      return lockingStrategy.getInitialValue(field);
   }

   // Public -----------------------------------------
   /**
    * Returns locking metadata per lock instance.
    */
   public JDBCOptimisticLockingMetaData getLockMetaData()
   {
      if(metadata == null)
         metadata = (JDBCOptimisticLockingMetaData)lockMetaDataByContainer.get(container);
      return metadata;
   }

   /**
    * Returns locking strategy implementation per lock instance
    */
   public LockingStrategy getLockingStrategy()
   {
      if(lockingStrategy == null)
         lockingStrategy = (LockingStrategy)lockingStrategyByContainer.get(container);
      return lockingStrategy;
   }

   /**
    * Returns store manager per lock instance.
    */
   public JDBCStoreManager getJDBCStoreManager()
   {
      if(manager == null)
         manager = (JDBCStoreManager)managerByContainer.get(container);
      return manager;
   }

   /**
    * Returns key generator per lock instance.
    */
   public KeyGenerator getKeyGenerator()
   {
      if(keyGenerator == null)
         keyGenerator = (KeyGenerator)keyGeneratorByContainer.get(container);
      return keyGenerator;
   }

   /**
    * Returns the next value for a locking field
    * by delegating the call to locking strategy implementation.
    */
   public Object getNextLockingValue(JDBCCMPFieldBridge field, Object value)
   {
      return getLockingStrategy().getNextLockingValue(field, value);
   }

   /**
    * This method is called by the store manager when some
    * event occured on a field state in the entity this lock
    * is associated with.
    * Delegates the call to locking strategy implementation.
    */
   public void fieldStateEventCallback(CMPMessage msg,
                                       JDBCFieldBridge field,
                                       Object value)
   {
      getLockingStrategy().fieldStateEventCallback(msg, field, value);
   }

   /**
    * This method actually locks the field
    * by delegating the call to locking strategy implementation.
    */
   public void lockField(JDBCFieldBridge field)
   {
      getLockingStrategy().lockField(field);
   }

   /**
    * Locks field's value (but not the field)
    * by delegating the call to locking strategy implementation.
    */
   public void lockFieldValue(JDBCFieldBridge field, EntityEnterpriseContext ctx)
   {
      getLockingStrategy().lockFieldValue(field, ctx);
   }

   /**
    * Returns locked field's value
    * by delegating the call to locking strategy implementation.
    */
   public Object getLockedFieldValue( JDBCCMPFieldBridge field )
   {
      return getLockingStrategy().getLockedFieldValue(field);
   }

   /**
    * Returns all the locked fields
    * by delegating the call to locking strategy implementation.
    */
   public Collection getLockedFields()
   {
      return getLockingStrategy().getLockedFields();
   }

   // BeanLockSupport overrides ----------------------
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
   }

   public void schedule(Invocation mi)
      throws Exception
   {
      Object id = mi.getId();
      if(id == null)
         return;

      EntityEnterpriseContext ctx = EntityContainer.getEntityInvocationRegistry().getContext(
                  container,
                  id,
                  mi.getTransaction());
      if(ctx == null)
      {
         ctx = (EntityEnterpriseContext)container.getInstancePool().get();
         ctx.setCacheKey(id);
         ctx.setId(id);
         container.activateEntity(ctx);
      }

      if(log.isTraceEnabled())
         log.trace("schedule> method=" + mi.getMethod().getName() + "; tx=" + mi.getTransaction());

      // lock fields and values new transaction came in
      if(getTransaction() != mi.getTransaction())
      {
         if(log.isTraceEnabled())
            log.trace("schedule> other tx came in: tx="
               + (mi.getTransaction() == null ? "null" : "" + mi.getTransaction().getStatus())
               + "; " + (ctx == null ? "ctx=null" : "ctx.id=" + ctx.getId()));

         setTransaction(mi.getTransaction());

         // lock fields/values
         getLockingStrategy().schedule(ctx);
      }

      if(log.isTraceEnabled())
         log.trace("schedule> "
            + (ctx == null ? "ctx=null" : "ctx.id=" + ctx.getId())
            + "; id=" + getId()
            + "; method=" + (mi.getMethod() == null ? "null" : mi.getMethod().getName())
            + "; tx=" + (getTransaction() == null ? "null" : "" + tx.getStatus())
         );

      return;
   }

   public void setTransaction(Transaction transaction)
   {
      super.setTransaction( transaction );
   }

   public void endTransaction( Transaction transaction )
   {
      // complete
   }

   public void wontSynchronize( Transaction trasaction )
   {
      // complete
   }

   public void endInvocation( Invocation mi )
   {
      // complete
   }

   // Inner ------------------------------------------
   /**
    * This class defines and implements methods common to all
    * locking strategies.
    */
   private static abstract class LockingStrategy
   {
      // Attributes ----------------------------------
      /** this is where field values are locked */
      private TransactionLocal lockedFieldValues = new TransactionLocal()
      {
         public Object initialValue()
         {
            return new HashMap();
         }
      };

      /** this is where fields are locked */
      private TransactionLocal lockedFields = new TransactionLocal()
      {
         public Object initialValue()
         {
            return new HashMap();
         }
      };

      // Public --------------------------------------
      /**
       * Returns initial value for a locking field
       */
      public Object getInitialValue(JDBCCMPFieldBridge field)
      {
         throw new IllegalStateException(
            "method getInitialValue() isn't supported by the chosen locking strategy."
         );
      }

      /**
       * Returns the next value for a locking field
       */
      public Object getNextLockingValue(JDBCCMPFieldBridge field, Object value)
      {
         throw new IllegalStateException(
            "method getNextLockingValue() isn't supported by the chosen locking strategy."
         );
      }

      /**
       * This method is called by store manager when some
       * event occured on a field state in the entity this lock
       * is associated with.
       */
      void fieldStateEventCallback(CMPMessage msg,
                                   JDBCFieldBridge field,
                                   Object value)
      {
         // ignore by default
      }

      /**
       * This method is called from BeanLockSupport implementation of
       * schedule(Invocation) to lock fields and their values
       */
      public abstract void schedule(EntityEnterpriseContext ctx);

      /**
       * This method actually locks the field
       */
      public void lockField(JDBCFieldBridge field)
      {
         Map fields = (Map) lockedFields.get();
         if(!fields.containsKey(field) && !field.isPrimaryKeyMember())
         {
            if(log.isTraceEnabled())
               log.trace("lockField> field=" + field.getFieldName());
            fields.put( field, field );
         }
         else
         {
            if(log.isTraceEnabled())
               log.trace("lockField> field " + field.getFieldName()
                  + " is already locked or is a primary key");
         }
      }

      /**
       * Locks field's value, but not the field
       */
      public void lockFieldValue(JDBCFieldBridge field, EntityEnterpriseContext ctx)
      {
         Map fieldValues = (Map)lockedFieldValues.get();
         if(!fieldValues.containsKey(field) && !field.isPrimaryKeyMember())
         {
            //field.resetPersistenceContext(ctx);
            Object value = field.getInstanceValue(ctx);
            if(log.isTraceEnabled())
               log.trace("lockFieldValue> field=" + field.getFieldName()
                  + "; value " + value);
            fieldValues.put(field, value);
         }
      }

      /**
       * Returns locked field's value
       */
      public Object getLockedFieldValue(JDBCCMPFieldBridge field)
      {
         return ((Map)lockedFieldValues.get()).get(field);
      }

      /**
       * Returns all the locked fields
       */
      public Collection getLockedFields()
      {
         return ((Map)lockedFields.get()).keySet();
      }
   }

   /**
    * Field group optimistic locking strategy implementation
    */
   private static class FieldGroupLockingStrategy
      extends LockingStrategy
   {
      // Attributes ----------------------------------------
      private List fields;

      // Constructor ---------------------------------------
      public FieldGroupLockingStrategy(List fields)
      {
         this.fields = fields;
      }

      // LockingStrategy implementation --------------------
      public void schedule( EntityEnterpriseContext ctx )
      {
         if(log.isTraceEnabled())
            log.trace("schedule> locking group");

         for( Iterator iter = fields.iterator(); iter.hasNext(); )
         {
            JDBCFieldBridge field = ( JDBCCMPFieldBridge ) iter.next();
            this.lockFieldValue(field, ctx);
            this.lockField(field);
         }
      }
   }

   /**
    * Modified optimistic locking strategy implementation
    */
   public static class ModifiedLockingStrategy
      extends LockingStrategy
   {
      // Attributes ----------------------------------------
      private List fields;

      // Constructor ---------------------------------------
      public ModifiedLockingStrategy(List fields)
      {
         this.fields = fields;
      }

      // LockingStrategy implementation --------------------
      void fieldStateEventCallback(CMPMessage msg,
                                   JDBCFieldBridge field,
                                   Object value)
      {
         if(msg == CMPMessage.CHANGED)
            this.lockField(field);
      }

      public void schedule(EntityEnterpriseContext ctx)
      {
         if(log.isTraceEnabled())
            log.trace("schedule> modified strategy: locking all field values");
         for(Iterator iter = fields.iterator(); iter.hasNext();)
         {
            JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
            this.lockFieldValue(field, ctx);
         }
      }
   }

   /**
    * Read optimistic locking strategy implementation
    */
   public static class ReadLockingStrategy
      extends LockingStrategy
   {
      // Attributes ----------------------------------------
      private List fields;

      // Constructor ---------------------------------------
      public ReadLockingStrategy(List fields)
      {
         this.fields = fields;
      }

      // LockingStrategy implementation --------------------
      void fieldStateEventCallback(CMPMessage msg,
                                   JDBCFieldBridge field,
                                   Object value)
      {
         if(msg == CMPMessage.ACCESSED || msg == CMPMessage.CHANGED)
            this.lockField( field );
      }

      public void schedule(EntityEnterpriseContext ctx)
      {
         if(log.isTraceEnabled())
            log.trace("schedule> read strategy: locking all field values");
         for(Iterator iter = fields.iterator(); iter.hasNext();)
         {
            JDBCFieldBridge field = ( JDBCFieldBridge ) iter.next();
            this.lockFieldValue(field, ctx);
         }
      }
   }

   /**
    * Version (counter) column optimistic locking strategy implementation
    */
   public static class VersionColumnLockingStrategy
      extends LockingStrategy
   {
      private JDBCCMPFieldBridge versionField;

      // Constructor ---------------------------------------
      public VersionColumnLockingStrategy(JDBCCMPFieldBridge versionField)
      {
         this.versionField = versionField;
      }

      // LockingStrategy implementation --------------------
      public Object getInitialValue(JDBCCMPFieldBridge field)
      {
         if(field.getFieldType() != java.lang.Long.class)
            throw new IllegalStateException(
               "Incorrect type of version column: " + field.getFieldType()
               + ". Version coulmn must be of type java.lang.Long"
            );

         return new Long(1);
      }

      public Object getNextLockingValue(JDBCCMPFieldBridge field, Object value)
      {
         try
         {
            return new Long(((Long)value).longValue() + 1);
         }
         catch(ClassCastException cce)
         {
            throw new IllegalStateException(
               "Incorrect type of version column: " + field.getFieldType()
               + ". Version coulmn must be of type java.lang.Long"
            );
         }
      }

      public void schedule(EntityEnterpriseContext ctx)
      {
         if(log.isTraceEnabled())
            log.trace("schedule> locking version field: " + versionField.getFieldName());

         this.lockFieldValue(versionField, ctx);
         this.lockField(versionField);
      }
   }

   /**
    * Timestamp column optimistic locking strategy implementation
    */
   public static class TimestampColumnLockingStrategy
      extends LockingStrategy
   {
      private JDBCCMPFieldBridge timestampField;

      // Constructor ---------------------------------------
      public TimestampColumnLockingStrategy(JDBCCMPFieldBridge timestampField)
      {
         this.timestampField = timestampField;
      }

      // LockingStrategy implementation --------------------
      public Object getInitialValue(JDBCCMPFieldBridge field)
      {
         if(field.getFieldType() != java.util.Date.class)
            throw new IllegalStateException(
               "Incorrect type of timestamp column: " + field.getFieldType()
               + ". Timestamp coulmn must be of type java.util.Date."
            );
         return new java.util.Date();
      }

      public Object getNextLockingValue(JDBCCMPFieldBridge field, Object value)
      {
         return new java.util.Date();
      }

      public void schedule(EntityEnterpriseContext ctx)
      {
         if(log.isTraceEnabled())
            log.trace("schedule> locking timestamp field: " + timestampField.getFieldName());

         this.lockFieldValue(timestampField, ctx);
         this.lockField(timestampField);
      }
   }

   /**
    * Key generator column optimistic locking strategy implementation
    */
   public static class KeyGeneratorColumnLockingStrategy
      extends LockingStrategy
   {
      private KeyGenerator keyGenerator;
      private JDBCCMPFieldBridge keyGenField;

      // Constructor ---------------------------------------
      public KeyGeneratorColumnLockingStrategy(KeyGenerator keyGenerator, JDBCCMPFieldBridge keyGenField)
      {
         this.keyGenField = keyGenField;
         this.keyGenerator = keyGenerator;
      }

      // LockingStrategy implementation --------------------
      public Object getInitialValue(JDBCCMPFieldBridge field)
      {
         return keyGenerator.generateKey();
      }

      public Object getNextLockingValue(JDBCCMPFieldBridge field, Object value)
      {
         return keyGenerator.generateKey();
      }

      public void schedule(EntityEnterpriseContext ctx)
      {
         if(log.isTraceEnabled())
            log.trace("schedule> locking generated field: " + keyGenField.getFieldName());
         this.lockFieldValue(keyGenField, ctx);
         this.lockField(keyGenField);
      }
   }
}
