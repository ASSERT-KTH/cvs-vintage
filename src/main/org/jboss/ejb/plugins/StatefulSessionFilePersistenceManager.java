/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.beans.Beans;
import java.beans.beancontext.BeanContextServicesSupport;
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
import java.rmi.NoSuchObjectException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;

import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public class StatefulSessionFilePersistenceManager
   implements StatefulSessionPersistenceManager
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   StatefulSessionContainer con;
   
   Method ejbActivate;
   Method ejbPassivate;
   Method ejbRemove;
   
   File dir;
   
   // Static --------------------------------------------------------
   private static long id = System.currentTimeMillis();
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container c)
   {
      con = (StatefulSessionContainer)c;
   }
   
   public void init()
      throws Exception
   {
      // Find methods
      ejbActivate = SessionBean.class.getMethod("ejbActivate", new Class[0]);
      ejbPassivate = SessionBean.class.getMethod("ejbPassivate", new Class[0]);
      ejbRemove = SessionBean.class.getMethod("ejbRemove", new Class[0]);
      
      String ejbName = con.getMetaData().getEjbName();
      dir = new File(getClass().getResource("/db/sessions/db.properties").getFile()).getParentFile();
      dir = new File(dir, ejbName);
      dir.mkdirs();
   }
   
   public void start()
      throws Exception
   {
   }

   public void stop()
   {
   }
   
   public void destroy()
   {
   }
   
   public void createSession(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws RemoteException, CreateException
   {
      // Get methods
      try
      {
         Method createMethod = con.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
      
         // Call ejbCreate
         createMethod.invoke(ctx.getInstance(), args);
         
/*         // Check exist
         if (getFile(id).exists())
            throw new DuplicateKeyException("Already exists:"+id);
*/

         // Set id
         ctx.setId(nextId());
         
         // Lock instance in cache
         ((StatefulSessionContainer)con).getInstanceCache().insert(ctx);
         
         // Create EJBObject
         ctx.setEJBObject(con.getContainerInvoker().getStatefulSessionEJBObject(ctx.getId()));

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

   public void activateSession(StatefulSessionEnterpriseContext ctx)
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
   
   public void passivateSession(StatefulSessionEnterpriseContext ctx)
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
      
   public void removeSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      // Call bean
      try
      {
         ejbRemove.invoke(ctx.getInstance(), new Object[0]);
      } catch (Exception e)
      {
         throw new ServerException("Remove failed", e);
      }
   }
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected Long nextId()
   {
      return new Long(id++);
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
