/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.logging.Logger;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class RandomEntityInstanceCache
   extends NoPassivationEntityInstanceCache
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   boolean running = false; // Passivator thread running?
   
   int minActive = 100; // Always try to passivate if more than this nr are active
   
   long timeout = 60*1000L; // Passivation sweep sleep time
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void start()
      throws Exception
   {
      running = true;
      new Thread(new Passivator()).start();
   }
   
   public void stop()
   {
      running = false;
   }

   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   class Passivator
      implements Runnable
   {
      RandomEntityInstanceCache cache;
      
      public void run()
      {
         Logger.debug("Passivator started");
         // Passivation loop
         while(running)
         {
//            System.out.println("Clearing cache");
            // Passivate old. Lock cache first
            synchronized(RandomEntityInstanceCache.this)
            {
               int currentActive = active.size();
               if (currentActive > minActive)
               {
                  InstancePool pool = con.getInstancePool();
                  
                  Logger.debug("Too many active instances:"+currentActive);
                  // Passivate some instance; they need to be unlocked though
                  Collection values = active.values();
                  Iterator enum = values.iterator();
                  while(enum.hasNext())
                  {
                     EntityEnterpriseContext ctx = (EntityEnterpriseContext)enum.next();
                     Logger.debug("Checking:"+ctx.getId());
                     InstanceInfo info = (InstanceInfo)ctx.getCacheContext();
                     if (!info.isLocked())
                     {
                        // Passivate
                        try
                        {
                           Logger.debug("Passivating:"+ctx.getId());
                           ((EntityContainer)con).getPersistenceManager().passivateEntity(ctx);
                           enum.remove();
                           RandomEntityInstanceCache.this.notifyAll(); // Necessary? (I think so!)
                           pool.free(ctx);
                        } catch (RemoteException e)
                        {
                           e.printStackTrace(); // TODO: log this
                        }
                        
                        // Check if enough are passivated
                        currentActive--;
                        if (currentActive == minActive)
                           break;
                     }
                  }
               }
            }
            
//            System.out.println("Passivation done");
            // Sleep
            try
            {
               Thread.sleep(timeout);
            } catch (InterruptedException e)
            {
               // Ignore
            }
         }
      }
   }
}
