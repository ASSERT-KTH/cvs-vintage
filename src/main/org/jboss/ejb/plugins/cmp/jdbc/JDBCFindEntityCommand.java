/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;
import java.util.Collection;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.GenericEntityObjectFactory;

/**
 * JDBCFindEntityCommand finds a single entity, by deligating to
 * find entities and checking that only entity is returned.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.15 $
 */
public final class JDBCFindEntityCommand
{
   private static final String NO_SUCH_ENTITY = "No such entity!";

   private final JDBCStoreManager manager;

   public JDBCFindEntityCommand(JDBCStoreManager manager)
   {
      this.manager = manager;
   }

   public Object execute(Method finderMethod, Object[] args, EntityEnterpriseContext ctx, GenericEntityObjectFactory factory)
      throws FinderException
   {

      JDBCQueryCommand query = manager.getQueryManager().getQueryCommand(finderMethod);

      Collection result = query.execute(finderMethod, args, ctx, factory);
      if(result.isEmpty())
      {
         throw new ObjectNotFoundException(NO_SUCH_ENTITY);
      }
      else if(result.size() == 1)
      {
         return result.iterator().next();
      }
      else
      {
         throw new FinderException("More than one entity matches the finder criteria: " + result);
      }
   }
}
