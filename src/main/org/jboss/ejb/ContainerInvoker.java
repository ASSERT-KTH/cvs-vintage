/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Name;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public interface ContainerInvoker
   extends ContainerPlugin
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------
   
   // Public --------------------------------------------------------
   public EJBMetaData getEJBMetaData();
   
   public EJBHome getEJBHome();
   
   public EJBObject getStatelessSessionEJBObject()
      throws RemoteException;

   public EJBObject getStatefulSessionEJBObject(Object id)
      throws RemoteException;

   public EJBObject getEntityEJBObject(Object id)
      throws RemoteException;

   public Collection getEntityCollection(Collection enum)
      throws RemoteException;
      
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

