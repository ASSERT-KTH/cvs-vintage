/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jaws;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import java.rmi.RemoteException;

import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.RemoveException;

import org.apache.log4j.Category;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;

import org.jboss.ejb.plugins.jaws.jdbc.JDBCCommandFactory;

import org.jboss.metadata.EntityMetaData;

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
 * @version $Revision: 1.34 $
 *
 *   <p><b>Revisions:</b>
 *
 *   <p><b>20010812 vincent.harcq@hubmethods.com:</b>
 *   <ul>
 *   <li> Get Rid of debug flag, use log4j instead
 *   </ul>
 *   <p><b>20011201 Dain Sundstrom:</b>
 *   <ul>
 *   <li> Added createBeanInstance and initEntity methods
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

   public Object createBeanClassInstance() throws Exception {
      return container.getBeanClass().newInstance();
   }

   /**
    * Reset all attributes to default value
    *
    * The EJB 1.1 specification is not entirely clear about this,
    * the EJB 2.0 spec is, see page 169.
    * Robustness is more important than raw speed for most server
    * applications, and not resetting atrribute values result in
    * *very* weird errors (old states re-appear in different instances and the
    * developer thinks he's on drugs).
    */
   public void initEntity(EntityEnterpriseContext ctx)
   {
      // first get cmp metadata of this entity
      Object instance = ctx.getInstance();
      Class ejbClass = instance.getClass();
      Field cmpField;
      Class cmpFieldType;

      EntityMetaData metaData = (EntityMetaData)container.getBeanMetaData();
      Iterator i= metaData.getCMPFields();

      while(i.hasNext())
      {
         try
         {
            // get the field declaration
            try
            {
               cmpField = ejbClass.getField((String)i.next());
               cmpFieldType = cmpField.getType();
               // find the type of the field and reset it
               // to the default value
               if (cmpFieldType.equals(boolean.class))
               {
                  cmpField.setBoolean(instance,false);
               }
               else if (cmpFieldType.equals(byte.class))
               {
                  cmpField.setByte(instance,(byte)0);
               }
               else if (cmpFieldType.equals(int.class))
               {
                  cmpField.setInt(instance,0);
               }
               else if (cmpFieldType.equals(long.class))
               {
                  cmpField.setLong(instance,0L);
               }
               else if (cmpFieldType.equals(short.class))
               {
                  cmpField.setShort(instance,(short)0);
               }
               else if (cmpFieldType.equals(char.class))
               {
                  cmpField.setChar(instance,'\u0000');
               }
               else if (cmpFieldType.equals(double.class))
               {
                  cmpField.setDouble(instance,0d);
               }
               else if (cmpFieldType.equals(float.class))
               {
                  cmpField.setFloat(instance,0f);
               }
               else
               {
                  cmpField.set(instance,null);
               }
            }
            catch (NoSuchFieldException e)
            {
               // will be here with dependant value object's private attributes
               // should not be a problem
            }
         }
         catch (Exception e)
         {
            throw new EJBException(e);
         }
      }
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

