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
import org.jboss.invocation.InvocationResponse;
import org.jboss.metadata.ConfigurationMetaData;

/**
 * This interceptor delegates calls to an EntiyPersistenceStore.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.5 $
 */
public final class CMPInterceptor extends AbstractEntityTypeInterceptor
{
   /**
    * The persistence store to which all store messages are delegated.
    */
   private EntityPersistenceStore store;

   public void create() throws Exception
   {
      String className = config.getAttribute("manager");
      Class storeClass = getContainer().getClassLoader().loadClass(className);
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

   protected InvocationResponse createInstance(Invocation invocation) throws Exception
   {
      return new InvocationResponse(store.createBeanClassInstance());
   }

   protected InvocationResponse createEntity(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();

      store.initEntity(ctx);

      getNext().invoke(invocation);

      return new InvocationResponse(store.createEntity(
            invocation.getMethod(), 
            invocation.getArguments(), 
            ctx));
   }

   protected InvocationResponse postCreateEntity(Invocation invocation) throws Exception
   {
      store.postCreateEntity(
         invocation.getMethod(),
         invocation.getArguments(),
         (EntityEnterpriseContext)invocation.getEnterpriseContext());

      return super.getNext().invoke(invocation);
   }

   protected InvocationResponse removeEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);
      
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.removeEntity(ctx);
     
      return new InvocationResponse(null);
   }

   protected InvocationResponse query(Invocation invocation) throws Exception
   {
      ConfigurationMetaData configuration = 
            getContainer().getBeanMetaData().getContainerConfiguration();
      if(!configuration.getSyncOnCommitOnly())
      {
         EntityContainer.getEntityInvocationRegistry().synchronizeEntities();
      }   

      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      Method finderMethod = invocation.getMethod();
      Object[] args = invocation.getArguments();

      Class returnType = finderMethod.getReturnType();

      // invoke the finder method
      // FIXME this is lame but will be replaced by a smarter store
      if(returnType != Collection.class && returnType != Set.class)
      {
         Object primaryKey = store.findEntity(finderMethod, args, ctx);
         if(primaryKey == null)
         {
            return new InvocationResponse(Collections.EMPTY_LIST);
         } 
         return new InvocationResponse(Collections.singletonList(primaryKey));
      } 
      else 
      {
         return new InvocationResponse(store.findEntities(finderMethod, args, ctx));
      }
   }

   protected InvocationResponse isModified(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      return new InvocationResponse(new Boolean(store.isModified(ctx)));
   }

   protected InvocationResponse loadEntity(Invocation invocation) throws Exception
   {
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.loadEntity(ctx);
      
      getNext().invoke(invocation);

      return new InvocationResponse(null);
   }
   
   protected InvocationResponse storeEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);
      
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.storeEntity(ctx);
      
      return new InvocationResponse(null);
   }

   protected InvocationResponse activateEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);
      
      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.activateEntity(ctx);

      return new InvocationResponse(null);
   }

   protected InvocationResponse passivateEntity(Invocation invocation) throws Exception
   {
      getNext().invoke(invocation);

      EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();
      store.passivateEntity(ctx);

      return new InvocationResponse(null);
   }
}
