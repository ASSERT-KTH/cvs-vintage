/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EntityBean;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.invocation.Invocation;

import org.jboss.metadata.ConfigurationMetaData;

/**
 * This interceptor delegates calls to an EntiyPersistenceStore.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.2 $
 */
public final class CMPInterceptor extends AbstractEntityTypeInterceptor
{
   /**
    * The persistence store to which all store messages are delegated.
    */
   private EntityPersistenceStore store;

   public void create() throws Exception
   {
      ConfigurationMetaData conf = 
            getContainer().getBeanMetaData().getContainerConfiguration();
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class storeClass = cl.loadClass(conf.getPersistenceManager());
      store = (EntityPersistenceStore) storeClass.newInstance();
      store.setContainer(getContainer());
      store.create();
   }

   public void start() throws Exception
   {
      store.start();
   }

   public void stop()
   {
      store.stop();
   }

   public void destroy()
   {
      if(store != null) 
      {
         store.destroy();
         store.setContainer(null);
         store = null;
      }
   }

   public Object createInstance(Invocation invocation) throws Exception
   {
      return store.createBeanClassInstance();
   }

   public Object createEntity(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();

      store.initEntity(ctx);

      getNext().invoke(invocation);

      return store.createEntity(
            invocation.getMethod(), 
            invocation.getArguments(), 
            ctx);
   }

   public Object postCreateEntity(Invocation invocation) throws Exception
   {
      store.postCreateEntity(
         invocation.getMethod(),
         invocation.getArguments(),
         (EntityEnterpriseContext)invocation.getEnterpriseContext());

      return super.getNext().invoke(invocation);
   }

   public Object removeEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);
      
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.removeEntity(ctx);
     
      return null;
   }

   public Object query(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      Method finderMethod = invocation.getMethod();
      Object[] args = invocation.getArguments();

      Class returnType = finderMethod.getReturnType();

      // invoke the finder method
      // FIXME this is lame but will be replaced by a smarter store
      Object finderResult;
      if(returnType != Collection.class && returnType != Set.class)
      {
         finderResult = store.findEntity(finderMethod, args, ctx);
      } 
      else 
      {
         finderResult = store.findEntities(finderMethod, args, ctx);
      }
 
      EntityContainer container = (EntityContainer)getContainer();
      EntityCache cache = (EntityCache)container.getInstanceCache();

      // Single object finder
      if(returnType != Collection.class && returnType != Set.class)
      {
         if(finderResult == null)
         {
            return null;
         }

         // convert primary keys to cache keys
         Object cacheKey = cache.createCacheKey(finderResult);

         // return the EJB[Local]Objects for the cache keys
         if(invocation.getType().isLocal())
         {
            LocalProxyFactory factory = container.getLocalProxyFactory();
            return factory.getEntityEJBLocalObject(cacheKey);
         }
         else
         {
            EJBProxyFactory factory = container.getProxyFactory();
            return factory.getEntityEJBObject(cacheKey);
         }
      }
   
      // Multi object finder
      if(finderResult == null)
      {
         return Collections.EMPTY_LIST;
      }

      // convert primary keys to cache keys
      List cacheKeys = new ArrayList();
      Collection primaryKeys = (Collection)finderResult;
      for(Iterator iter = primaryKeys.iterator(); iter.hasNext(); )
      {
         cacheKeys.add(cache.createCacheKey(iter.next()));
      }

      // Get the EJB[Local]Objects for the cache keys
      if(invocation.getType().isLocal())
      {
         LocalProxyFactory factory = container.getLocalProxyFactory();
         return factory.getEntityLocalCollection(cacheKeys);
      }
      else
      {
         EJBProxyFactory factory = container.getProxyFactory();
         return factory.getEntityCollection(cacheKeys);
      }
   }

   public Object isModified(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      return new Boolean(store.isModified(ctx));
   }

   public Object loadEntity(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.loadEntity(ctx);
      
      getNext().invoke(invocation);

      return null;
   }
   
   public Object storeEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);
      
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.storeEntity(ctx);
      
      return null;
   }

   public Object activateEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);
      
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.activateEntity(ctx);

      return null;
   }

   public Object passivateEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);

      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.passivateEntity(ctx);

      return null;
   }
}
