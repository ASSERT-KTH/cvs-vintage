/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import org.jboss.ejb.EntityEnterpriseContext;
import java.rmi.RemoteException;

/**
 * ActivateEntityCommand handles the EntityBean activate message.
 * This command is invoked after the bean's ejbActivate is invoked.
 * This command notifies the store that a context has been assigned
 * to an instance of the bean. This is a place where a store can 
 * initialize the "PersistenceContext" maintianed in the context.
 *      
 * Life-cycle:
 *		Tied to CMPStoreManager.
 *    
 * Multiplicity:	
 *		One per CMPStore.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public interface ActivateEntityCommand
{
   // Public --------------------------------------------------------
   
   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException;
}
