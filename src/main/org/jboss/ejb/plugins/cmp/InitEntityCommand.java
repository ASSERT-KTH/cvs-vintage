/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import org.jboss.ejb.EntityEnterpriseContext;

/**
 * InitEntityCommand informs the store that a new entity is about
 * to be created. This command is invoked before the bean's ejbCreate
 * is invoked. This command must reset the value of all cmpFields
 * to 0 or null, as is required by the EJB 2.0 specification.  
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public interface InitEntityCommand
{
   // Public --------------------------------------------------------

   public void execute(EntityEnterpriseContext ctx);
}
