
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.metadata.EntityMetaData;

/**
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @version $Revision: 1.18 $
 * <p><b>20010801 marc fleury:</b>
 * <ul>
 * <li>- insertion in cache upon create in now done in the instance interceptor
 * </ul>
 * <p><b>20011201 Dain Sundstrom:</b>
 * <ul>
 * <li>- added createBeanInstance and initiEntity methods
 * </ul>
 * <p><b>20020525 Dain Sundstrom:</b>
 * <ul>
 * <li>- Replaced FinderResults with Collection
 * <li>- Removed unused method loadEntities
 * </ul>
 */
public class CMPFilePersistenceManager
   implements EntityPersistenceStore
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   EntityContainer con;
   File dir;
   Field idField;

   /**
    *  Optional isModified method used by storeEntity
    */
   Method isModified;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (EntityContainer)c;
   }
   
   public void create()
      throws Exception
   {
      String ejbName = con.getBeanMetaData().getEjbName();
      dir = new File(getClass().getResource("/db/"+ejbName+"/db.properties").getFile()).getParentFile();
      idField = con.getBeanClass().getField("id");

      try
      {
         isModified = con.getBeanClass().getMethod("isModified", new Class[0]);
         if (!isModified.getReturnType().equals(Boolean.TYPE))
            isModified = null; // Has to have "boolean" as return type!
      }
      catch (NoSuchMethodException ignored) {}
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
   
   public Object createBeanClassInstance() throws Exception {
      return con.getBeanClass().newInstance();
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

      EntityMetaData metaData = (EntityMetaData)con.getBeanMetaData();
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

   public Object createEntity(
         Method m, Object[] args, EntityEnterpriseContext ctx)
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

   public Object findEntity(
         Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
      throws FinderException
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
     
   public Collection findEntities(
         Method finderMethod, Object[] args, EntityEnterpriseContext ctx)
   {
      if (finderMethod.getName().equals("findAll"))
      {
         String[] files = dir.list();
         ArrayList result = new ArrayList();
         for (int i = 0; i < files.length; i++)
            if (files[i].endsWith(".ser"))
            {
               result.add(files[i].substring(0,files[i].length()-4));
            }
            
         return result;
      } else
      {
         // we only support find all
         return Collections.EMPTY_LIST;
      }
   }

   public void activateEntity(EntityEnterpriseContext ctx)
   {
      //Nothing to do
   }
   
   public void loadEntity(EntityEnterpriseContext ctx)
   {
      try
      {
         // Read fields
         ObjectInputStream in = new CMPObjectInputStream(
               new FileInputStream(getFile(ctx.getId())));
         
         Object obj = ctx.getInstance();
         
         Field[] f = obj.getClass().getFields();
         for (int i = 0; i < f.length; i++)
         {
            f[i].set(obj, in.readObject());
         }
         
         in.close();
         
      } catch (Exception e)
      {
         throw new EJBException("Load failed", e);
      }
   }
      
   private void storeEntity(Object id, Object obj) 
   {
      try
      {
         // Store fields
         ObjectOutputStream out = new CMPObjectOutputStream(
               new FileOutputStream(getFile(id)));
               
         Field[] f = obj.getClass().getFields();
         for (int i = 0; i < f.length; i++)
         {
            out.writeObject(f[i].get(obj));
         }
         
         out.close();
      } catch (Exception e)
      {
         throw new EJBException("Store failed", e);
      }
   }

   public boolean isModified(EntityEnterpriseContext ctx) throws Exception 
   {
      if(isModified == null)
      {
         return true;
      }

      Object[] args = {};
      Boolean modified = (Boolean) isModified.invoke(ctx.getInstance(), args);
      return modified.booleanValue();
   }
  
   public void storeEntity(EntityEnterpriseContext ctx)
   {
	   storeEntity(ctx.getId(), ctx.getInstance());
   }

   public void passivateEntity(EntityEnterpriseContext ctx)
   {
     // This plugin doesn't do anything specific
	}
      
   public void removeEntity(EntityEnterpriseContext ctx)
      throws RemoveException
   {
      
      // Remove file
      if (!getFile(ctx.getId()).delete())
         throw new RemoveException("Could not remove file:" +
               getFile(ctx.getId()));
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

