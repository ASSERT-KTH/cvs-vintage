/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * PassivateEntityCommand handles the EntityBean passivate message.
 * This command is invoked after the bean's ejbPassivate is invoked.
 * This command notifies the store that the context has been revoked
 * from an instance. The store should cleanup the "PersistenceContext"
 * maintianed in the context. All data associated with the instance
 * should be dumped here. If the container intends to keep the data
 * cached (Commit options A and B) it will not call passivate.
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStore.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.4 $
 */
public interface PassivateEntityCommand
{
   // Public --------------------------------------------------------
   
   public void execute(EntityEnterpriseContext ctx);
}
