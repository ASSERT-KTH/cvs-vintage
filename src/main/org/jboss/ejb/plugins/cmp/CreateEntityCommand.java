/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.cmp;

import java.lang.reflect.Method;
import org.jboss.ejb.EntityEnterpriseContext;
import javax.ejb.CreateException;

/**
 * CreateEntityCommand handles the EntityBean create message.
 * This command is invoked after the bean's ejbCreate is invoked.
 * This command should store the current state of the instance.
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
public interface CreateEntityCommand
{
   // Public --------------------------------------------------------
   
   public Object execute(Method m, 
                         Object[] args, 
                         EntityEnterpriseContext ctx)
      throws CreateException;
}
