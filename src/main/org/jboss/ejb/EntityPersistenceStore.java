/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.ContainerPlugin;
import org.jboss.util.FinderResults;

/**
 *	This interface is implemented by any EntityBean persistence Store.
 *
 *  These stores just deal with the persistence aspect of storing java objects
 *  They need not be aware of the EJB semantics.
 *
 *  They act as delegatees for the CMPEntityPersistenceManager class.
 * 
 *	@see EntityPersistenceManager
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 *	@version $Revision: 1.5 $
 */
public interface EntityPersistenceStore
extends ContainerPlugin
{
   // Public --------------------------------------------------------

	/**
	 *	This method is called whenever an entity is to be created. The persistence manager
	 *	is responsible for handling the results properly wrt the persistent store.
	 *
	 *  The return is 
	 *  The primary key in case of CMP PM
	 *  Null in case of BMP PM (but no store should exist)
	 *
	 * @param   m  the create method in the home interface that was called
	 * @param   args  any create parameters
	 * @param   instance  the instance being used for this create call
	 * @return  Object, the primary key computed by CMP PM or null for BMP
	 * @exception   Exception  
	 */
   public Object createEntity(Method m, Object[] args, EntityEnterpriseContext instance)
      throws Exception;

	/**
	 *	This method is called when single entities are to be found. The persistence manager must find out
	 *	whether the wanted instance is available in the persistence store, if so it returns
	 *  the primary key of the object.
	 *
	 * @param   finderMethod  the find method in the home interface that was called
	 * @param   args  any finder parameters
	 * @param   instance  the instance to use for the finder call
	 * @return     a primary key representing the found entity
	 * @exception   RemoteException  thrown if some system exception occurs
	 * @exception   FinderException  thrown if some heuristic problem occurs
	 */
   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext instance)
      throws Exception;
   
	/**
	 *	This method is called when collections of entities are to be found. The persistence manager must find out
	 *	whether the wanted instances are available in the persistence store, and if so 
	 *  it must return a collection of primaryKeys.
	 *
	 * @param   finderMethod  the find method in the home interface that was called
	 * @param   args  any finder parameters
	 * @param   instance  the instance to use for the finder call
	 * @return     an primary key collection representing the found entities
	 * @exception   RemoteException  thrown if some system exception occurs
	 * @exception   FinderException  thrown if some heuristic problem occurs
	 */
   public FinderResults findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext instance)
      throws Exception;


	/**
	 * This method is called when an entity shall be activated. 
	 *
	 * With the PersistenceManager factorization most EJB calls should not exists
	 * However this calls permits us to introduce optimizations in the persistence
	 * store.  Particularly the context has a "PersistenceContext" that a 
	 * PersistenceStore can use (JAWS does for smart updates) and this is as good a 
	 * callback as any other to set it up. 
	 *
	 * @param   instance  the instance to use for the activation
	 * @exception   RemoteException  thrown if some system exception occurs
	 */
   public void activateEntity(EntityEnterpriseContext instance)
      throws RemoteException;
   

	/**
	 *	This method is called whenever an entity shall be load from the underlying storage. The persistence manager
	 *	must load the state from the underlying storage and then call ejbLoad on the supplied instance.
	 *
	 * @param   instance  the instance to synchronize
	 * @exception   RemoteException  thrown if some system exception occurs
	 */
   public void loadEntity(EntityEnterpriseContext instance)
      throws RemoteException;
      
   /**
    * This method is called whenever a set of entities should be preloaded from
    * the underlying storage. The persistence store is allowed to make this a 
    * null operation
    * 
    * @param instances the EntityEnterpriseContexts for the entities that must be loaded
    * @param keys a PagableKeyCollection previously returned from findEntities. 
    */
   public void loadEntities(FinderResults keys)
      throws RemoteException;
      
	/**
	 *	This method is called whenever an entity shall be stored to the underlying storage. The persistence manager
	 *	must call ejbStore on the supplied instance and then store the state to the underlying storage.
	 *
	 * @param   instance  the instance to synchronize
	 * @exception   RemoteException  thrown if some system exception occurs
	 */
   public void storeEntity(EntityEnterpriseContext instance)
      throws RemoteException;


	/**
	 * This method is called when an entity shall be passivate. The persistence manager must call the ejbPassivate
	 *	method on the instance.                                 
     *                                     
	 * See the activate discussion for the reason for exposing EJB callback calls to 
	 * the store.
	 *
	 * @param   instance  the instance to passivate
	 * @exception   RemoteException  thrown if some system exception occurs
	 */
   public void passivateEntity(EntityEnterpriseContext instance)
      throws RemoteException;
      

	/**
	 * This method is called when an entity shall be removed from the underlying storage. The persistence manager 
	 *	must call ejbRemove on the instance and then remove its state from the underlying storage.
	 *
	 * @param   instance  the instance to remove
	 * @exception   RemoteException  thrown if some system exception occurs
	 * @exception   RemoveException  thrown if the instance could not be removed
	 */
   public void removeEntity(EntityEnterpriseContext instance)
      throws RemoteException, RemoveException;
}

