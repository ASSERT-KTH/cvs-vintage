/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.EJBException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.MethodInvocation;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.ejb.plugins.CMPPersistenceManager;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.CMRMessage;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.logging.Logger;

/**
 *
 * The role of this interceptor relationship messages from a related CMR field
 * and invoke the specified message on this container's cmr field of the
 * relationship.  This interceptor also manages the relation table data.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.8 $
 */
public class JDBCRelationInterceptor extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
   protected static final Method GET_RELATED_ID;
   protected static final Method ADD_RELATION;
   protected static final Method REMOVE_RELATION;
   
   static
   {
      try
      {
         final Class empty[] = {};
         final Class type = CMRMessage.class;
         
         GET_RELATED_ID = type.getMethod("getRelatedId", new Class[]
         {
            EntityEnterpriseContext.class,
            JDBCCMRFieldBridge.class
         });
         
         ADD_RELATION = type.getMethod("addRelation", new Class[]
         {
            EntityEnterpriseContext.class,
            Object.class
         });
         
         REMOVE_RELATION = type.getMethod("removeRelation", new Class[]
         {
            EntityEnterpriseContext.class,
            Object.class
         });
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   // Attributes ----------------------------------------------------
   
   /**
    *  The container of this interceptor.
    */
   protected EntityContainer container;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;
   }
   
   public Container getContainer()
   {
      return container;
   }
   
   // Interceptor implementation --------------------------------------
   
   public Object invoke(MethodInvocation mi) throws Exception
   {
      // We are going to work with the context a lot
      EntityEnterpriseContext ctx =
            (EntityEnterpriseContext)mi.getEnterpriseContext();
      
      // The Tx coming as part of the Method Invocation
      Transaction tx = mi.getTransaction();
      
      try
      {
         if(GET_RELATED_ID.equals(mi.getMethod()))
         {
            
            // call getRelateId
            JDBCCMRFieldBridge cmrField =
                  (JDBCCMRFieldBridge)mi.getArguments()[0];
            return cmrField.getRelatedId(ctx);
            
         } else if(ADD_RELATION.equals(mi.getMethod()))
         {
            
            // call addRelation
            JDBCCMRFieldBridge cmrField =
                  (JDBCCMRFieldBridge)mi.getArguments()[0];
            
            Object relatedId = mi.getArguments()[1];
            cmrField.addRelation(ctx, relatedId);
            
            RelationData relationData = getRelationData(cmrField, tx);
            relationData.addRelation(
                  cmrField,
                  ctx.getId(),
                  cmrField.getRelatedCMRField(),
                  relatedId);

            return null;
            
         } else if(REMOVE_RELATION.equals(mi.getMethod()))
         {
            
            // call removeRelation
            JDBCCMRFieldBridge cmrField = 
                  (JDBCCMRFieldBridge)mi.getArguments()[0];
            
            Object relatedId = mi.getArguments()[1];
            cmrField.removeRelation(ctx, relatedId);
            
            RelationData relationData = getRelationData(cmrField, tx);
            relationData.removeRelation(
                  cmrField,
                  ctx.getId(),
                  cmrField.getRelatedCMRField(),
                  relatedId);

            return null;
            
         } else
         {
            // No a message. Invoke down the chain
            return getNext().invoke(mi);
         }
         
      } finally
      {
         register(tx);
      }
   }
   
   protected RelationData getRelationData(
         JDBCCMRFieldBridge cmrField, Transaction tx)
   {
      Map txDataMap = cmrField.getJDBCStoreManager().getTxDataMap();
      Map txData = (Map)txDataMap.get(tx);
      if(txData == null)
      {
         txData = new HashMap();
         txDataMap.put(tx, txData);
      }
      
      JDBCRelationMetaData relationMetaData = 
            cmrField.getMetaData().getRelationMetaData();
      RelationData relationData = (RelationData)txData.get(relationMetaData);
      if(relationData == null)
      {
         relationData = new RelationData(
               cmrField, cmrField.getRelatedCMRField());
         txData.put(relationMetaData, relationData);
      }
      return relationData;
   }
   
   // Private  ----------------------------------------------------
   
   /**
    *  Register a transaction synchronization callback with a context.
    */
   private void register(Transaction tx)
   {
      // Create a new synchronization
      RelationSynchronization synch = new RelationSynchronization(tx);
      
      try
      {
         // We want to be notified when the transaction commits
         tx.registerSynchronization(synch);
      } catch(EJBException e)
      {
         throw e;
      } catch(Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   // Inner classes -------------------------------------------------
   
   private class RelationSynchronization implements Synchronization
   {
      /**
       *  The transaction we follow.
       */
      private Transaction tx;
      
      /**
       *  Create a new instance synchronization instance.
       */
      private RelationSynchronization(Transaction tx)
      {
         this.tx = tx;
      }
      
      // Synchronization implementation -----------------------------
      
      /**
       * Saves all of the relations changed in this transaction.
       */
      public void beforeCompletion()
      {
         // This is an independent point of entry. We need to make sure the
         // thread is associated with the right context class loader
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         Thread.currentThread().setContextClassLoader(
               container.getClassLoader());
         
         try
         {
            // Get the manager
            CMPPersistenceManager cmpPM = 
                  (CMPPersistenceManager)container.getPersistenceManager();
            JDBCStoreManager manager = 
                  (JDBCStoreManager)cmpPM.getPersistenceStore();
            
            manager.synchronizeRelationData(tx);
         } catch (Exception e)
         {
            log.error("ex", e);
            
            // Store failed -> rollback!
            try
            {
               tx.setRollbackOnly();
            } catch (SystemException ex)
            {
               // DEBUG ex.printStackTrace();
            } catch (IllegalStateException ex)
            {
               // DEBUG ex.printStackTrace();
            }
         } finally
         {
            Thread.currentThread().setContextClassLoader(oldCl);
         }
      }
      
      /**
       * Unused
       */
      public void afterCompletion(int status)
      {
      }
   }
}

