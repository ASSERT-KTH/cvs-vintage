/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.util.Collections;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

import org.jboss.metadata.ConfigurationMetaData;

/**
 * This is only optimized findByPrimaryKey right now, but this is where
 * we will put full query caching.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class FindByPrimaryKeyInterceptor 
      extends AbstractEntityTypeInterceptor
{
   private int commitOption;
   
   public void create() throws Exception
   {
      // get the commit option
      ConfigurationMetaData configuration = 
            getContainer().getBeanMetaData().getContainerConfiguration();
      commitOption = configuration.getCommitOption();
   }

   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      // Check if findByPrimaryKey
      // If so we check if the entity is in cache first
      if(invocation.getMethod().getName().equals("findByPrimaryKey") && 
            commitOption != ConfigurationMetaData.B_COMMIT_OPTION && 
            commitOption != ConfigurationMetaData.C_COMMIT_OPTION)
      {
         Object id = invocation.getArguments()[0];

         // check if the id is active in the cache
         if(getContainer().getInstanceCache().isActive(id))
         {
               return new InvocationResponse(Collections.singleton(id));
         }
         else
         {
            return new InvocationResponse(Collections.singleton(id));
         }
      }
      return getNext().invoke(invocation);
   }
}
