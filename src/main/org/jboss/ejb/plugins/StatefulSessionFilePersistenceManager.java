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
import java.lang.reflect.Modifier;
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
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.logging.Logger;


/**
 *  StatefulSessionFilePersistenceManager
 *
 *  This class is one of the passivating plugins for jBoss.  
 *  It is fairly simple and can work from the file system from wich jBoss is operating
 *      
 *  @see <related>
 *  @author Rickard �berg (rickard.oberg@telkel.com)
 *  @author <a href="marc.fleury@telkel.com">Marc Fleury</a>
 *  @version $Revision: 1.11 $
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
        throws Exception {
         
        // Find methods
        ejbActivate = SessionBean.class.getMethod("ejbActivate", new Class[0]);
        
       ejbPassivate = SessionBean.class.getMethod("ejbPassivate", new Class[0]);
        
       ejbRemove = SessionBean.class.getMethod("ejbRemove", new Class[0]);
      
        // Initialize the dataStore
       String ejbName = con.getBeanMetaData().getEjbName();
      
       // Base dir
       File databaseDir = new File(getClass().getResource("/db.properties").getFile()).getParentFile();
      
       File database = new File(databaseDir, "sessions");
      
       dir = new File(database, ejbName);
         
       dir.mkdirs();
       
       Logger.debug("Storing sessions for "+ejbName+" in:"+dir);
      
       // Clear dir of old files
       File[] sessions = dir.listFiles();
       for (int i = 0; i < sessions.length; i++)
       {
         sessions[i].delete();
       }
       Logger.debug(sessions.length + " old sessions removed");
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
      throws Exception
   {
      // Get methods
      try
      {
         Method createMethod = con.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
      
         // Call ejbCreate
         createMethod.invoke(ctx.getInstance(), args);
         
      } catch (IllegalAccessException e)
       {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
       } catch (InvocationTargetException ite) 
       {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
      	   // Rethrow exception
      	   throw (EJBException)e;
         } else if (e instanceof RuntimeException)
         {
      	   // Wrap runtime exceptions
      	   throw new EJBException((Exception)e);
         } else if (e instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception)e;
         } else
         {
            throw (Error)e;
         }
       }
      
      // Set id
      ctx.setId(nextId());

      // Insert in cache
      ((StatefulSessionContainer)con).getInstanceCache().insert(ctx);

      // Create EJBObject
      ctx.setEJBObject(con.getContainerInvoker().getStatefulSessionEJBObject(ctx.getId()));
   }

   public void activateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException
   {
       try
       {
         
            ObjectInputStream in;
            
            
            // Load state
            in = new SessionObjectInputStream(ctx, new FileInputStream(new File(dir, ctx.getId()+".ser")));
         Field[] fields = ctx.getInstance().getClass().getFields();
         
         for (int i = 0; i < fields.length; i++)
          if (!Modifier.isTransient(fields[i].getModifiers()))
              fields[i].set(ctx.getInstance(), in.readObject());
         
          // Call bean
         ejbActivate.invoke(ctx.getInstance(), new Object[0]);
      } catch (ClassNotFoundException e)
      {
        throw new ServerException("Could not activate", e); 
      } catch (IOException e)
      {
        throw new ServerException("Could not activate", e); 
      } catch (IllegalAccessException e)
       {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
       } catch (InvocationTargetException ite) 
       {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
      	   // Rethrow exception
      	   throw (EJBException)e;
         } else if (e instanceof RuntimeException)
         {
      	   // Wrap runtime exceptions
      	   throw new EJBException((Exception)e);
         } else if (e instanceof RemoteException)
         {
            // Remote, Create, or custom app. exception
            throw (RemoteException)e;
         } else
         {
            throw (Error)e;
         }
       }
   }
   
   public void passivateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException
   {
      try
      {
          // Call bean
         ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
       
          // Store state
          ObjectOutputStream out = new SessionObjectOutputStream(new FileOutputStream(new File(dir, ctx.getId()+".ser")));
         
         Field[] fields = ctx.getInstance().getClass().getFields();
         
         for (int i = 0; i < fields.length; i++)
          if (!Modifier.isTransient(fields[i].getModifiers()))
              out.writeObject(fields[i].get(ctx.getInstance()));
         
         out.close();   
       } catch (IOException e)
       {
         throw new ServerException("Could not passivate", e);
       }catch (IllegalAccessException e)
        {
          // Throw this as a bean exception...(?)
          throw new EJBException(e);
        } catch (InvocationTargetException ite) 
        {
          Throwable e = ite.getTargetException();
          if (e instanceof EJBException)
          {
       	   // Rethrow exception
       	   throw (EJBException)e;
          } else if (e instanceof RuntimeException)
          {
       	   // Wrap runtime exceptions
       	   throw new EJBException((Exception)e);
          } else if (e instanceof RemoteException)
          {
             // Remote, Create, or custom app. exception
             throw (RemoteException)e;
          } else
          {
             throw (Error)e;
          }
        }
   }
      
   public void removeSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      // Call bean
      try
      {
         ejbRemove.invoke(ctx.getInstance(), new Object[0]);
      } catch (IllegalAccessException e)
       {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
       } catch (InvocationTargetException ite) 
       {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
      	   // Rethrow exception
      	   throw (EJBException)e;
         } else if (e instanceof RuntimeException)
         {
      	   // Wrap runtime exceptions
      	   throw new EJBException((Exception)e);
         } else if (e instanceof RemoveException)
         {
            throw (RemoveException)e;
         } else if (e instanceof RemoteException)
         {
            throw (RemoteException)e;
         } else
         {
            throw (Error)e;
         }
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
}
