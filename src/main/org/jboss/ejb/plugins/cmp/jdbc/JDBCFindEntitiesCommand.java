/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.FinderResults;

/**
 * Delegates to the specific query command.
 *
 * @see org.jboss.ejb.plugins.cmp.FindEntitiesCommand
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.16 $
 */
public class JDBCFindEntitiesCommand {
   private final JDBCStoreManager manager;
   
   public JDBCFindEntitiesCommand(JDBCStoreManager manager) {
      this.manager = manager;
   }
   
   public FinderResults execute(Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx) throws FinderException {   

      JDBCQueryCommand query = 
            manager.getQueryManager().getQueryCommand(finderMethod);
      return (FinderResults)query.execute(finderMethod, args, ctx);
   }
}
