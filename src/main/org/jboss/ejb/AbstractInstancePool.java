/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

import javax.ejb.EJBHome;

import org.jboss.ejb.Container;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.EnterpriseContext;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public abstract class AbstractInstancePool
   implements InstancePool
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   private Container container;
   
   Stack pool = new Stack();
   int maxSize = 30;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   /**
    *   Set the callback to the container. This is for initialization.
    *   The IM may extract the configuration from the container.
    *
    * @param   c  
    */
   public void setContainer(Container c)
   {
      this.container = c;
   }
   
   public Container getContainer()
   {
      return container;
   }

   public void init()
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
    *   Get an instance without identity.
    *   Can be used by finders,create-methods, and activation
    *
    * @return     Context /w instance
    * @exception   RemoteException  
    */
   public synchronized EnterpriseContext get()
      throws RemoteException
   {
//      System.out.println("Get instance "+this);
      
      if (!pool.empty())
      {
         return (EnterpriseContext)pool.pop();
      } else
      {
         try
         {
            return create(container.getBeanClass().newInstance(), container);
         } catch (InstantiationException e)
         {
            throw new ServerException("Could not instantiate bean", e);
         } catch (IllegalAccessException e)
         {
            throw new ServerException("Could not instantiate bean", e);
         }
      }
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
      // Pool it
//      System.out.println("Free instance:"+ctx.getId()+"#"+ctx.getTransaction());
      
      if (pool.size() < maxSize)
      {
         pool.push(ctx);
      } else
      {
         discard(ctx);
      }
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
   }
   
   // Z implementation ----------------------------------------------
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected abstract EnterpriseContext create(Object instance, Container con)
      throws RemoteException;
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

