/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;
import javax.ejb.NoSuchEntityException;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public interface InstanceCache
   extends ContainerPlugin
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   /**
    *   Get an instance of a particular identity. 
    *   This may involve activation if necessary.
    *
    *   This method is never called for stateless session beans.
    *
    * @param   id  
    * @return     Context /w instance
    * @exception   RemoteException  
    */
   public EnterpriseContext get(Object id)
      throws RemoteException, NoSuchEntityException;

   /**
    *   Insert an active instance after creation or activation. Write-lock is required.
    *
    * @param   ctx  
    */
   public void insert(EnterpriseContext ctx);

   /**
    *   Release an instance after usage.
    *
    * @param   ctx  
    */
   public void release(EnterpriseContext ctx);

   /**
    *   Remove an instance corresponding to the given id after removal
    *
    * @param   ctx  
    */
   public void remove(Object id);
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

