/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import javax.ejb.EJBException;

import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatelessSessionEnterpriseContext;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 *  Singleton pool for session beans. This lets you have
 * singletons in EJB!
 *
 *  @author Rickard Oberg
 *  @version $Revision: 1.24 $
 */
public class SingletonStatelessSessionInstancePool extends AbstractInstancePool
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   EnterpriseContext ctx;
   boolean inUse = false;
   boolean isSynchronized = true;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void create()
      throws Exception
   {
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
    * @exception   Exception
    */
   public synchronized EnterpriseContext get()
      throws Exception
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
            ctx = create(getContainer().createBeanClassInstance());
         } catch (InstantiationException e)
         {
            throw new EJBException("Could not instantiate bean", e);
         } catch (IllegalAccessException e)
         {
            throw new EJBException("Could not instantiate bean", e);
         }
      }
      else
      {
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

   public synchronized void discard(EnterpriseContext ctx)
   {
      // Throw away
      try
      {
         ctx.discard();
      } catch (RemoteException e)
      {
         // DEBUG Logger.exception(e);
      }

      // Notify waiters
      inUse = false;
      this.notifyAll();
   }

   /**
    * Add a instance in the pool
    */
   public void add()
      throws Exception
   {
      // Empty
   }

   public int getCurrentSize()
   {
      return 1;
   }

   public int getMaxSize()
   {
      return 1;
   }

   // Z implementation ----------------------------------------------

    // XmlLoadable implementation
    public void importXml(Element element) throws DeploymentException
    {
      Element synch = MetaData.getUniqueChild(element, "Synchronized");
      isSynchronized = Boolean.valueOf(MetaData.getElementContent(synch)).booleanValue();
    }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected EnterpriseContext create(Object instance)
      throws Exception
   {
      // The instance is created by the caller and is a newInstance();
      return new StatelessSessionEnterpriseContext(instance, getContainer());
   }
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
