/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import org.jboss.ejb.EntityEnterpriseContext;
import java.rmi.RemoteException;

/**
 * LoadEntityCommand handles the EntityBean load message.
 * This command is invoked before the bean's ejbLoad is invoked.
 * This command should load the current state of the instance.
 *      
 * Life-cycle:
 *		Tied to CMPStoreManager.
 *    
 * Multiplicity:	
 *		One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public interface LoadEntityCommand
{
   // Public --------------------------------------------------------
   
   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException;
}
