/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import java.lang.reflect.Method;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.util.FinderResults;

/**
 * FindEntitiesCommand handles finders that return collections.
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStore.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.5 $
 */
public interface FindEntitiesCommand
{
   // Public --------------------------------------------------------
   
   public FinderResults execute(Method finderMethod, 
                             Object[] args, 
                             EntityEnterpriseContext ctx)
      throws Exception;
}
