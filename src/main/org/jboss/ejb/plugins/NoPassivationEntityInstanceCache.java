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
import org.jboss.ejb.CacheKey;

import org.jboss.metadata.EntityMetaData;
import org.jboss.logging.Logger;

/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:andreas.schaefer@madplanet.com">Andy Schaefer</a>
*	@version $Revision: 1.15 $
*/
public class NoPassivationEntityInstanceCache
implements EntityInstanceCache
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    Container con;
    
    Map cache = Collections.synchronizedMap(new HashMap());
    
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
       
       return new CacheKey( id );
    }
    
    /**
    *  get(Object CacheKey)
    * 
    * We base our lookups on the CacheKey. 
    */
    
    public synchronized EnterpriseContext get(Object id)
    throws Exception
    {
        
        // Use the CacheKey for the rest of the method
        CacheKey cacheKey = (CacheKey) id;
		
//DEBUG		Logger.debug("Get "+cacheKey+" from cache");
        
        EntityEnterpriseContext ctx;
      
       // Lookup the instance 
       ctx = (EntityEnterpriseContext)cache.get(cacheKey);
       
        if (ctx == null) // Not in fast cache under that cacheKey
        {
            
            // Get new instance from pool
            ctx = (EntityEnterpriseContext)((InstancePoolContainer)con).getInstancePool().get();
            
            // The context only knows about the Database id
            ctx.setId(cacheKey.id);
            
            // Activate it
            ((EntityContainer)con).getPersistenceManager().activateEntity(ctx);
              
            // insert
            cache.put(cacheKey, ctx);
        }
        
       
        // At this point we own the instance with the given identity
        
       // Tell the context the key 
       ctx.setCacheKey(cacheKey);
       
        // DEBUG Logger.debug("Got entity:"+ctx.getId());
       
        return ctx;
    }
    
    public synchronized void insert (EnterpriseContext ctx) {
    
        // Cache can know about the instance now
        cache.put(((EntityEnterpriseContext) ctx).getCacheKey(), ctx);
    }
    
    /*
    * Remove works with the cachekey that the cache constructs.  Leakage will happen if the cache key 
    * doesn't consistently see 
    * m1 = new CacheKey(id); and 
    * m2 = new CacheKey(id);
    * as hashCode equals and equals.  The implementation of org.jboss.CacheKey behaves correctly
    *
    */
    public synchronized void remove(Object id)
    {
        // remove usually comes with the id of the db instance
        cache.remove(createCacheKey(id));                         
    }
    
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
