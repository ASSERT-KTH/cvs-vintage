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

import org.jboss.logging.Logger;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.FindEntityCommand;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;

/**
 * JDBCFindEntityCommand finds a single entity, by deligating to 
 * find entities and checking that only entity is returned.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.7 $
 */
public class JDBCFindEntityCommand implements FindEntityCommand {
   private JDBCBeanExistsCommand beanExistsCommand;
   private FindEntitiesCommand findEntitiesCommand;
   private Logger log;
   
   // Constructors --------------------------------------------------
   
   public JDBCFindEntityCommand(JDBCStoreManager manager) {
      beanExistsCommand = manager.getCommandFactory().createBeanExistsCommand();
      findEntitiesCommand = manager.getCommandFactory().createFindEntitiesCommand();
      log = Logger.getLogger(this.getClass().getName() + "." +
            manager.getMetaData().getName());
   }
   
   // FindEntityCommand implementation ---------------------------
   
   public Object execute(Method finderMethod,
                  Object[] args,
                  EntityEnterpriseContext ctx)
            throws Exception {

      if(finderMethod.getName().equals("findByPrimaryKey")) {
         return findByPrimaryKey(args[0]);
      } else {
         Collection result = (Collection)findEntitiesCommand.execute(finderMethod, args, ctx);
         
         if (result.size() == 0) {
            throw new ObjectNotFoundException("No such entity!");
         } else if (result.size() == 1) {
            Object [] objects = result.toArray();
            return objects[0];
         } else {
            throw new FinderException("More than one entity matches the finder criteria.");
         }
      }
   }
   
   // Protected -----------------------------------------------------
   protected Object findByPrimaryKey(Object primaryKey) throws FinderException {
      try {
         if(beanExistsCommand.execute(primaryKey)) {
            return primaryKey;
         } else {
            throw new ObjectNotFoundException("Object with primary key " + 
                  primaryKey + " not found in storage");
         }
      } catch(Exception e) {
         log.error(e);
         throw new FinderException("Error while searching for entity: " + e);
      }
   }
}
