/**
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import javax.ejb.EJBException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.invocation.Invocation;
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
 * @version $Revision: 1.10 $
 */
public class JDBCRelationInterceptor extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
   private static final Method GET_RELATED_ID;
   private static final Method ADD_RELATION;
   private static final Method REMOVE_RELATION;
   
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
   private EntityContainer container;

   /**
    * The log.
    */ 
   private Logger log;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container)
   {
      this.container = (EntityContainer)container;

      JDBCStoreManager manager = null;
      try {
         EntityContainer entityContainer = (EntityContainer)container;
         CMPPersistenceManager cmpManager = 
               (CMPPersistenceManager)entityContainer.getPersistenceManager();
         manager = (JDBCStoreManager) cmpManager.getPersistenceStore();
      } catch(ClassCastException e) {
         throw new EJBException("JDBCRealtionInteceptor can only be used " +
               "JDBCStoreManager", e);
      }
      
      log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            manager.getMetaData().getName());
   }
   
   public Container getContainer()
   {
      return container;
   }
   
   // Interceptor implementation --------------------------------------
   
   public Object invoke(Invocation mi) throws Exception
   {
      // We are going to work with the context a lot
      EntityEnterpriseContext ctx =
            (EntityEnterpriseContext)mi.getEnterpriseContext();
      
      if(GET_RELATED_ID.equals(mi.getMethod()))
      {
            
         // call getRelateId
         JDBCCMRFieldBridge cmrField =
               (JDBCCMRFieldBridge)mi.getArguments()[0];
         if(log.isTraceEnabled()) {
            log.trace("Getting related id: field=" + cmrField.getFieldName() +
                  " id=" + ctx.getId());
         }
         return cmrField.getRelatedId(ctx);
         
      } else if(ADD_RELATION.equals(mi.getMethod()))
      {
         
         // call addRelation
         JDBCCMRFieldBridge cmrField =
               (JDBCCMRFieldBridge)mi.getArguments()[0];
         
         Object relatedId = mi.getArguments()[1];
         if(log.isTraceEnabled()) {
            log.trace("Add relation: field=" + cmrField.getFieldName() +
                  " id=" + ctx.getId() +
                  " relatedId=" + relatedId);
         }
         cmrField.addRelation(ctx, relatedId);
         
         RelationData relationData = getRelationData(cmrField);
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
         if(log.isTraceEnabled()) {
            log.trace("Remove relation: field=" + cmrField.getFieldName() +
                  " id=" + ctx.getId() +
                  " relatedId=" + relatedId);
         }
         cmrField.removeRelation(ctx, relatedId);
         
         RelationData relationData = getRelationData(cmrField);
         relationData.removeRelation(
               cmrField,
               ctx.getId(),
               cmrField.getRelatedCMRField(),
               relatedId);

         return null;
         
      } else
      {
         // Not a message. Invoke down the chain
         return getNext().invoke(mi);
      }
   }
   
   private RelationData getRelationData(JDBCCMRFieldBridge cmrField)
   {
      JDBCStoreManager manager = cmrField.getJDBCStoreManager();
      JDBCRelationMetaData relationMetaData = 
            cmrField.getMetaData().getRelationMetaData();

      
      RelationData relationData = 
            (RelationData)manager.getApplicationTxData(relationMetaData);

      if(relationData == null)
      {
         relationData = new RelationData(
               cmrField, cmrField.getRelatedCMRField());
         manager.putApplicationTxData(relationMetaData, relationData);
      }
      return relationData;
   }

   // Private  ----------------------------------------------------
}

