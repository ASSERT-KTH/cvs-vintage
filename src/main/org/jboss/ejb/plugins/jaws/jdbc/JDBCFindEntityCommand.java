/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import java.util.Collection;
import java.util.ArrayList;

import java.rmi.RemoteException;

import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;

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
 * @version $Revision: 1.6 $
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
         Collection result =
            (Collection)findEntitiesCommand.execute(finderMethod, args, ctx);
         
         if (result.size() == 0)
         {
            throw new ObjectNotFoundException("No such entity!");
         } else if (result.size() == 1)
         {
            Object [] objects = result.toArray();
            return objects[0];
         } else
         {
            throw new FinderException("More than one entity matches the finder criteria.");
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
         throw new ObjectNotFoundException("Object with primary key " + id +
                                           " not found in storage");
      }
   }
}
