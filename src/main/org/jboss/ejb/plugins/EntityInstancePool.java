/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * An entity bean instance pool.
 *
 * @version <tt>$Revision: 1.22 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *      
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Added statistics gathering
 * </ul>
 * <p><b>20010920 Sacha Labourey:</b>
 * <ul>
 * <li>- Moved "reclaim" flag (set by Bill Burke) to the AbstractInstancePool level.
 *       It can now be used for SLSB and MDB (pooling activated)
 * </ul>
 * <p><b>20011208 Vincent Harcq:</b>
 * <ul>
 * <li>- A TimedInstancePoolFeeder thread is started at first use of the pool
 *       and will populate the pool with new instances at a regular period.
 * </ul>
 */
public class EntityInstancePool
   extends AbstractInstancePool
{
   /**
    * Return an instance to the free pool. Reset state
    *
    * <p>Called in 3 cases:
    * <ul>
    *   <li>Done with finder method
    *   <li>Removed
    *   <li>Passivated
    * </ul>
    *
    * @param   ctx  
    */
   public synchronized void free(EnterpriseContext ctx)
   {
       // If transaction still present don't do anything (let the instance be GC)
       if (ctx.getTransaction() != null)
       {
          if( log.isTraceEnabled() )
             log.trace("Can Not FREE Entity Context because a Transaction exists.");
          return ;
       }

       super.free(ctx);
   }

   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      return new EntityEnterpriseContext(instance, getContainer());
   }
}
