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
import org.jboss.invocation.InvocationResponse;
import org.jboss.ejb.plugins.AbstractInterceptor;
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
 * @version $Revision: 1.15 $
 */
public final class JDBCRelationInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      // We are going to work with the context a lot
      EntityEnterpriseContext ctx =
            (EntityEnterpriseContext)invocation.getEnterpriseContext();

      CMRMessage relationshipMessage = 
            (CMRMessage)invocation.getValue(CMRMessage.CMR_MESSAGE_KEY);

      if(relationshipMessage == null) 
      {
         // Not a relationship message. Invoke down the chain
         return getNext().invoke(invocation);
      }
      else if(CMRMessage.GET_RELATED_ID == relationshipMessage)
      {
         // call getRelateId
         JDBCCMRFieldBridge cmrField =
               (JDBCCMRFieldBridge)invocation.getArguments()[0];
         if(log.isTraceEnabled())
         {
            log.trace("Getting related id: field=" + cmrField.getFieldName() +
                  " id=" + ctx.getId());
         }
         return new InvocationResponse(cmrField.getRelatedId(ctx));
         
      }
      else if(CMRMessage.ADD_RELATION == relationshipMessage)
      {
         
         // call addRelation
         JDBCCMRFieldBridge cmrField =
               (JDBCCMRFieldBridge)invocation.getArguments()[0];
         
         Object relatedId = invocation.getArguments()[1];
         if(log.isTraceEnabled())
         {
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

         return new InvocationResponse(null);
         
      }
      else if(CMRMessage.REMOVE_RELATION == relationshipMessage)
      {
         
         // call removeRelation
         JDBCCMRFieldBridge cmrField = 
               (JDBCCMRFieldBridge)invocation.getArguments()[0];
         
         Object relatedId = invocation.getArguments()[1];
         if(log.isTraceEnabled())
         {
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

         return new InvocationResponse(null);
         
      }
      else
      {
         // this should not be possible we are using a type safe enum
         throw new EJBException("Unknown cmp2.0-relationship-message=" +
               relationshipMessage);
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
}
