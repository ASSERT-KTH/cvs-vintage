/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;


import org.jboss.logging.Logger;
import org.jboss.util.FinderResults;

/**
 * CMPStoreManager is a classic facade pattern [Gamma et. al, 1995].
 * Currently the only client of this facade is CMPPersistenceManager.
 * This is an abstract class, which is designed to allow plugable persistence
 * storage. Currently there is only one implementation JDBCStoreManager.
 *
 * CMPStoreManager deligates messages from CMPPersistenceManager to a 
 * command object, which implements the command pattern [Gamma et. al, 1995].
 * There are 2 basic types of messages: life cycle and entity.
 * Life cycle messages are init, start, stop, and destroy. Entity messages
 * are the classic EntityBean messages (e.g., activate, passivate...). 
 *
 * Dependency:
 *      In general, this package depends as little as possible on other
 * packages. Specifically it depends on container information from 
 * org.jboss.ejb, such as EntityEnterpriseContext.  Additionally, 
 * implementations of this class, will depend on org.jboss.metadata to aquire 
 * information about the entity.
 *
 * Life-cycle:
 *      Tied to the life-cycle of the entity container.
 *
 * Multiplicity:   
 *      One per cmp entity bean. This could be less if another implementaion of 
 * EntityPersistenceStore is created and thoes beans use the implementation.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @see org.jboss.ejb.EntityPersistenceStore
 * @version $Revision: 1.9 $
 */                            
public abstract class CMPStoreManager 
   implements EntityPersistenceStore
{
   // Attributes ----------------------------------------------------

   protected EntityContainer container;
   protected Logger log;

   protected CommandFactory commandFactory;

   protected InitCommand initCommand;
   protected StartCommand startCommand;
   protected StopCommand stopCommand;
   protected DestroyCommand destroyCommand;

   protected InitEntityCommand initEntityCommand;
   protected FindEntityCommand findEntityCommand;
   protected FindEntitiesCommand findEntitiesCommand;
   protected CreateEntityCommand createEntityCommand;
   protected RemoveEntityCommand removeEntityCommand;
   protected LoadEntityCommand loadEntityCommand;
   protected LoadEntitiesCommand loadEntitiesCommand;
   protected StoreEntityCommand storeEntityCommand;

   protected ActivateEntityCommand activateEntityCommand;
   protected PassivateEntityCommand passivateEntityCommand;
   
   // EntityPersistenceStore implementation -------------------------

   public EntityContainer getContainer() {
      return container;
   }

   public void setContainer(Container container) {
      this.container = (EntityContainer)container;
      this.log = Logger.getLogger(
            this.getClass().getName() + 
            "." + 
            container.getBeanMetaData().getEjbName());
   }
   
   protected abstract CommandFactory createCommandFactory()  throws Exception ;
   
   // Container Life cycle commands -------------------------

   public void init() throws Exception {
      log.debug("Initializing CMP plugin for " +
                container.getBeanMetaData().getEjbName());

      // Set up Commands
      commandFactory = createCommandFactory();

      initCommand = commandFactory.createInitCommand();
      startCommand = commandFactory.createStartCommand();
      stopCommand = commandFactory.createStopCommand();
      destroyCommand = commandFactory.createDestroyCommand();

      initEntityCommand = commandFactory.createInitEntityCommand();
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
      // On deploy errors, sometimes CMPStoreManager was never initialized!
      if(stopCommand != null) { 
         stopCommand.execute();
      }
   }

   public void destroy()
   {
      // On deploy errors, sometimes CMPStoreManager was never initialized!
      if(destroyCommand != null) {
         destroyCommand.execute();
      }
   }

   // EJB Commands -------------------------

   public void initEntity(EntityEnterpriseContext ctx)
   {
      initEntityCommand.execute(ctx);
   }

   public Object createEntity(Method m,
                            Object[] args,
                            EntityEnterpriseContext ctx)
      throws CreateException
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
   {
      activateEntityCommand.execute(ctx);
   }

   public void loadEntity(EntityEnterpriseContext ctx)
   {
      loadEntityCommand.execute(ctx);
   }
   
   public void loadEntities(FinderResults keys) 
   {
      loadEntitiesCommand.execute(keys);
   }

   public void storeEntity(EntityEnterpriseContext ctx)
   {
      storeEntityCommand.execute(ctx);
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
   {
      passivateEntityCommand.execute(ctx);
   }

   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoveException
   {
      removeEntityCommand.execute(ctx);
   }

   // Inner classes -------------------------------------------------

   // This class supports tuned updates and read-only entities

   public static class PersistenceContext {
      public Map fieldState = new HashMap();
   }
}

