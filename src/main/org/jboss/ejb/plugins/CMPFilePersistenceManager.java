/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.EntityBean;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

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
public class CMPFilePersistenceManager
   implements EntityPersistenceManager
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EntityContainer con;
   
   Method ejbStore;
   Method ejbLoad;
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;
   File dir;
   Field idField;
    
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
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
      
      String ejbName = con.getMetaData().getEjbName();
      dir = new File(getClass().getResource("/db/"+ejbName+"/db.properties").getFile()).getParentFile();
      idField = con.getBeanClass().getField("id");
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
         createMethod.invoke(ctx.getInstance(), args);
         Object id = idField.get(ctx.getInstance());
         
         // Check exist
         if (getFile(id).exists())
            throw new DuplicateKeyException("Already exists:"+id);
         
         // Set id
         ctx.setId(id);
         
         // Lock instance in cache
         ((EntityContainer)con).getInstanceCache().insert(ctx);
         
         // Create EJBObject
         ctx.setEJBObject(con.getContainerInvoker().getEntityEJBObject(id));

         // Store to file
         storeEntity(ctx);
         
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
      throws RemoteException, FinderException
   {
      if (finderMethod.getName().equals("findByPrimaryKey"))
      {
         if (!getFile(args[0]).exists())
            throw new FinderException(args[0]+" does not exist");
            
         return args[0];
      }
      else
         return null;
   }
     
   public Collection findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException
   {
      if (finderMethod.getName().equals("findAll"))
      {
//         System.out.println("Find all entities");
         
         String[] files = dir.list();
         ArrayList result = new ArrayList();
         for (int i = 0; i < files.length; i++)
            if (files[i].endsWith(".ser"))
            {
//               System.out.println("Found entity");
               result.add(files[i].substring(0,files[i].length()-4));
            }
            
//         System.out.println("Find all entities done");
         return result;
      } else
      {
         return new java.util.ArrayList();
      }
   }

   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Call bean
      try
      {
         ejbActivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Activation failed", e);
      }
   }
   
   public void loadEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      try
      {
         // Read fields
         ObjectInputStream in = new CMPObjectInputStream(new FileInputStream(getFile(ctx.getId())));
         
         Object obj = ctx.getInstance();
         
         Field[] f = obj.getClass().getFields();
         for (int i = 0; i < f.length; i++)
         {
            f[i].set(obj, in.readObject());
         }
         
         in.close();
         
         // Call bean
         ejbLoad.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Load failed", e);
      }
   }
      
   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
//      System.out.println("Store entity");
      
      try
      {
         // Call bean
         ejbStore.invoke(ctx.getInstance(), new Object[0]);

         // Store fields
         ObjectOutputStream out = new CMPObjectOutputStream(new FileOutputStream(getFile(ctx.getId())));
         
         Object obj = ctx.getInstance();
         
         Field[] f = obj.getClass().getFields();
         for (int i = 0; i < f.length; i++)
         {
            out.writeObject(f[i].get(obj));
         }
         
         out.close();
      } catch (Exception e)
      {
         throw new ServerException("Store failed", e);
      }
      
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      // Call bean
      try
      {
         ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Passivation failed", e);
      }
   }
      
   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      try
      {
         // Call ejbRemove
         ejbRemove.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new RemoveException("Could not remove "+ctx.getId());
      }
      
      // Remove file
      if (!getFile(ctx.getId()).delete())
         throw new RemoveException("Could not remove file:"+getFile(ctx.getId()));
//      System.out.println("Removed file for"+ctx.getId());
   }
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected File getFile(Object id)
   {
      return new File(dir, id+".ser");
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
   static class CMPObjectOutputStream
      extends ObjectOutputStream
   {
      public CMPObjectOutputStream(OutputStream out)
         throws IOException
      {
         super(out);
         enableReplaceObject(true);
      }
      
      protected Object replaceObject(Object obj)
         throws IOException
      {
         if (obj instanceof EJBObject)
            return ((EJBObject)obj).getHandle();
            
         return obj;
      }
   }
   
   static class CMPObjectInputStream
      extends ObjectInputStream
   {
      public CMPObjectInputStream(InputStream in)
         throws IOException
      {
         super(in);
         enableResolveObject(true);
      }
      
      protected Object resolveObject(Object obj)
         throws IOException
      {
         if (obj instanceof Handle)
            return ((Handle)obj).getEJBObject();
            
         return obj;
      }
   }
}

