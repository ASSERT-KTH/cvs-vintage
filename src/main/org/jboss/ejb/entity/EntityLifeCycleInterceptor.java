/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.entity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import javax.ejb.EJBObject;
import org.jboss.ejb.EntityCache;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EJBProxyFactory;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.LocalProxyFactory;
import org.jboss.ejb.plugins.AbstractInterceptor;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationResponse;
import org.jboss.util.collection.SerializableEnumeration;

/**
 * Intercepts life cycle methods and redirects them to the persistence
 * manager.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.1 $
 */
public final class EntityLifeCycleInterceptor extends AbstractInterceptor
{
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      EntityContainer container = (EntityContainer)getContainer();
      LifeCycleEvent event = LifeCycleEvent.get(invocation);
      if(event == LifeCycleEvent.CREATE)
      {
         // Call the create method
         Object id = container.getPersistenceManager().createEntity(
               invocation.getMethod(),
               invocation.getArguments(),
               (EntityEnterpriseContext)invocation.getEnterpriseContext());

         // Get the context
         EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext) invocation.getEnterpriseContext();

         // we just create this one so it is clean
         ctx.setValid(true);

         // Set the id into the context and invocation
         ctx.setId(id);
         invocation.setId(id);

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
      else if(event == LifeCycleEvent.POST_CREATE)
      {
         container.getPersistenceManager().postCreateEntity(
               invocation.getMethod(),
               invocation.getArguments(),
               (EntityEnterpriseContext)invocation.getEnterpriseContext());
         return new InvocationResponse(null);
      } 
      else if(event == LifeCycleEvent.REMOVE)
      {
         // remove it from the persistence manager
         container.getPersistenceManager().removeEntity(
               (EntityEnterpriseContext)invocation.getEnterpriseContext());

         // the the id to null so other interceptors know it has been deleted
         // FIXME: this is lame
         EntityEnterpriseContext ctx = 
            (EntityEnterpriseContext)invocation.getEnterpriseContext();
         ctx.setId(null);

         return new InvocationResponse(null);
      } 
      else if(event == LifeCycleEvent.QUERY)
      {
         Collection primaryKeys = container.getPersistenceManager().query(
                  invocation.getMethod(),
                  invocation.getArguments(),
                  (EntityEnterpriseContext)invocation.getEnterpriseContext());

         // Single object finder
         Class returnType = invocation.getMethod().getReturnType();
         if(returnType != Collection.class && returnType != Enumeration.class)
         {
            if(primaryKeys.isEmpty())
            {
               return new InvocationResponse(null);
            }

            // get the sole primary key
            Object primaryKey = primaryKeys.iterator().next();

            // return the EJB[Local]Objects for the cache keys
            if(invocation.getType().isLocal())
            {
               LocalProxyFactory factory = container.getLocalProxyFactory();
               return new InvocationResponse(
                     factory.getEntityEJBLocalObject(primaryKey));
            }
            else
            {
               EJBProxyFactory factory = container.getProxyFactory();
               return new InvocationResponse(
                     factory.getEntityEJBObject(primaryKey));
            }
         }

         // Get the entity objects one by one to assure order is maintained
         Collection results = new ArrayList(primaryKeys.size());
         if(invocation.getType().isLocal())
         {
            LocalProxyFactory factory = container.getLocalProxyFactory();
            for(Iterator iterator = primaryKeys.iterator(); iterator.hasNext();)
            {
               Object primaryKey = iterator.next();
               if(primaryKey == null)
               {
                  results.add(null);
               }
               else
               {
                  results.add(factory.getEntityEJBLocalObject(primaryKey));
               }
            }
         }
         else
         {
            EJBProxyFactory factory = container.getProxyFactory();
            for(Iterator iterator = primaryKeys.iterator(); iterator.hasNext();)
            {
               Object primaryKey = iterator.next();
               if(primaryKey == null)
               {
                  results.add(null);
               }
               else
               {
                  results.add(factory.getEntityEJBObject(primaryKey));
               }
            }
         }

         // BMP entity finder methods are allowed to return an Enumeration.
         if(returnType == Enumeration.class)
         {
            if(invocation.getType().isLocal())
            {
               return new InvocationResponse(Collections.enumeration(results));
            }
            else
            {
               // This is on a remote interface, so we need a serializable 
               // Enumeration, and Collections.enumeration() is not.
               return new InvocationResponse(new SerializableEnumeration(results));
            }
         }

         // return an unmodifiable collection
         return new InvocationResponse(Collections.unmodifiableCollection(results));
      } 
      else
      {
         return getNext().invoke(invocation);
      }
   }
}
