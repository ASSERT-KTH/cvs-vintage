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
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.util.FastKey;

import org.jboss.metadata.EntityMetaData;

/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.8 $
*/
public class NoPassivationEntityInstanceCache
implements InstanceCache
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    Container con;
    
    Map fastCache = Collections.synchronizedMap(new HashMap());
    Map fastKeys = Collections.synchronizedMap(new HashMap());
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
    
    /**
    *  get(Object fastKey)
    * 
    * get works from the hack of the fastKey.
    * The class contains a "fastCache" that hashes fastKeys representing
    * EJBObjects on client to the context of an instance.
    * In case the fastKey is not found in fastCache then the method goes on to find if 
    * it is already loaded but under a different fastKey (I.e. an EJBOBject is already 
    * working on this instance) in case it does, it adds the new pair fastKey, instance to the 
    * cache.  In case it is not found it creates a new context and associates fastKey to instance
    * in the cache.   Remove takes care of working from the DB primary Key and killing
    * all the associations of fastKey to context. (can be many)
    * This class relies on the proper implementation of the hash and equals for the 
    * lookup by db key, but is lenient in case something screws up...
    *
    * MF FIXME: The synchronization is probably very f*cked up, someone needs to think
    * through the use cases of this puppy.  I know it is most probably broken as is.
    */
    
    public synchronized EnterpriseContext get(Object id)
    throws RemoteException
    {
        
        if (!(id instanceof FastKey)) {
           
            // That instance should be used by a FastKey already
            // retrieve the List of Keys for that instance
            LinkedList keysList = (LinkedList) fastKeys.get(id);
            
            // Get the context for the first one (they are all associated to that context)
            return get((FastKey) keysList.getFirst());
        }
        
        // Use the FastKey for the rest of the method
        FastKey fastKey = (FastKey) id;
        
        // TODO: minimize synchronization of IM
        
        EntityEnterpriseContext ctx;
        InstanceInfo info = null;
        while ((ctx = (EntityEnterpriseContext)fastCache.get(fastKey)) != null)
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
            //Maybe it is in the cache but under another fastKey, check for the DB id
            if (fastKeys.containsKey(fastKey.id)) {
                
                // Ok, the instance is in fastCache but under another fastKey
                
                // retrieve the List of Keys for that instance
                LinkedList keysList = (LinkedList) fastKeys.get(fastKey.id);
                
                // Get the context for the first one (they are all associated to that context)
                ctx = (EntityEnterpriseContext) fastCache.get(keysList.getFirst());
                
                // Add the fastkey to the List
                keysList.addLast(fastKey);
                
                // MF FIXME: I don't think we need this operation but just in case
                fastKeys.put(fastKey.id, keysList);              
                
                // Store the context in the fastCache
                fastCache.put(fastKey, ctx);
                
                // Redo the call the previous cache will work the synchronization
                return get(fastKey);
            }
            
            // The instance is brand new and not know to the cache structures
            else {
                
                // Get new instance from pool
                ctx = (EntityEnterpriseContext)((InstancePoolContainer)con).getInstancePool().get();
                
                // The context only knows about the Database id
                ctx.setId(fastKey.id);
                
                // Activate it
                ((EntityContainer)con).getPersistenceManager().activateEntity(ctx);
                
                // Initiate the fastKeys List for this Id
                //LinkedList keysList = (LinkedList) Collections.synchronizedList(new LinkedList());
                LinkedList keysList = new LinkedList();
                
                // Add this fastKey at least
                keysList.addLast(fastKey);
                
                // Keep the list under the DB ID
                fastKeys.put(fastKey.id, keysList);
                
                // implicit passing with the ctx
                ctx.setFastKey(fastKey);
                
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
        return ctx;
    }
    
    public synchronized void insert( EnterpriseContext ctx)
    {
        InstanceInfo info = createInstanceInfo(ctx);
        ((EntityEnterpriseContext)ctx).setCacheContext(info);
        info.lock();
        
        // Cache can know about the instance now
        fastCache.put(((EntityEnterpriseContext)ctx).getFastKey(), ctx);
    
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
    * unfortunately a f*cked up hash and equals will create leaks in 
    * the maps :(((((
    *
    * Actually that may be one way to diagnose bad behaviour of the stuff
    * (I guess, MF)
    */
    public synchronized void remove(Object id)
    {
        // remote the List of keys from the ID map, it returns the list
        LinkedList list = (LinkedList) fastKeys.remove(id);
        
        // If the fastKey wasn't used there is no link to a context
        if (list == null) return ;
            
        Iterator keysIterator = list.listIterator();
        
        // We need to remove all the associations that exists with the context
        Object ctx = null;
        
        while (keysIterator.hasNext()) {
            
            Object currentFastKey = keysIterator.next();
            
            // remove the fastKey from fastCache 
            ctx = fastCache.remove(currentFastKey);
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
    }
}
