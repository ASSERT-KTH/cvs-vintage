
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
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
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.util.FinderResults;

/**
*	<description> 
*      
*   @see <related>
*   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @version $Revision: 1.13 $
*   <p><b>20010801 marc fleury:</b>
*   <ul>
*   <li>- insertion in cache upon create in now done in the instance interceptor
*   </ul>
*/
public class CMPFilePersistenceManager
   implements EntityPersistenceStore
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EntityContainer con;
   
   /* The Methods are taken care of by CMPPersistenceManager
   Method ejbStore;
   Method ejbLoad;
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;
   */
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
	   // The methods are now taken care of by CMPPersistenceManager
	  /*
      ejbStore = EntityBean.class.getMethod("ejbStore", new Class[0]);
      ejbLoad = EntityBean.class.getMethod("ejbLoad", new Class[0]);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", new Class[0]);
	  */
       
      String ejbName = con.getBeanMetaData().getEjbName();
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
   
   public Object createEntity(Method m, Object[] args, EntityEnterpriseContext ctx)
      throws Exception
	{                      
		try { 

			Object id = idField.get(ctx.getInstance());
			
			// Check exist
			if (getFile(id).exists())
				throw new DuplicateKeyException("Already exists:"+id);
			
			// Store to file
			storeEntity(id, ctx.getInstance());
			
			return id;
		} 
		catch (IllegalAccessException e)
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
     
   public FinderResults findEntities(Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws RemoteException
   {
      if (finderMethod.getName().equals("findAll"))
      {
//DEBUG         Logger.debug("Find all entities");
         
         String[] files = dir.list();
         ArrayList result = new ArrayList();
         for (int i = 0; i < files.length; i++)
            if (files[i].endsWith(".ser"))
            {
//DEBUG               Logger.debug("Found entity");
               result.add(files[i].substring(0,files[i].length()-4));
            }
            
//         Logger.debug("Find all entities done");
         return new FinderResults(result,null,null,null);
      } else
      {
         return new FinderResults(new java.util.ArrayList(),null,null,null);
      }
   }

   public void activateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
      //Nothing to do
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
         
      } catch (Exception e)
      {
         throw new ServerException("Load failed", e);
      }
   }
      
   public void loadEntities(FinderResults keys) {
      //this is a no op for this persistence store.
   }
   
   private void storeEntity(Object id, Object obj) 
   	throws RemoteException {
	  
      try
      {
         // Store fields
         ObjectOutputStream out = new CMPObjectOutputStream(new FileOutputStream(getFile(id)));
               
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
   
   public void storeEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
//      Logger.debug("Store entity");
     
	   storeEntity(ctx.getId(), ctx.getInstance());
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
      throws RemoteException
   {
     // This plugin doesn't do anything specific
	}
      
   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      
      // Remove file
      if (!getFile(ctx.getId()).delete())
         throw new RemoveException("Could not remove file:"+getFile(ctx.getId()));
//      Logger.debug("Removed file for"+ctx.getId());
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

