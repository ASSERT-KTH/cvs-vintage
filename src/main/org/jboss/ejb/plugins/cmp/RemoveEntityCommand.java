/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import org.jboss.ejb.EntityEnterpriseContext;
import javax.ejb.RemoveException;

/**
 * RemoveEntityCommand handles the EntityBean remove message.
 * This command is invoked after the bean's ejbRemove is invoked.
 * This command should remove the current state of the instance.
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public interface RemoveEntityCommand
{
   // Public --------------------------------------------------------
   
   public void execute(EntityEnterpriseContext ctx) throws RemoveException;
}
