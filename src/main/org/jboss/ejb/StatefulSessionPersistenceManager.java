/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public interface StatefulSessionPersistenceManager
   extends ContainerPlugin
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void createSession(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws Exception;
      
   public void activateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException;
   
   public void passivateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException;

   public void removeSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException, RemoveException;
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

