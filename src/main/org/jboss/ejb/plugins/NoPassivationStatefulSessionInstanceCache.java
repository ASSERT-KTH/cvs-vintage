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
import java.util.Collections;

import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolContainer;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.logging.Logger;


/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*   @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.12 $
*/
public class NoPassivationStatefulSessionInstanceCache
implements InstanceCache
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    Container con;
    
    Map active = new HashMap();
    
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
    
    public synchronized EnterpriseContext get(Object id)
    throws RemoteException
    {
//DEBUG        Logger.debug("Stateful cache looking for ID "+id);
     
       // Do we have the context in cache?
       StatefulSessionEnterpriseContext ctx = 
         (StatefulSessionEnterpriseContext)active.get(id);
         
        // We don't have it in cache
        if (ctx == null) {
            
            // Get new instance from pool (bogus in our case)
            ctx = (StatefulSessionEnterpriseContext)((InstancePoolContainer)con).getInstancePool().get();
            
            // Activate
            ctx.setId(id);
            
            try {
                
                ((StatefulSessionContainer)con).getPersistenceManager().activateSession(ctx);
            }
            catch (Exception e) {
                
                throw new RemoteException("Object was not found");
            }
            
            insert(ctx);
        }
       // The context has the instance as well and the right id
       return ctx;
    }
    
    public synchronized void insert(EnterpriseContext ctx)
    {
       active.put(ctx.getId(), ctx) ;
    }
    
    
    
    public synchronized void remove(Object id)
    {
       Object ctx = active.remove(id);
    }
    
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
