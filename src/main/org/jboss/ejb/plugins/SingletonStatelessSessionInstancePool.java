/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.ejb.EJBHome;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;

import org.jboss.ejb.deployment.SingletonStatelessSessionInstancePoolConfiguration;

/**
 *	Singleton pool for session beans. This lets you have
 * singletons in EJB!
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public class SingletonStatelessSessionInstancePool
   implements InstancePool
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Container con;
   
   EnterpriseContext ctx;
   boolean inUse = false;
   boolean isSynchronized = true;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   /**
    *   Set the callback to the container. This is for initialization.
    *   The pool may extract the configuration from the container.
    *
    * @param   c  
    */
   public void setContainer(Container c)
   {
      this.con = c;
   }

   public void init()
      throws Exception
   {
	   SingletonStatelessSessionInstancePoolConfiguration conf = (SingletonStatelessSessionInstancePoolConfiguration)con.getMetaData().getContainerConfiguration().getInstancePoolConfiguration();
	   isSynchronized = conf.getSynchronized();
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
   
   /**
    *   Get the singleton instance
    *
    * @return     Context /w instance
    * @exception   RemoteException  
    */
   public synchronized EnterpriseContext get()
      throws RemoteException
   {
      // Wait while someone else is using it
      while(inUse && isSynchronized)
      {
         try { this.wait(); } catch (InterruptedException e) {}
      }
      
      // Create if not already created (or it has been discarded)
      if (ctx == null)
      {
         try
         {
            ctx = create(con.getBeanClass().newInstance(), con);
         } catch (InstantiationException e)
         {
            throw new ServerException("Could not instantiate bean", e);
         } catch (IllegalAccessException e)
         {
            throw new ServerException("Could not instantiate bean", e);
         }
      }
      
      // Lock and return instance
      inUse = true;
      return ctx;
   }
   
   /**
    *   Return an instance after invocation.
    *
    *   Called in 2 cases:
    *   a) Done with finder method
    *   b) Just removed
    *
    * @param   ctx  
    */
   public synchronized void free(EnterpriseContext ctx)
   {
      // Notify waiters
      inUse = false;
      this.notifyAll();
   }
   
   public void discard(EnterpriseContext ctx)
   {
      // Throw away
      try
      {
         ctx.discard();
      } catch (RemoteException e)
      {
         e.printStackTrace();
      }
      
      // Notify waiters
      inUse = false;
      this.notifyAll();
   }
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance, Container con)
      throws RemoteException
   {
      return new StatelessSessionEnterpriseContext(instance, con);
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

