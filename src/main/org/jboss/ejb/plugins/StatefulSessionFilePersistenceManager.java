/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.ejb.SessionBean;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

import org.jboss.system.server.ServerConfigLocator;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.util.jmx.MBeanServerLocator;

import org.jboss.util.id.UID;

/**
 * This class is one of the passivating plugins for JBoss.
 *
 * <p>
 * It is fairly simple and can work from the file system from wich JBoss is operating.
 * Passivates beans into the system data directory under the 'sessions' sub-directory.
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 * 
 * @version <tt>$Revision: 1.37 $</tt>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class StatefulSessionFilePersistenceManager
   extends ServiceMBeanSupport
   implements StatefulSessionPersistenceManager, StatefulSessionFilePersistenceManagerMBean
{
   /** A refernece to the {@link SessionBean#ejbActivate} method. */
   private static final Method EJB_ACTIVATE_METHOD;

   /** A refernece to the {@link SessionBean#ejbPassivate} method. */
   private static final Method EJB_PASSIVATE_METHOD;
   
   /** A refernece to the {@link SessionBean#ejbRemove} method. */
   private static final Method EJB_REMOVE_METHOD;

   /** Setup method references. */
   static {
      try {
         EJB_ACTIVATE_METHOD = SessionBean.class.getMethod("ejbActivate", new Class[0]);
         EJB_PASSIVATE_METHOD = SessionBean.class.getMethod("ejbPassivate", new Class[0]);
         EJB_REMOVE_METHOD = SessionBean.class.getMethod("ejbRemove", new Class[0]);
      }
      catch (Exception e) {
         throw new org.jboss.util.NestedError("System failure; Missing required interface classes", e);
      }
   }

   /** Our container. */
   private StatefulSessionContainer con;

   /** The base directory where sessions state files are stored for our container. */
   private File dataDir;
    
   public void setContainer(Container c)
   {
      con = (StatefulSessionContainer)c;
   }

   protected void createService() throws Exception
   {
      boolean debug = log.isDebugEnabled();

      // Initialize the dataStore
      String ejbName = con.getBeanMetaData().getEjbName();

      // Get the system data directory
      File systemDataDir = ServerConfigLocator.locate().getServerDataDir();

      // Gte a handle on the sysdata/sessions/ejbname directory
      dataDir = new File(new File(systemDataDir, "sessions"), ejbName);
      if (debug) {
         log.debug("Storing sessions for '" + ejbName + "' in: " + dataDir);
      }

      // if the directory does not exist then try to create it
      if (!dataDir.exists()) {
         if (!dataDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + dataDir);
         }
      }
      
      // make sure we have a directory
      if (!dataDir.isDirectory()) {
         throw new IOException("File exists where directory expected: " + dataDir);
      }

      // make sure we can read and write to it
      if (!dataDir.canWrite() || !dataDir.canRead()) {
         throw new IOException("Directory must be readable and writable: " + dataDir);
      }
      
      // Purge state session state files
      File[] sessions = dataDir.listFiles();
      for (int i = 0; i < sessions.length; i++)
      {
         if (! sessions[i].delete()) {
            log.warn("Failed to delete session state file: " + sessions[i]);
         }
         else {
            log.debug("Removed stale session state: " + sessions[i]);
         }
      }
   }

   /**
    * Make a file for the given instance id.
    */
   protected File getFile(final Object key)
   {
      return new File(dataDir, key + ".ser");
   }

   public void createSession(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws Exception
   {
      // Set id
      Object id = new UID();
      ctx.setId(id);
        
      // Get methods
      try
      {
         String ejbName = "ejbC" + m.getName().substring(1);
         Method createMethod = con.getBeanClass().getMethod(ejbName, m.getParameterTypes());
            
         // Call ejbCreate<METHOD>
         createMethod.invoke(ctx.getInstance(), args);
      }
      catch (IllegalAccessException e)
      {
         // Clear id
         ctx.setId(null);
            
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         // Clear id
         ctx.setId(null);
            
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
         else if (e instanceof Exception)
         {
            // Remote, Create, or custom app. exception
            throw (Exception)e;
         }
         else if (e instanceof Error)
         {
            throw (Error)e;
         }
         else {
            throw new org.jboss.util.UnexpectedThrowable(e);
         }
      }
        
      // Insert in cache
      ((StatefulSessionContainer)con).getInstanceCache().insert(ctx);
        
      // Create EJBObject
      if (con.getProxyFactory() != null)
         ctx.setEJBObject((EJBObject)con.getProxyFactory().getStatefulSessionEJBObject(id));
      
      // Create EJBLocalObject
      if (con.getLocalHomeClass() != null)
         ctx.setEJBLocalObject(con.getLocalProxyFactory().getStatefulSessionEJBLocalObject(id));
   }

   public void activateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException
   {
      try
      {
         boolean debug = log.isDebugEnabled();
         if (debug) {
            log.debug("Attempting to activate; ctx=" + ctx);
         }

         Object id = ctx.getId();

         // Load state
         File file = getFile(id);
         if (debug) {
            log.debug("Reading object state from: " + file);
         }

         ObjectInputStream in =
            new SessionObjectInputStream(ctx, new BufferedInputStream(new FileInputStream(file)));
         
         try {
            Object obj = in.readObject();
            log.debug("Object state: " + obj);
            
            ctx.setInstance(obj);
         }
         finally {
            in.close();
         }
            
         removePassivated(id);
            
         // Call bean
         EJB_ACTIVATE_METHOD.invoke(ctx.getInstance(), new Object[0]);

         if (debug) {
            log.info("Activation complete; ctx=" + ctx);
         }
      }
      catch (ClassNotFoundException e)
      {
         throw new ServerException("Could not activate", e);
      }
      catch (IOException e)
      {
         throw new ServerException("Could not activate", e);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
         else if (e instanceof RemoteException)
         {
            // Remote, Create, or custom app. exception
            throw (RemoteException)e;
         }
         else if (e instanceof Error)
         {
            throw (Error)e;
         }
         else {
            throw new org.jboss.util.UnexpectedThrowable(e);
         }
      }
   }
    
   public void passivateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException
   {
      try
      {
         boolean debug = log.isDebugEnabled();
         if (debug) {
            log.debug("Attempting to passivate; ctx=" + ctx);
         }
         
         // Call bean
         EJB_PASSIVATE_METHOD.invoke(ctx.getInstance(), new Object[0]);
            
         // Store state

         File file = getFile(ctx.getId());
         if (debug) {
            log.debug("Saving object state to: " + file);
         }
         
         ObjectOutputStream out =
            new SessionObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

         try {
            Object obj = ctx.getInstance();
            if (debug) {
               log.debug("Writing object state: " + obj);
            }
            
            out.writeObject(obj);
         }
         finally {
            out.close();
         }

         if (debug) {
            log.debug("Passivation complete; ctx=" + ctx);
         }
      }
      catch (IOException e)
      {
         throw new ServerException("Could not passivate", e);
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
         else if (e instanceof RemoteException)
         {
            // Remote, Create, or custom app. exception
            throw (RemoteException)e;
         }
         else if (e instanceof Error)
         {
            throw (Error)e;
         }
         else {
            throw new org.jboss.util.UnexpectedThrowable(e);
         }
      }
   }
    
   public void removeSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException, RemoveException
   {
      try
      {
         boolean debug = log.isDebugEnabled();
         if (debug) {
            log.debug("Attempting to remove; ctx=" + ctx);
         }

         // Call bean
         EJB_REMOVE_METHOD.invoke(ctx.getInstance(), new Object[0]);

         if (debug) {
            log.debug("Removal complete; ctx=" + ctx);
         }
      }
      catch (IllegalAccessException e)
      {
         // Throw this as a bean exception...(?)
         throw new EJBException(e);
      }
      catch (InvocationTargetException ite)
      {
         Throwable e = ite.getTargetException();
         if (e instanceof EJBException)
         {
            // Rethrow exception
            throw (EJBException)e;
         }
         else if (e instanceof RuntimeException)
         {
            // Wrap runtime exceptions
            throw new EJBException((Exception)e);
         }
         else if (e instanceof RemoveException)
         {
            throw (RemoveException)e;
         }
         else if (e instanceof Error)
         {
            throw (Error)e;
         }
         else {
            throw new org.jboss.util.UnexpectedThrowable(e);
         }
      }
   }
    
   public void removePassivated(Object key)
   {
      boolean debug = log.isDebugEnabled();
      
      // OK also if the file does not exists
      File file = getFile(key);
      if (debug) {
         log.debug("Removing passivated state file: " + file);
      }
      
      if (file.exists() && !file.delete()) {
         log.warn("Failed to delete passivated state file: " + file);
      }
   }
}
