/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;

/**
 * This command establishes relationships for CMR fields that have
 * foreign keys mapped to primary keys.
 *
 * @author <a href="mailto:aloubyansky@hotmail.com">Alex Loubyansky</a>
 *
 * @version $Revision: 1.9 $
 */
public final class JDBCPostCreateEntityCommand
{
   // Attributes ------------------------------------
   private final JDBCEntityBridge entity;
   private final JDBCCMRFieldBridge[] cmrWithFKMappedToCMP;

   // Constructors ----------------------------------
   public JDBCPostCreateEntityCommand(JDBCStoreManager manager)
   {
      entity = manager.getEntityBridge();
      JDBCFieldBridge[] cmrFields = entity.getCMRFields();
      List fkToCMPList = new ArrayList(4);
      for(int i = 0; i < cmrFields.length; ++i)
      {
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge) cmrFields[i];
         JDBCCMRFieldBridge relatedCMRField = (JDBCCMRFieldBridge)cmrField.getRelatedCMRField();
         if(cmrField.hasFKFieldsMappedToCMPFields()
            || relatedCMRField.hasFKFieldsMappedToCMPFields())
         {
            fkToCMPList.add(cmrField);
         }
      }
      if(fkToCMPList.isEmpty())
         cmrWithFKMappedToCMP = null;
      else
         cmrWithFKMappedToCMP = (JDBCCMRFieldBridge[]) fkToCMPList
            .toArray(new JDBCCMRFieldBridge[fkToCMPList.size()]);
   }

   // Public ----------------------------------------
   public Object execute(Method m, Object[] args, EntityEnterpriseContext ctx)
   {
      if(cmrWithFKMappedToCMP == null)
         return null;

      for(int i = 0; i < cmrWithFKMappedToCMP.length; ++i)
      {
         JDBCCMRFieldBridge cmrField = cmrWithFKMappedToCMP[i];
         JDBCCMRFieldBridge relatedCMRField = (JDBCCMRFieldBridge)cmrField.getRelatedCMRField();
         if(cmrField.hasFKFieldsMappedToCMPFields())
         {
            Object relatedId = cmrField.getRelatedIdFromContext(ctx);
            if(relatedId != null)
            {
               try
               {
                  if(cmrField.isForeignKeyValid(relatedId))
                  {
                     cmrField.createRelationLinks(ctx, relatedId);
                  }
                  else
                  {
                     relatedCMRField.addRelatedPKWaitingForMyPK(relatedId, ctx.getId());
                  }
               }
               catch(Exception e)
               {
                  // no such object
               }
            }
         }
         else if(relatedCMRField.hasFKFieldsMappedToCMPFields())
         {
            cmrField.addRelatedPKsWaitedForMe(ctx);
         }
      }
      return null;
   }
}
