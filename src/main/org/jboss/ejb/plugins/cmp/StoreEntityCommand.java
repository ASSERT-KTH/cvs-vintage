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
 * StoreEntityCommand handles the EntityBean create message.
 * This command is invoked after the bean's ejbStore is invoked.
 * This command should update the current state of the instance.
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.3 $
 */
public interface StoreEntityCommand
{
   // Public --------------------------------------------------------
   
   public void execute(EntityEnterpriseContext ctx)
      throws RemoteException;
}
