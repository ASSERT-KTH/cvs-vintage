/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import java.lang.reflect.Method;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 * FindEntityCommand handles finders that return a single bean.
 *      
 * Life-cycle:
 *      Tied to CMPStoreManager.
 *    
 * Multiplicity:   
 *      One per CMPStoreManager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.5 $
 */
public interface FindEntityCommand
{
   // Public --------------------------------------------------------
   
   public Object execute(Method finderMethod, 
                         Object[] args, 
                         EntityEnterpriseContext ctx)
      throws Exception;
}
