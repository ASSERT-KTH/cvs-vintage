/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;

import javax.ejb.EntityBean;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.FinderException;

import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.LocalProxyFactory;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.PayloadKey;

import org.jboss.metadata.ConfigurationMetaData;

/**
 * Persistence manager for BMP entites.  All calls are simply deligated
 * to the entity implementation class.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.4 $
 */
public final class BaseEntityInterceptor extends AbstractEntityTypeInterceptor
{
   private Method ejbLoad;
   private Method ejbStore;
   private Method ejbActivate;
   private Method ejbPassivate;
   private Method ejbRemove;

   private int commitOption;
   
   public void create() throws Exception
   {
      // get the commit option
      EntityContainer container = (EntityContainer)getContainer();
      ConfigurationMetaData configuration = 
            container.getBeanMetaData().getContainerConfiguration();
      commitOption = configuration.getCommitOption();

      container.setRootEntityInterceptor(this);

      // get reference to some of the ejb call back methods
      ejbLoad = EntityBean.class.getMethod("ejbLoad", null);
      ejbStore = EntityBean.class.getMethod("ejbStore", null);
      ejbActivate = EntityBean.class.getMethod("ejbActivate", null);
      ejbPassivate = EntityBean.class.getMethod("ejbPassivate", null);
      ejbRemove = EntityBean.class.getMethod("ejbRemove", null);
   }

   protected InvocationResponse createEntity(Invocation invocation) throws Exception
   {
      // Call the create method
      InvocationResponse response = getNext().invoke(invocation);
      Object id = response.getResponse();

      // Get the context
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();

      // Set the id into the context and invocation
      ctx.setId(id);
      invocation.setId(id);

      EntityContainer container = (EntityContainer)getContainer();

      // Create EJBObject
      if(container.getProxyFactory() != null)
      {
         EJBProxyFactory proxyFactory = container.getProxyFactory();
         ctx.setEJBObject((EJBObject)proxyFactory.getEntityEJBObject(id));
      }

      // Create EJBLocalObject
      if(container.getLocalHomeClass() != null)
      {
         LocalProxyFactory localFactory = container.getLocalProxyFactory();
         ctx.setEJBLocalObject(localFactory.getEntityEJBLocalObject(id));
      }

      // return the actual entity 
      if(invocation.getType().isLocal())
      {
         return new InvocationResponse(ctx.getEJBLocalObject());
      } 
      else
      {
         return new InvocationResponse(ctx.getEJBObject());
      }
   }

   protected InvocationResponse removeEntity(Invocation invocation) throws Exception
   {
      // synchronize entities with the datastore before the bean is removed
      // this will write queued updates so datastore will be consistent 
      // before removal
      synchronizeData(invocation);
 
      // set the callback method
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbRemove,
            PayloadKey.TRANSIENT);

      InvocationResponse returnValue = getNext().invoke(invocation);

      // We signify "removed" with a null id.
      // There is no need to synchronize on the context since all the threads
      // reaching here have gone through the InstanceInterceptor so the
      // instance is locked and we only have one thread.  The case of reentrant
      // threads is unclear. Would you want to delete an instance in reentrancy?
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext)invocation.getEnterpriseContext();
      ctx.setId(null);

      return returnValue;
   }

   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      // As per the spec 9.6.4, entities must be synchronized with the 
      // datastore when an ejbFind<METHOD> is called.
      synchronizeData(invocation);

      EntityContainer container = (EntityContainer)getContainer();
      Method queryMethod = invocation.getMethod();

      // Check if findByPrimaryKey
      // If so we check if the entity is in cache first
      if (queryMethod.getName().equals("findByPrimaryKey") && 
            commitOption != ConfigurationMetaData.B_COMMIT_OPTION && 
            commitOption != ConfigurationMetaData.C_COMMIT_OPTION)
      {
         // get the cache key if possible
         EntityCache cache = (EntityCache)container.getInstanceCache();
         EntityEnterpriseContext ctx = 
               (EntityEnterpriseContext) invocation.getEnterpriseContext();

         // get the primary key
         Object[] args = invocation.getArguments();
         if(args.length != 1)
         {
            throw new FinderException("findByPrimaryKey must take a single argument: args.length=" + args.length);
         }
         Object id = args[0];
         if(id == null)
         {
            throw new FinderException("<null> is not a valid argument to findByPrimaryKey");
         }

         // check if the key is active
         if(cache.isActive(id))
         {
            // Object is active, and it exists, so no need to call query
            if(EJBHome.class.isAssignableFrom(queryMethod.getDeclaringClass()))
            {
               return new InvocationResponse(container.getProxyFactory().getEntityEJBObject(id));
            }
            else
            {
               LocalProxyFactory lpf = container.getLocalProxyFactory();
               return new InvocationResponse(lpf.getEntityEJBLocalObject(id));
            }
         }
      }

      return getNext().invoke(invocation);
   }

   protected InvocationResponse loadEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbLoad,
            PayloadKey.TRANSIENT);
      invocation.setValue(
            InvocationKey.CALLBACK_ARGUMENTS, 
            new Object[0],
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse storeEntity(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      
      if(ctx.getId() == null)
      {
         return new InvocationResponse(null);
      }

      // check if the entity has been modified
      invocation.setValue(
            EntityInvocationKey.TYPE, 
            EntityInvocationType.IS_MODIFIED,
            PayloadKey.TRANSIENT);
      InvocationResponse response = getNext().invoke(invocation);
      boolean isModified = 
         ((Boolean)response.getResponse()).booleanValue();
      
      // set the entity invocation type back to STORE 
      invocation.setValue(
            EntityInvocationKey.TYPE, 
            EntityInvocationType.STORE,
            PayloadKey.TRANSIENT);

      if(!isModified) 
      {
         return new InvocationResponse(null);
      }

      // ok it was modified return
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbStore,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse activateEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbActivate,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   protected InvocationResponse passivateEntity(Invocation invocation) throws Exception
   {
      invocation.setValue(
            InvocationKey.CALLBACK_METHOD, 
            ejbPassivate,
            PayloadKey.TRANSIENT);
      return getNext().invoke(invocation);
   }

   // FIXME: move this to a new EntitySynchronizationInterceptor
   private void synchronizeData(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      ConfigurationMetaData configuration = 
            container.getBeanMetaData().getContainerConfiguration();
      if(!configuration.getSyncOnCommitOnly()) {
         container.getEntityInvocationRegistry().synchronizeEntities(
               invocation.getTransaction());
      }
   }
}
