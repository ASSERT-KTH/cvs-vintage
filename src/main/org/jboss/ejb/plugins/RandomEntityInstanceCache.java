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
import org.jboss.logging.Log;

/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*	@version $Revision: 1.11 $
*/
public class RandomEntityInstanceCache
extends NoPassivationEntityInstanceCache
{
    
    // Constants -----------------------------------------------------
    public static final String NAME = "Passivator";
    
    // Attributes ----------------------------------------------------
    boolean running = false; // Passivator thread running?
    
    int minActive = 100; // Always try to passivate if more than this nr are active
    
    long timeout = 10*1000L; // Passivation sweep sleep time
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public void start()
    throws Exception
    {
       running = true;
       new Thread(new Passivator(),NAME).start();
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
        public void run()
        {
            Log.setLog(new Log(NAME));
            Logger.debug("Passivator started");
            
            // Passivation loop
            while(running)
            {
                
                while(cache.size() > minActive)
                {
                    // Passivate some instance; they need to be unlocked though
                    
                    Logger.debug("Too many active instances: "+cache.size());
                    
                    Object key = null;
                    
                    // when using Collections.synchronizedMap,
                    // only the iterators need explicit synchronization 
                    synchronized(cache) {
                        Iterator iterator = cache.keySet().iterator();
                        if (iterator.hasNext()) key = iterator.next();
                    }
                    
                    if (key == null) {
                        // this should not happen so often 
                        // (ie only if minActive instances have been removed between the while
                        // and the synchronized(cache) )
                        break;
                    }
                    
                    //Get the context
                    EntityEnterpriseContext ctx = (EntityEnterpriseContext) cache.get(key);
                    
                    
                    // Make sure we can work on it
                    // DEBUG Logger.debug("Checking:"+ctx.getId());
                    
                    //TODO do the Locking logic
                    try {
                        Logger.debug("Passivating:"+ctx.getId());
                        
                        // Passivate the entry
                        ((EntityContainer)con).getPersistenceManager().passivateEntity(ctx);
                        
                        // Remove the entry	
                        cache.remove(key);
                    
                    } catch (Exception e) { Logger.warning("Could not passivate instance");}
                }    
                
                // DEBUG Logger.debug("Passivation done");
                
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
