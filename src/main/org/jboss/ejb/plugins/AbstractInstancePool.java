/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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

import org.w3c.dom.Element;
import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.XmlLoadable;
import org.jboss.logging.Logger;



/**
 *	<description>
 *
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.11 $
 */
public abstract class AbstractInstancePool
   implements InstancePool, XmlLoadable
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
      throws Exception
   {
//DEBUG      Logger.debug("Get instance "+this);

      if (!pool.empty())
      {
         return (EnterpriseContext)pool.pop();
      } else
      {
         try
         {
            return create(container.createBeanClassInstance());
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
//DEBUG      Logger.debug("Free instance:"+ctx.getId()+"#"+ctx.getTransaction());

      ctx.clear();

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
         // DEBUG Logger.exception(e);
      }
   }

   // Z implementation ----------------------------------------------

    // XmlLoadable implementation
    public void importXml(Element element) throws DeploymentException {
       String maximumSize = MetaData.getElementContent(MetaData.getUniqueChild(element, "MaximumSize"));
        try {
         maxSize = Integer.parseInt(maximumSize);
       } catch (NumberFormatException e) {
         throw new DeploymentException("Invalid MaximumSize value for instance pool configuration");
       }
    }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
   protected abstract EnterpriseContext create(Object instance)
      throws Exception;

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}

