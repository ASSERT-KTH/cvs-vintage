/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.lang.reflect.Method;

import java.rmi.RemoteException;

import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.jaws.jdbc.JDBCCommandFactory;

import org.jboss.logging.Log;

/**
 *   Just Another Web Store - an O/R mapper
 *
 * @see org.jboss.ejb.EntityPersistenceStore
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.24 $
 */
public class JAWSPersistenceManager
   implements EntityPersistenceStore
{
   // Attributes ----------------------------------------------------

   EntityContainer container;

   JPMCommandFactory commandFactory;

   JPMInitCommand initCommand;
   JPMStartCommand startCommand;
   JPMStopCommand stopCommand;
   JPMDestroyCommand destroyCommand;

   JPMFindEntityCommand findEntityCommand;
   JPMFindEntitiesCommand findEntitiesCommand;
   JPMCreateEntityCommand createEntityCommand;
   JPMRemoveEntityCommand removeEntityCommand;
   JPMLoadEntityCommand loadEntityCommand;
   JPMStoreEntityCommand storeEntityCommand;

   JPMActivateEntityCommand activateEntityCommand;
   JPMPassivateEntityCommand passivateEntityCommand;

   Log log = new Log("JAWS");

   // EntityPersistenceStore implementation -------------------------

   public void setContainer(Container c)
   {
      container = (EntityContainer)c;
   }

   public void init() throws Exception
   {
      log.debug("Initializing JAWS plugin for " +
                container.getBeanMetaData().getEjbName());

      // Set up Commands
      commandFactory = new JDBCCommandFactory(container, log);

      initCommand = commandFactory.createInitCommand();
      startCommand = commandFactory.createStartCommand();
      stopCommand = commandFactory.createStopCommand();
      destroyCommand = commandFactory.createDestroyCommand();

      findEntityCommand = commandFactory.createFindEntityCommand();
      findEntitiesCommand = commandFactory.createFindEntitiesCommand();
      createEntityCommand = commandFactory.createCreateEntityCommand();
      removeEntityCommand = commandFactory.createRemoveEntityCommand();
      loadEntityCommand = commandFactory.createLoadEntityCommand();
      storeEntityCommand = commandFactory.createStoreEntityCommand();

      activateEntityCommand = commandFactory.createActivateEntityCommand();
      passivateEntityCommand = commandFactory.createPassivateEntityCommand();

      // Execute the init Command

      initCommand.execute();
   }

   public void start() throws Exception
   {
      startCommand.execute();
   }

   public void stop()
   {
      if(stopCommand != null) // On deploy errors, sometimes JAWS was never initialized!
         stopCommand.execute();
   }

   public void destroy()
   {
      if(destroyCommand != null) // On deploy errors, sometimes JAWS was never initialized!
         destroyCommand.execute();
   }

   public Object createEntity(Method m,
                            Object[] args,
                            EntityEnterpriseContext ctx)
      throws RemoteException, CreateException
   {
      return createEntityCommand.execute(m, args, ctx);
   }

   public Object findEntity(Method finderMethod,
                            Object[] args,
                            EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      return findEntityCommand.execute(finderMethod, args, ctx);
   }

   public Collection findEntities(Method finderMethod,
                                  Object[] args,
                                  EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      return findEntitiesCommand.execute(finderMethod, args, ctx);
   }

   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      activateEntityCommand.execute(ctx);
   }

   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      loadEntityCommand.execute(ctx);
   }

   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      storeEntityCommand.execute(ctx);
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      passivateEntityCommand.execute(ctx);
   }

   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      removeEntityCommand.execute(ctx);
   }

   // Inner classes -------------------------------------------------

   // This class supports tuned updates and read-only entities

   public static class PersistenceContext
   {
      public Object[] state;
      public long lastRead = -1;
   }
}
