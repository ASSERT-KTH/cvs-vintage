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
import java.util.LinkedList;

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
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.10 $
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
       //RandomEntityInstanceCache cache;
       
       public void run()
       {
         Logger.debug("Passivator started");
         // Passivation loop
         while(running)
         {
          //            Logger.debug("Clearing cache");
          // Passivate old. Lock cache first
          //synchronized(RandomEntityInstanceCache.this)
          synchronized(cache)
          {
              // Do not use cache (many to one entries)
              int currentActive = cache.size();
              if (currentActive > minActive)
              {
                 InstancePool pool = ((EntityContainer)con).getInstancePool();
                 
                 Logger.debug("Too many active instances:"+currentActive);
                 
                 // Passivate some instance; they need to be unlocked though
                 
                 //KeySet has cacheKeys (currentActive>0)	
                 Iterator keys = cache.keySet().iterator();
                 
                 while(keys.hasNext())
                 {
                   
                   Object key = keys.next();
                   
                   //Get the context
                   EntityEnterpriseContext ctx =  
                    (EntityEnterpriseContext) cache.get(key);
                   
                   
                   // Make sure we can work on it
                   Logger.debug("Checking:"+ctx.getId());
                            
                   //TODO do the Locking logic
                            try
                   {
                    Logger.debug("Passivating:"+ctx.getId());
                    
                                // Passivate the entry
                                ((EntityContainer)con).getPersistenceManager().passivateEntity(ctx);
                        
                    // Remove the entry	
                    cache.remove(key);
                    
                                //keep the count
                                currentActive--;
                   }
                    
                   catch (Exception e) { Logger.warning("Could not passivate instance");}
                 
                   // Are we done?
                   if (currentActive == minActive) break;
                 }
              
              }    
          }	
          // DEBUG Logger.debug("Passivation done");
                Logger.debug("Passivation done");
          
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
