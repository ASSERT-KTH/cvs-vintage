/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.IOException;
import java.util.ArrayList;

import javax.ejb.EJBException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.FinderResults;

/**
 * EntityPersistenceStore implementation storing values in-memory
 * for very efficient access.
 *
 * @see org.jboss.ejb.EntityPersistenceStore
 * @see org.jboss.ejb.plugins.CMPFilePersistenceManager
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.2 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>28.12.2001 - Sacha Labourey:</b>
 * <ul>
 * <li> First implementation highly based on CMPFilePersistenceManager</li>
 * </ul>
 */

public class CMPInMemoryPersistenceManager implements org.jboss.ejb.EntityPersistenceStore
{
   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   protected org.jboss.ejb.EntityContainer con = null;   
   protected java.util.HashMap beans = null;   
   protected Field idField = null;
   
   /**
    *  Optional isModified method used by storeEntity
    */
   protected Method isModified = null;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public CMPInMemoryPersistenceManager ()
   {
   }
   
   /**
    * create the service, do expensive operations etc
    */
   public void create () throws Exception
   {
      this.beans = new java.util.HashMap (1000);
      
      String ejbName = con.getBeanMetaData ().getEjbName ();
      
      idField = con.getBeanClass ().getField ("id");
      
      try
      {
         isModified = con.getBeanClass ().getMethod ("isModified", new Class[0]);
         if (!isModified.getReturnType ().equals (Boolean.TYPE))
            isModified = null; // Has to have "boolean" as return type!
      }
      catch (NoSuchMethodException ignored)
      {}
   }
   
   /**
    * start the service, create is already called
    */
   public void start () throws Exception
   {
   }
   
   /**
    * This callback is set by the container so that the plugin may access it
    *
    * @param con    The container using this plugin.
    */
   public void setContainer (org.jboss.ejb.Container con)
   {
      this.con = (org.jboss.ejb.EntityContainer)con;
   }
   
   /**
    * stop the service
    */
   public void stop ()
   {
      this.beans.clear ();
   }
   
   
   /**
    * destroy the service, tear down
    */
   public void destroy ()
   {
   }
   
   // Public --------------------------------------------------------
   
   // EntityPersistenceStore implementation ----------------------------------------------
   
   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    *
    * @return   the new instance
    *
    * @throws Exception
    */
   public Object createBeanClassInstance () throws Exception
   {
      return con.getBeanClass ().newInstance ();
   }
   
   /**
    * Initializes the instance context.
    *
    * <p>This method is called before createEntity, and should
    *   reset the value of all cmpFields to 0 or null.
    *
    * @param ctx
    *
    * @throws RemoteException
    */
   public void initEntity (EntityEnterpriseContext ctx)
   {
      // first get cmp metadata of this entity
      Object instance = ctx.getInstance ();
      Class ejbClass = instance.getClass ();
      Field cmpField;
      Class cmpFieldType;
      
      org.jboss.metadata.EntityMetaData metaData = (org.jboss.metadata.EntityMetaData)con.getBeanMetaData ();
      java.util.Iterator i= metaData.getCMPFields ();
      
      while(i.hasNext ())
      {
         try
         {
            // get the field declaration
            try
            {
               cmpField = ejbClass.getField ((String)i.next ());
               cmpFieldType = cmpField.getType ();
               // find the type of the field and reset it
               // to the default value
               if (cmpFieldType.equals (boolean.class))
               {
                  cmpField.setBoolean (instance,false);
               }
               else if (cmpFieldType.equals (byte.class))
               {
                  cmpField.setByte (instance,(byte)0);
               }
               else if (cmpFieldType.equals (int.class))
               {
                  cmpField.setInt (instance,0);
               }
               else if (cmpFieldType.equals (long.class))
               {
                  cmpField.setLong (instance,0L);
               }
               else if (cmpFieldType.equals (short.class))
               {
                  cmpField.setShort (instance,(short)0);
               }
               else if (cmpFieldType.equals (char.class))
               {
                  cmpField.setChar (instance,'\u0000');
               }
               else if (cmpFieldType.equals (double.class))
               {
                  cmpField.setDouble (instance,0d);
               }
               else if (cmpFieldType.equals (float.class))
               {
                  cmpField.setFloat (instance,0f);
               }
               else
               {
                  cmpField.set (instance,null);
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
            throw new EJBException (e);
         }
      }
   }
   
   /**
    * This method is called whenever an entity is to be created.
    * The persistence manager is responsible for handling the results properly
    * wrt the persistent store.
    *
    * @param m           the create method in the home interface that was
    *                   called
    * @param args        any create parameters
    * @param instance    the instance being used for this create call
    * @return            The primary key computed by CMP PM or null for BMP
    *
    * @throws Exception
    */
   public Object createEntity (Method m, Object[] args, EntityEnterpriseContext ctx) throws Exception
   {
      try
      {
         
         Object id = idField.get (ctx.getInstance ());
         
         // Check exist
         if (this.beans.containsKey (id))
            throw new javax.ejb.DuplicateKeyException ("Already exists:"+id);
         
         // Store to file
         storeEntity (id, ctx.getInstance ());
         
         return id;
      }
      catch (IllegalAccessException e)
      {
         throw new javax.ejb.CreateException ("Could not create entity:"+e);
      }
   }
   
   /**
    * This method is called when single entities are to be found. The
    * persistence manager must find out whether the wanted instance is
    * available in the persistence store, if so it returns the primary key of
    * the object.
    *
    * @param finderMethod    the find method in the home interface that was
    *                       called
    * @param args            any finder parameters
    * @param instance        the instance to use for the finder call
    * @return                a primary key representing the found entity
    *
    * @throws RemoteException    thrown if some system exception occurs
    * @throws FinderException    thrown if some heuristic problem occurs
    */
   public Object findEntity (Method finderMethod, Object[] args, EntityEnterpriseContext instance) throws Exception
   {
      if (finderMethod.getName ().equals ("findByPrimaryKey"))
      {
         if (!this.beans.containsKey (args[0]))
            throw new javax.ejb.FinderException (args[0]+" does not exist");
         
         return args[0];
      }
      else
         return null;
   }
   
   /**
    * This method is called when collections of entities are to be found. The
    * persistence manager must find out whether the wanted instances are
    * available in the persistence store, and if so  it must return a
    * collection of primaryKeys.
    *
    * @param finderMethod    the find method in the home interface that was
    *                       called
    * @param args            any finder parameters
    * @param instance        the instance to use for the finder call
    * @return                an primary key collection representing the found
    *                       entities
    *
    * @throws RemoteException    thrown if some system exception occurs
    * @throws FinderException    thrown if some heuristic problem occurs
    */
   public FinderResults findEntities (Method finderMethod, Object[] args, EntityEnterpriseContext instance) throws Exception
   {
      if (finderMethod.getName ().equals ("findAll"))
      {
         ArrayList result = new ArrayList (this.beans.keySet ());
         
         return new FinderResults (result,null,null,null);
      } else
      {
         return new FinderResults (new ArrayList (),null,null,null);
      }
   }
   
   /**
    * This method is called when an entity shall be activated.
    *
    * <p>With the PersistenceManager factorization most EJB calls should not
    *   exists However this calls permits us to introduce optimizations in
    *   the persistence store.  Particularly the context has a
    *   "PersistenceContext" that a PersistenceStore can use (JAWS does for
    *   smart updates) and this is as good a callback as any other to set it
    *   up.
    *
    * @param instance    the instance to use for the activation
    *
    * @throws RemoteException    thrown if some system exception occurs
    */
   public void activateEntity (EntityEnterpriseContext instance)
   {
   }
   
   /**
    * This method is called whenever an entity shall be load from the
    * underlying storage. The persistence manager must load the state from
    * the underlying storage and then call ejbLoad on the supplied instance.
    *
    * @param instance    the instance to synchronize
    *
    * @throws RemoteException    thrown if some system exception occurs
    */
   public void loadEntity (EntityEnterpriseContext ctx)
   {
      try
      {
         // Read fields
         
         java.io.ObjectInputStream in = new org.jboss.ejb.plugins.CMPInMemoryPersistenceManager.CMPObjectInputStream (
         new java.io.ByteArrayInputStream ((byte[])this.beans.get (ctx.getId ())));
         
         Object obj = ctx.getInstance ();
         
         Field[] f = obj.getClass ().getFields ();
         for (int i = 0; i < f.length; i++)
         {
            f[i].set (obj, in.readObject ());
         }
         
         in.close ();
         
      } catch (Exception e)
      {
         throw new EJBException ("Load failed", e);
      }
   }
   
   /**
    * This method is used to determine if an entity should be stored.
    *
    * @param instance    the instance to check
    * @return true, if the entity has been modified
    * @throws Exception    thrown if some system exception occurs
    */
   public boolean isModified (EntityEnterpriseContext ctx) throws Exception
   {
      if(isModified == null)
      {
         return true;
      }
      
      Object[] args =
      {};
      Boolean modified = (Boolean) isModified.invoke (ctx.getInstance (), args);
      return modified.booleanValue ();
   }
   
   /**
    * This method is called whenever an entity shall be stored to the
    * underlying storage. The persistence manager must call ejbStore on the
    * supplied instance and then store the state to the underlying storage.
    *B
    * @param instance    the instance to synchronize
    *
    * @throws RemoteException    thrown if some system exception occurs
    */
   public void storeEntity (EntityEnterpriseContext ctx)
   {
      storeEntity (ctx.getId (), ctx.getInstance ());
   }
   
   /**
    * This method is called when an entity shall be passivate. The persistence
    * manager must call the ejbPassivate method on the instance.
    *
    * <p>See the activate discussion for the reason for exposing EJB callback
    *   calls to the store.
    *
    * @param instance    the instance to passivate
    *
    * @throws RemoteException    thrown if some system exception occurs
    */
   public void passivateEntity (EntityEnterpriseContext instance)
   {
      // This plugin doesn't do anything specific
   }
   
   /**
    * This method is called when an entity shall be removed from the
    * underlying storage. The persistence manager must call ejbRemove on the
    * instance and then remove its state from the underlying storage.
    *
    * @param instance    the instance to remove
    *
    * @throws RemoteException    thrown if some system exception occurs
    * @throws RemoveException    thrown if the instance could not be removed
    */
   public void removeEntity (EntityEnterpriseContext ctx) throws javax.ejb.RemoveException
   {
      if (this.beans.remove (ctx.getId ()) == null)
         throw new javax.ejb.RemoveException ("Could not remove bean:" +
         ctx.getId ());
   }
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   protected void storeEntity (Object id, Object obj)
   {
      try
      {
         // Store fields
         java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream ();
         java.io.ObjectOutputStream out = new org.jboss.ejb.plugins.CMPInMemoryPersistenceManager.CMPObjectOutputStream (baos);
         
         Field[] f = obj.getClass ().getFields ();
         for (int i = 0; i < f.length; i++)
         {
            out.writeObject (f[i].get (obj));
         }
         
         out.close ();
         
         this.beans.put (id, baos.toByteArray ());
         
      } catch (Exception e)
      {
         throw new EJBException ("Store failed", e);
      }
   }
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
   static class CMPObjectOutputStream extends java.io.ObjectOutputStream
   {
      public CMPObjectOutputStream (java.io.OutputStream out) throws IOException
      {
         super (out);
         enableReplaceObject (true);
      }
      
      protected Object replaceObject (Object obj)
      throws IOException
      {
         if (obj instanceof javax.ejb.EJBObject)
            return ((javax.ejb.EJBObject)obj).getHandle ();
         
         return obj;
      }
   }
   
   static class CMPObjectInputStream extends java.io.ObjectInputStream
   {
      public CMPObjectInputStream (java.io.InputStream in) throws IOException
      {
         super (in);
         enableResolveObject (true);
      }
      
      protected Object resolveObject (Object obj)
      throws IOException
      {
         if (obj instanceof javax.ejb.Handle)
            return ((javax.ejb.Handle)obj).getEJBObject ();
         
         return obj;
      }
   }
   
}
