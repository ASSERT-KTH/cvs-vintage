/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.bmp;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.Collections;

import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;
import org.jboss.logging.Logger;
import org.jboss.util.FinderResults;

/**
 * CMPStoreManager CustomFindByEntitiesCommand.
 * Implements bridge for custom implemented finders in container managed entity beans.
 * These methods are called ejbFindX in the EJB implementation class, where X can be
 * anything. Such methods are called findX in the Home interface. The EJB implementation
 * must return a Collection of primary keys.
 *
 * @see org.jboss.ejb.plugins.cmp.jdbc.JDBCFindEntitiesCommand
 * @author <a href="mailto:michel.anke@wolmail.nl">Michel de Groot</a>
 * @version $Revision: 1.7 $
 */
public class CustomFindByEntitiesCommand implements FindEntitiesCommand
{
   // Attributes ----------------------------------------------------
   protected static Logger log = Logger.create(CustomFindByEntitiesCommand.class);

   /**
    *  method that implements the finder
    */
   protected Method finderImplMethod;

   // Constructors --------------------------------------------------

   /**
    * Constructs a command which can handle multiple entity finders
    * that are BMP implemented.
    * @param finderMethod the EJB finder method implementation
    */
   public CustomFindByEntitiesCommand(Method finderMethod) {
      finderImplMethod = finderMethod;

      log.debug("Finder: Custom finder " + finderMethod.getName());
   }

   // FindEntitiesCommand implementation -------------------------

   public FinderResults execute(Method finderMethod,
         Object[] args,
         EntityEnterpriseContext ctx)
      throws Exception
   {
      try {
         // invoke implementation method on ejb instance
         Object result = finderImplMethod.invoke(ctx.getInstance(), args);

         // if expected return type is Collection, return as is
         // if expected return type is not Collection, wrap result in Collection
         if(!(result instanceof Collection)) {
             result = Collections.singleton(result);
         }
         return new FinderResults((Collection)result, null, null, null);
      } catch (IllegalAccessException e) {
         throw new FinderException("Unable to access finder implementation: " + finderImplMethod.getName());
      } catch (IllegalArgumentException e) {
         throw new FinderException("Illegal arguments for finder implementation: " + finderImplMethod.getName());
      } catch (InvocationTargetException e) {
         Throwable target  = e.getTargetException();
         if(target instanceof Exception) {
             throw (Exception)target;
         }
         throw new FinderException("Unable to initialize finder implementation: " + finderImplMethod.getName());
      }
   }

}
