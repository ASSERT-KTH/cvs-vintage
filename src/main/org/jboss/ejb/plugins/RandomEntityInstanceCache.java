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
*	@version $Revision: 1.5 $
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
				//            Logger.log("Clearing cache");
                // Passivate old. Lock cache first
                synchronized(RandomEntityInstanceCache.this)
                {
                    // Do not use cache (many to one entries)
                    int currentActive = fastKeys.size();
                    if (currentActive > minActive)
                    {
                        InstancePool pool = ((EntityContainer)con).getInstancePool();
                        
                        Logger.debug("Too many active instances:"+currentActive);
                        
                        // Passivate some instance; they need to be unlocked though
                        
                        Iterator ids = fastKeys.keySet().iterator();
                        
                        while(ids.hasNext())
                        {
                            
                            Object id = ids.next();
                            
                            //Get the context
                            EntityEnterpriseContext ctx = 
                            (EntityEnterpriseContext)fastCache.get(((LinkedList) fastKeys.get(id)).getFirst());
                            
                            
                            // Make sure we can work on it
                            Logger.debug("Checking:"+ctx.getId());
                            InstanceInfo info = (InstanceInfo)ctx.getCacheContext();
                            
                            //We we locked?
                            if (!info.isLocked())
                            {
                                // Nope then Passivate
                                try
                                {
                                    Logger.debug("Passivating:"+ctx.getId());
                                    ((EntityContainer)con).getPersistenceManager().passivateEntity(ctx);
                                    
                                    
                                    // Get the List by removing from fastKeys 
                                    LinkedList keysList = (LinkedList) fastKeys.remove(ids.next());
                                    
                                    // Remove all the fastKeys from the cache
                                    Iterator iterator = keysList.listIterator();
                                    
                                    while (iterator.hasNext()) {
                                        
                                        fastCache.remove(iterator.next());
                                    }
                                    
                                    currentActive--;
                                }
                                
                                catch (Exception e) { Logger.log("Could not passivate instance");}
                            }
                            
                            if (currentActive == minActive) break;
                        
                        }
					}
				}
				//            Logger.log("Passivation done");
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