/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;    
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;

import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityInstanceCache;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.util.FastKey;
import org.jboss.ejb.CacheKey;

import org.jboss.metadata.EntityMetaData;

/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:andreas.schaefer@madplanet.com">Andy Schaefer</a>
*	@version $Revision: 1.9 $
*/
public class NoPassivationEntityInstanceCache
implements EntityInstanceCache
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    Container con;
    
    Map cache = Collections.synchronizedMap(new HashMap());
    Map cacheKeys = Collections.synchronizedMap(new HashMap());
    boolean isReentrant;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    
    /**
    *   Set the callback to the container. This is for initialization.
    *   The IM may extract the configuration from the container.
    *
    * @param   c  
    */
    public void setContainer(Container c)
    {
        this.con = c;
    }
    
    public void init()
    throws Exception
    {
        isReentrant = ((EntityMetaData)con.getBeanMetaData()).isReentrant();
    }
    
    public void start()
    throws Exception
    {
    }
    
    public void stop()
    {
    }
    
    public void destroy()
    {
    }
    
    public Object createCacheKey( Object id ) {
		// If no fastkey then just return id
		// Here is where you supply the implementation of the cache you need
		// This could be done from configuration.
		
		return new FastKey( id );
	}
	
    /**
    *  get(Object CacheKey)
    * 
	*
    * MF FIXME: The synchronization is probably very f*cked up, someone needs to think
    * through the use cases of this puppy.  I know it is most probably broken as is.
    */
    
    public synchronized EnterpriseContext get(Object id)
    throws RemoteException
    {
        
        // Use the FastKey for the rest of the method
        CacheKey cacheKey = (CacheKey) id;
        
        // TODO: minimize synchronization of IM
        
        EntityEnterpriseContext ctx;
        InstanceInfo info = null;
        while ((ctx = (EntityEnterpriseContext)cache.get(cacheKey)) != null)
        {
            synchronized(ctx)
            {
                info = (InstanceInfo)ctx.getCacheContext();
                if (!info.isLocked())
                break;
                //DEBUG            Logger.log("Cache is waiting for "+id+"("+info.isLocked()+","+ctx.getTransaction()+")");
                
                // Check if same tx; reentrant call
                try                                          
                {
                    if (ctx.getTransaction() != null && ctx.getTransaction().equals(con.getTransactionManager().getTransaction()))
                    {
                        if (isReentrant)
                        {
                            break;
                        } else
                        {
                            // MF FIXME
                            // This is wrong but doing it right requires time
                            // The problem is that the entity EJB calls from an instance
                            // come back on the instance and in the presence of a 
                            // transaction it throws the exception....
                            // Since I suspect most people will use the EJBObject method calls before they set the reentrant right :)
                            // I would rather bypass the default for now. 
                            
                            //throw new RemoteException("Reentrant call not allowed");
                            break;
                        }
                    }
                } catch (SystemException e)
                {
                    throw new ServerException("Could not check for re-entrancy", e);
                }
                
                
                // Wait for previous tx to finish
                try 
                { 
                    ctx.wait(5000); 
                } catch (InterruptedException e) 
                { 
                    // Someone else is using this id right now
                    throw new ServerException("Time out",e); 
                }
            }
        }
        
        if (ctx == null) // Not in fast cache under that fastKey
        {
            //Maybe it is in the cache but under another cacheKey, check for the DB id
			// Also if cache is virtual work from the id
            if (cacheKeys.containsKey(cacheKey.id) || cacheKey.isVirtual) {
                
                
				// Ok, the instance is in Cache but under another cacheKey
				
                // retrieve the List of Keys for that instance
                LinkedList keysList = (LinkedList) cacheKeys.get(cacheKey.id);
                
                // Get the context for the first one (they are all associated to that context)
                ctx = (EntityEnterpriseContext) cache.get(keysList.getFirst());
                
				// Only if we have a real cache key, corresponding to the client
				if (!cacheKey.isVirtual) {
                	// Add the fastkey to the List
                	keysList.addLast(cacheKey);
                	
                	// Store the context in the cache under the new cacheKey
                	cache.put(cacheKey, ctx);
				}
				
                // Redo the call the previous cache will work the synchronization
                return get(cacheKey);
            }
            
            // The instance is brand new and not known to the cache structures
            else {
                
                // Get new instance from pool
                ctx = (EntityEnterpriseContext)((InstancePoolContainer)con).getInstancePool().get();
                
                // The context only knows about the Database id
                ctx.setId(cacheKey.id);
                
                // Activate it
                ((EntityContainer)con).getPersistenceManager().activateEntity(ctx);
                
                // Initiate the fastKeys List for this Id
                //LinkedList keysList = (LinkedList) Collections.synchronizedList(new LinkedList());
                LinkedList keysList = new LinkedList();
                
                // Add this fastKey at least
                keysList.addLast(cacheKey);
                
                // Keep the list under the DB ID
                cacheKeys.put(cacheKey.id, keysList);
                
				// Give the cacheKey to the context
				ctx.setCacheKey(cacheKey);
				
                // insert
                insert(ctx);
            }
        } else
        {
            // Lock the instance
            info.lock();
        }
        
        // At this point we own the instance with the given identity
        //      Logger.log("Got entity:"+ctx.getId());
		
		// Tell the context the key 
		ctx.setCacheKey(cacheKey);
		
        return ctx;
    }
    
	public synchronized void insert (EnterpriseContext ctx) {
		
		InstanceInfo info = createInstanceInfo(ctx);
        ((EntityEnterpriseContext)ctx).setCacheContext(info);
        info.lock();
        
        // Cache can know about the instance now
        cache.put(((EntityEnterpriseContext) ctx).getCacheKey(), ctx);
    
	}
	
    public void release(EnterpriseContext ctx)
    {
        // This context is now available for other threads
        synchronized(ctx)
        {
            ((InstanceInfo)((EntityEnterpriseContext)ctx).getCacheContext()).unlock();
            //         Logger.log("Release entity:"+ctx.getId());
            if (!((InstanceInfo)((EntityEnterpriseContext)ctx).getCacheContext()).isLocked())
                ctx.notifyAll();
        }
    }
    
    /*
	* This needs the id hash and equals to work
    *
    */
    public synchronized void remove(Object id)
    {
        // remote the List of keys from the ID map, it returns the list
        LinkedList list = (LinkedList) cacheKeys.remove(id);
        
        // If the cacheKey weren't used there is no link to a context
        if (list == null) return ;
            
        Iterator keysIterator = list.listIterator();
        
        // We need to remove all the associations that exists with the context
        Object ctx = null;
        
        while (keysIterator.hasNext()) {
            
            Object currentCacheKey = keysIterator.next();
            
            // remove the cacheKey from cache 
            ctx = cache.remove(currentCacheKey);
        }
        
        
        // We will need notification (MF FIXME actually not anymore imho)
        synchronized(ctx)
        {
            ctx.notifyAll();
        }
    }
    
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    protected InstanceInfo createInstanceInfo(EnterpriseContext ctx)
    {
        return new InstanceInfo(ctx);
    }
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
    class InstanceInfo
    {
        int locked = 0; // 0 == unlocked, >0 == locked
        
		CacheKey key ;
		
        EntityEnterpriseContext ctx;
        
        InstanceInfo(EnterpriseContext ctx)
        {
            this.ctx = (EntityEnterpriseContext)ctx;
        }
        
        public void lock()
        {
            locked++;
        }
        
        public void unlock()
        {
            locked--;
        }
        
        public boolean isLocked()
        {
            return locked > 0;
        }
        
        public EntityEnterpriseContext getContext()
        {
            return ctx;
        }
		
		public void setCacheKey(CacheKey key) {
			
			this.key = key;
    	}
		
		public CacheKey getCacheKey() {
			
			return key;
		}
    }
}
