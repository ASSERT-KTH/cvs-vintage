/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public interface EntityPersistenceManager
   extends ContainerPlugin
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void createEntity(Method m, Object[] args, EntityEnterpriseContext instance)
      throws RemoteException, CreateException;

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext instance)
      throws RemoteException, FinderException;
      
   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext instance)
      throws RemoteException, FinderException;

   public void activateEntity(EntityEnterpriseContext instance)
      throws RemoteException;
   
   public void loadEntity(EntityEnterpriseContext instance)
      throws RemoteException;
      
   public void storeEntity(EntityEnterpriseContext instance)
      throws RemoteException;

   public void passivateEntity(EntityEnterpriseContext instance)
      throws RemoteException;
      
   public void removeEntity(EntityEnterpriseContext instance)
      throws RemoteException, RemoveException;
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

