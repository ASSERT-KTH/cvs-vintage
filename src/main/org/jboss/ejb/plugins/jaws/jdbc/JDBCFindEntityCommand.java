/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import java.util.Collection;
import java.util.ArrayList;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JPMFindEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand;

/**
 * JAWSPersistenceManager JDBCFindEntityCommand
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCFindEntityCommand implements JPMFindEntityCommand
{
   // Attributes ----------------------------------------------------
   
   JDBCBeanExistsCommand beanExistsCommand;
   JPMFindEntitiesCommand findEntitiesCommand;
   
   // Constructors --------------------------------------------------
   
   public JDBCFindEntityCommand(JDBCCommandFactory factory)
   {
      beanExistsCommand = factory.createBeanExistsCommand();
      findEntitiesCommand = factory.createFindEntitiesCommand();
   }
   
   // JPMFindEntityCommand implementation ---------------------------
   
   public Object execute(Method finderMethod,
                         Object[] args,
                         EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      if (finderMethod.getName().equals("findByPrimaryKey"))
      {
      
         return findByPrimaryKey(args[0]);
      }
      else
      {
         ArrayList result =
            (ArrayList)findEntitiesCommand.execute(finderMethod, args, ctx);
         
         if (result.size() == 0)
         {
            throw new FinderException("No such entity!");
         } else
         {
            return result.get(0);
         }
      }
   }
   
   // Protected -----------------------------------------------------
   
   protected Object findByPrimaryKey(Object id) throws FinderException
   {
      if (beanExistsCommand.execute(id))
      {
         return id;
      } else
      {
         throw new FinderException("Object with primary key " + id +
                                   " not found in storage");
      }
   }
}
