/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;

import javax.ejb.EntityBean;
import javax.ejb.CreateException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.EntityEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class BMPPersistenceManager
   implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EntityContainer con;
   
   Method ejbLoad;
   Method ejbStore;
   Method ejbActivate;
   Method ejbPassivate;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (EntityContainer)c;
   }
   
   public void init()
      throws Exception
   {
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
   }
   
   public void start()
   {
   }
   
   public void stop()
   {
   }

   public void destroy()
   {
   }
   
   public void createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException, CreateException
   {
      // Get methods
      try
      {
         Method createMethod = con.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
         Method postCreateMethod = con.getBeanClass().getMethod("ejbPostCreate", m.getParameterTypes());
      
         // Call ejbCreate
         Object id = createMethod.invoke(ctx.getInstance(), args);
         ctx.setId(id);
         
         // Lock instance in cache
         con.getInstanceCache().insert(ctx);
         
         // Create EJBObject
         ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(id));

         postCreateMethod.invoke(ctx.getInstance(), args);
      } catch (InvocationTargetException e)
      {
         throw new CreateException("Create failed:"+e);
      } catch (NoSuchMethodException e)
      {
         throw new CreateException("Create methods not found:"+e);
      } catch (IllegalAccessException e)
      {
         throw new CreateException("Could not create entity:"+e);
      }
   }

   public Object findEntity(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException
   {
      return null;
   }
     
   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException
   {
      return new java.util.ArrayList();
   }

   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
   }
   
   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
   }
      
   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
//      System.out.println("Store entity");
      try
      {
         ejbStore.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Store failed", e);
      }
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
   }
      
   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
   }
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

