/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.lang.reflect.Method;

import java.rmi.RemoteException;


import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import org.apache.log4j.Category;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.jaws.jdbc.JDBCCommandFactory;

import org.jboss.util.FinderResults;

/**
 *   Just Another Web Store - an O/R mapper
 *
 * @see org.jboss.ejb.EntityPersistenceStore
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.33 $
 *
 *   <p><b>Revisions:</b>
 *
 *   <p><b>20010812 vincent.harcq@hubmethods.com:</b>
 *   <ul>
 *   <li> Get Rid of debug flag, use log4j instead
 *   </ul>
 *
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
   JPMLoadEntitiesCommand loadEntitiesCommand;
   JPMStoreEntityCommand storeEntityCommand;

   JPMActivateEntityCommand activateEntityCommand;
   JPMPassivateEntityCommand passivateEntityCommand;

   Category log = Category.getInstance(this.getClass().getName());

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
      commandFactory = new JDBCCommandFactory(container);

      initCommand = commandFactory.createInitCommand();
      startCommand = commandFactory.createStartCommand();
      stopCommand = commandFactory.createStopCommand();
      destroyCommand = commandFactory.createDestroyCommand();

      findEntityCommand = commandFactory.createFindEntityCommand();
      findEntitiesCommand = commandFactory.createFindEntitiesCommand();
      createEntityCommand = commandFactory.createCreateEntityCommand();
      removeEntityCommand = commandFactory.createRemoveEntityCommand();
      loadEntityCommand = commandFactory.createLoadEntityCommand();
      loadEntitiesCommand = commandFactory.createLoadEntitiesCommand();
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
      throws Exception
   {
      return findEntityCommand.execute(finderMethod, args, ctx);
   }

   public FinderResults findEntities(Method finderMethod,
                                  Object[] args,
                                  EntityEnterpriseContext ctx)
      throws Exception
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
   
   public void loadEntities(FinderResults keys) 
      throws RemoteException
   {
      loadEntitiesCommand.execute(keys);
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

