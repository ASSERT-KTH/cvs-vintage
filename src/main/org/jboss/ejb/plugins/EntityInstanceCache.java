/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.util.Sync;

/**
 * Cache subclass for entity beans.
 * 
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.20 $
 */
public class EntityInstanceCache extends AbstractInstanceCache implements EntityCache
{
   private EntityContainer container;

   public int getCacheSize()
   {
      return getCache().size();
   }

   public void flush()
   {
      getCache().flush();
   }

   public void setContainer(Container c) 
   {
      this.container = (EntityContainer)c;
   }

   public EnterpriseContext get(Object id) 
      throws RemoteException, NoSuchObjectException 
   {
      return super.get(id);
   }

   public void remove(Object id)
   {
      super.remove(id);
   }

   public void destroy()
   {
      synchronized( this )
      {
         this.container = null;
      }
      super.destroy();
   }

   protected synchronized Container getContainer()
   {
      return container;
   }

   protected void passivate(EnterpriseContext ctx) throws Exception
   {
      container.passivateEntity((EntityEnterpriseContext)ctx);
   }

   protected void activate(EnterpriseContext ctx) throws Exception
   {
      container.activateEntity((EntityEnterpriseContext)ctx);
   }

   protected EnterpriseContext acquireContext() throws Exception
   {
      return container.getInstancePool().get();
   }

   protected void freeContext(EnterpriseContext ctx)
   {
      container.getInstancePool().free(ctx);
   }

   protected boolean canPassivate(EnterpriseContext ctx) 
   {
      if (ctx.isLocked()) 
      {
         // The context is in the interceptor chain
         return false;
      }
      else if (ctx.getTransaction() != null) 
      {
         return false;
      }
      else if (container.getLockManager().canPassivate(((EntityEnterpriseContext)ctx).getId()))
      {
         return false;
      }
      return true;
   }
}
