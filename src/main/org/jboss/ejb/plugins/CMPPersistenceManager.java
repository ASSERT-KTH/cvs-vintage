/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/                           
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.util.FastKey;

/**
*	The CMP Persistence Manager implements the semantics of the CMP
*  EJB 1.1 call back specification. 
*
*  This Manager works with a "EntityPersistenceStore" that takes care of the 
*  physical storing of instances (JAWS, JDBC O/R, FILE, Object).
*      
*	@see <related>
*	@author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*	@version $Revision: 1.4 $
*/
public class CMPPersistenceManager
implements EntityPersistenceManager {
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    EntityContainer con;
    // Physical persistence implementation
    EntityPersistenceStore store;
    
    // The EJB Methods, the reason for this class
    Method ejbLoad;
    Method ejbStore;
    Method ejbActivate;
    Method ejbPassivate;
    Method ejbRemove;
    
    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public void setContainer(Container c)	{
        con = (EntityContainer)c;
        if (store != null) store.setContainer(c);
    }
    
    
    public void setPersistenceStore(EntityPersistenceStore store) {
        this.store= store;
        
        //Give it the container
        if (con!= null) store.setContainer(con);
    }
    
    public void init()
    throws Exception {
        
        // The common EJB methods
        ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
        ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
        ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
        ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
        ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
        
        // Initialize the sto re
        store.init();
    }
    
    public void start() 
    throws Exception {
        
        store.start();
    }
    
    public void stop() {
        store.stop();
    }
    
    public void destroy() {
        store.destroy();
    }
    
    public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
    throws RemoteException, CreateException {
        // Get methods
        try {
            
            Method createMethod = con.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
            Method postCreateMethod = con.getBeanClass().getMethod("ejbPostCreate", m.getParameterTypes());
            
            // Call ejbCreate on the target bean
            createMethod.invoke(ctx.getInstance(), args);
            
            // Have the store persist the new instance, the return is the key
            Object id = store.createEntity(m, args, ctx);
            
            // Set the key on the target context
            ctx.setId(id);
            
            // Create a new FastKey
            FastKey fastKey =  new FastKey(id);
            
            // Pass it implicitely!
            ctx.setFastKey(fastKey);
            
            // Lock instance in cache
            con.getInstanceCache().insert(ctx);
            
            // Create EJBObject
            ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(fastKey));
            
            postCreateMethod.invoke(ctx.getInstance(), args);
        
        } 
        catch (InvocationTargetException e) {
            throw new CreateException("Create failed:"+e);
        } 
        catch (NoSuchMethodException e) {
            throw new CreateException("Create methods not found:"+e);
        } 
        catch (IllegalAccessException e) {
            throw new CreateException("Could not create entity:"+e);
        }
    }
    
    public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
    throws RemoteException, FinderException {
      
        return store.findEntity(finderMethod, args, ctx);
    }
    
    public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
    throws RemoteException, FinderException {
        
        return store.findEntities(finderMethod, args, ctx);
    }
    
    /*
    * activateEntity(EnterpriseContext ctx) 
    *
    * The method calls the target beans for spec compliant callbacks.
    * Since these are pure EJB calls it is not obvious that the store should 
    * expose the interfaces.  In case of jaws however we found that store specific
    * contexts could be set in the activateEntity calls and hence a propagation of 
    * the call made sense.  The persistence store is called for "extension" purposes.
    *
    * @see activateEntity on EntityPersistenceStore.java
    */
    public void activateEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        
        // Call bean
        try
        {
            ejbActivate.invoke(ctx.getInstance(), new Object[0]);
        } catch (Exception e)
        {
            throw new ServerException("Activation failed", e);
        }
        
        store.activateEntity(ctx);
    }
    
    public void loadEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        
        try {
            
            // Have the store deal with create the fields of the instance
            store.loadEntity(ctx);
            
            // Call ejbLoad on bean instance, wake up!
            ejbLoad.invoke(ctx.getInstance(), new Object[0]);
        }
        catch (Exception e) {
            throw new ServerException("Load failed", e);
        }
    }
    
    public void storeEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        //      Logger.log("Store entity");
        try {
            
            // Prepare the instance for storage
            ejbStore.invoke(ctx.getInstance(), new Object[0]);
            
            // Have the store deal with storing the fields of the instance
            store.storeEntity(ctx);
        } 
        
        catch (Exception e) {
            throw new ServerException("Store failed", e);
        }
    }
    
    public void passivateEntity(EntityEnterpriseContext ctx)
    throws RemoteException {
        
        try {
            
            // Prepare the instance for passivation 
            ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
        } 
        catch (Exception e) {
            
            throw new ServerException("Passivation failed", e);
        }
        
        store.passivateEntity(ctx);
    }
    
    public void removeEntity(EntityEnterpriseContext ctx)
    throws RemoteException, RemoveException {
        
        try {
            
            // Call ejbRemove
            ejbRemove.invoke(ctx.getInstance(), new Object[0]);
        } 
        catch (Exception e){
            
            throw new RemoveException("Could not remove "+ctx.getId());
        }
        
        store.removeEntity(ctx);
    }
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}

