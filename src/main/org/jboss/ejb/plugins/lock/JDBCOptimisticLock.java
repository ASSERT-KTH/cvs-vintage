/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.lock;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMPMessage;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.Container;
import org.jboss.ejb.entity.EntityInvocationType;
import org.jboss.ejb.entity.EntityInvocationKey;

import java.util.List;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCOptimisticLockingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.JDBCStoreManager;
import org.jboss.ejb.plugins.cmp.jdbc.TransactionLocal;
import org.jboss.ejb.plugins.lock.BeanLockSupport;
import org.jboss.ejb.plugins.lock.Entrancy;
import org.jboss.ejb.plugins.keygenerator.KeyGenerator;
import org.jboss.deployment.DeploymentException;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.security.SecurityAssociation;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

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
 * @version $Revision: 1.2 $
 */
public class JDBCOptimisticLock
   extends BeanLockSupport
{
   // Attributes -------------------------------------
   private static Logger log = Logger.getLogger( JDBCOptimisticLock.class );

   /** locking metadata */
   private static final Map lockMetaDataByContainer = new HashMap();
   private JDBCOptimisticLockingMetaData metadata;

   /** store manager */
   private static final Map managerByContainer = new HashMap();
   private JDBCStoreManager manager;

   /** key generator factories */
   private static final Map keyGeneratorByContainer = new HashMap();
   private KeyGenerator keyGenerator;

   /** this is where the field values are locked */
   private TransactionLocal lockedFieldValues = new TransactionLocal() {
      public Object initialValue()
      {
         return new HashMap();
      }
   };

   /** this is where the fields are locked */
   private TransactionLocal lockedFields = new TransactionLocal() {
      public Object initialValue()
      {
         return new HashMap();
      }
   };

   // Constructor ------------------------------------
   public JDBCOptimisticLock() {}

   // Static -----------------------------------------
   public static void setLockMetaData( Container container,
                                       JDBCOptimisticLockingMetaData lockMetaData )
   {
      lockMetaDataByContainer.put( container, lockMetaData );
   }

   public static void setJDBCStoreManager( JDBCStoreManager manager )
   {
      managerByContainer.put( manager.getContainer(), manager );
   }

   public static void setKeyGenerator( Container container,
                                       KeyGenerator keyGenerator )
   {
      keyGeneratorByContainer.put( container, keyGenerator );
   }

   public static Object getInitialValue( JDBCCMPFieldBridge field )
   {
      KeyGenerator keyGenerator = (KeyGenerator)
         keyGeneratorByContainer.get(field.getManager().getContainer());
      if(keyGenerator != null)
         return keyGenerator.generateKey();

      Class fieldType = field.getFieldType();
      if( fieldType == Long.class )
         return new Long( 1 );
      else if( fieldType == java.util.Date.class )
         return new java.util.Date();

      throw new IllegalArgumentException( "Type " + fieldType
         + " is not supported for optimistic locking version column." );
   }

   public Object getNextLockingValue( JDBCCMPFieldBridge field, Object value )
   {
      KeyGenerator keyGenerator = (KeyGenerator)
         keyGeneratorByContainer.get(field.getManager().getContainer());
      if(keyGenerator != null)
         return keyGenerator.generateKey();

      Class fieldType = value.getClass();
      if( fieldType == Long.class )
         return new Long( ((Long)value).longValue() + 1 );
      else if( fieldType == java.util.Date.class )
         return new java.util.Date();

      throw new IllegalArgumentException( "Type " + fieldType
         + " is not supported for optimistic locking version column." );
   }

   // Public -----------------------------------------
   public JDBCOptimisticLockingMetaData getLockMetaData()
   {
      if(metadata == null)
         metadata = ( JDBCOptimisticLockingMetaData ) lockMetaDataByContainer.get( container );
      return metadata;
   }

   public JDBCStoreManager getJDBCStoreManager()
   {
      if( manager == null )
         manager = ( JDBCStoreManager ) managerByContainer.get( container );
      return manager;
   }

   public KeyGenerator getKeyGenerator()
   {
      if(keyGenerator == null)
         keyGenerator = (KeyGenerator) keyGeneratorByContainer.get(container);
      return keyGenerator;
   }

   public void fieldStateEventCallback( CMPMessage msg,
                                        JDBCFieldBridge field,
                                        Object value )
   {
      if( msg == CMPMessage.ACCESSED )
      {
         if( getLockMetaData().getLockingStrategy() ==
            JDBCOptimisticLockingMetaData.READ_STRATEGY )
            lockField( field );
      }
      else if( msg == CMPMessage.CHANGED )
      {
         if( getLockMetaData().getLockingStrategy() ==
            JDBCOptimisticLockingMetaData.MODIFIED_STRATEGY
            || getLockMetaData().getLockingStrategy() ==
            JDBCOptimisticLockingMetaData.READ_STRATEGY )
            lockField( field );
      }
      else if( msg == CMPMessage.CREATED )
      {
         // not emitted, not used
      }
      else if( msg == CMPMessage.LOADED )
      {
         // not emitted, not used
      }
      else if( msg == CMPMessage.RESETTED )
      {
         // not emitted, not used
      }
   }

   /**
    * These fields are actual locks
    */
   public void lockField(JDBCFieldBridge field)
   {
      Map fields = (Map)lockedFields.get();
      if(!fields.containsKey(field)
         && !field.isPrimaryKeyMember())
      {
         if(log.isDebugEnabled())
            log.debug( "lockField> locking field=" + field.getFieldName());
         fields.put(field, field);
      }
      else
      {
         if(log.isDebugEnabled())
            log.debug( "lockField> field " + field.getFieldName() + " is already locked" );
      }
   }

   /**
    * Locks field value
    */
   public void lockFieldValue( JDBCFieldBridge field,
                               Object value )
   {
      Map fieldValues = (Map)lockedFieldValues.get();
      if( !fieldValues.containsKey( field )
         && !field.isPrimaryKeyMember() )
      {
         if(log.isDebugEnabled())
         {
            log.debug( "lockFieldValue> locking field=" + field.getFieldName()
               + "; value " + value );
         }
         fieldValues.put( field, value );
      }
   }

   /**
    * Returns locked field value
    */
   public Object getLockedFieldValue( JDBCCMPFieldBridge field )
   {
      return ((Map)lockedFieldValues.get()).get(field);
   }

   /**
    * Returns all locked fields
    */
   public Collection getLockedFields()
   {
      return ((Map)lockedFields.get()).keySet();
   }

   // BeanLockSupport overrides ----------------------
   public void schedule( Invocation mi )
      throws Exception
   {
      EntityEnterpriseContext ctx = ( EntityEnterpriseContext ) mi.getEnterpriseContext();
      if( ctx == null )
      {
         ctx = ( EntityEnterpriseContext ) container.getInstanceCache().get( getId() );
      }

      if(log.isTraceEnabled())
         log.trace("schedule> method=" + mi.getMethod().getName()
            + "; tx=" + mi.getTransaction());

      if( getTransaction() != mi.getTransaction() )
      {
         if(log.isTraceEnabled()) {
            log.trace( "schedule> other tx came in: tx="
               + ( mi.getTransaction() == null ? "null" : "" + mi.getTransaction().getStatus() )
               + "; " + ( ctx == null ? "ctx=null" : "ctx.id=" + ctx.getId() ) );
         }
         setTransaction( mi.getTransaction() );

         // lock the fields
         if( !ctx.isValid() )
         {
            // ctx is not valid -> load
            Invocation invocation = new Invocation();
            invocation.setValue(
               EntityInvocationKey.TYPE,
               EntityInvocationType.LOAD,
               PayloadKey.TRANSIENT
            );
            invocation.setValue(
               Entrancy.ENTRANCY_KEY,
               Entrancy.NON_ENTRANT,
               PayloadKey.AS_IS
            );
            invocation.setEnterpriseContext( ctx );
            invocation.setId( mi.getId() );
            invocation.setTransaction( getTransaction() );
            invocation.setPrincipal( SecurityAssociation.getPrincipal() );
            invocation.setCredential( SecurityAssociation.getCredential() );
            container.invoke( invocation );

            // mark the ctx as valid
            ctx.setValid( true );
         }

         // lock fields/values
         JDBCOptimisticLockingMetaData md = getLockMetaData();
         if( md.getGroupName() != null )
         {
            if(log.isTraceEnabled())
               log.trace("schedule> locking group: " + md.getGroupName());

            JDBCStoreManager mngr = getJDBCStoreManager();
            for( Iterator iter = mngr.getEntityBridge().getLoadGroup( md.getGroupName() ).iterator();
                 iter.hasNext(); )
            {
               JDBCFieldBridge field = ( JDBCCMPFieldBridge ) iter.next();
               lockFieldValue(field, field.getInstanceValue( ctx ));
               lockField(field);
            }
         }
         else if( md.getLockingField() != null )
         {
            if(log.isTraceEnabled())
               log.trace( "schedule> locking version field: "
                  + md.getLockingField().getFieldName() );

            JDBCStoreManager mngr = getJDBCStoreManager();
            JDBCFieldBridge versionField =
               getJDBCStoreManager().getEntityBridge().getVersionField();
            lockFieldValue( versionField, versionField.getInstanceValue( ctx ) );
            lockField(versionField);
         }
         // It should be read or modified strategy.
         // Lock all the field values.
         // Fields will be locked when they are read or modified
         else
         {
            if(log.isTraceEnabled())
               log.trace("schedule> locking all field values");
            for(Iterator iter=getJDBCStoreManager().getEntityBridge().getFields().iterator();
               iter.hasNext();)
            {
               JDBCFieldBridge field = (JDBCFieldBridge)iter.next();
               lockFieldValue(field, field.getInstanceValue(ctx));
            }
         }
      }

      if(log.isTraceEnabled())
         log.trace( "schedule> "
            + ( ctx == null ? "ctx=null" : "ctx.id=" + ctx.getId() )
            + "; id=" + getId()
            + "; method=" + ( mi.getMethod() == null ? "null" : mi.getMethod().getName() )
            + "; type=" + mi.getValue( EntityInvocationKey.TYPE )
            + "; tx=" + ( getTransaction() == null ? "null" : "" + tx.getStatus() )
         );

      return;
   }

   public void setTransaction( Transaction transaction )
   {
      //log.debug( "setTransaction> tx=" + transaction );
      super.setTransaction( transaction );
   }

   public void endTransaction( Transaction transaction )
   {
      //log.debug( "endTransaction> tx=" + transaction );
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
}
