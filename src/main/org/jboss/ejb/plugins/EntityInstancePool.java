/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;

import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *  @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 *	@version $Revision: 1.14 $
 *      
 * <p><b>Revisions:</b>
 * <p><b>20010718 andreas schaefer:</b>
 * <ul>
 * <li>- Added statistics gathering
 * </ul>
 */
public class EntityInstancePool
   extends AbstractInstancePool
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   /**
    *   Return an instance to the free pool. Reset state
    *
    *   Called in 3 cases:
    *   a) Done with finder method
    *   b) Removed
    *   c) Passivated
    *
    * @param   ctx  
    */
   private boolean reclaim = false;

   public boolean getReclaim()
   {
      return reclaim;
   }

   public void setReclaim(boolean reclaim)
   {
      this.reclaim = reclaim;
   }

   public synchronized void free(EnterpriseContext ctx)
   {
       // If transaction still present don't do anything (let the instance be GC)
       if (ctx.getTransaction() != null) return ;
        
       // To simplify design we don't reuse the ctx. 
       if (reclaim) super.free(ctx);
   }
   
   // Z implementation ----------------------------------------------
   public void start()
      throws Exception
   {
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      mInstantiate.add();
      return new EntityEnterpriseContext(instance, getContainer());
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

