/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.rmi.ServerException;
import java.io.IOException;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.system.Registry;
import org.jboss.util.FinderResults;

import org.jboss.logging.Logger;


/**
*  <description>
*
* As we remove the one one association between container STACK and invoker we keep this around
* IN the future the creation of proxies is a task done on a container basis but the container
* as a logical representation, in other words, the container "Entity with RMI/IIOP" is not a 
* container stack but an association at the invocation level that points to all metadata for 
* a given container. 
*
* In other words this is here for legacy reason and to not disrupt the container at once
* In particular we declare that we "implement" the container invoker interface when we are
* just implementing the Proxy generation calls. Separation of concern. 
*
*  @see <related>
*  @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
*  @version $Revision: 1.1 $
*/

public class ProxyFactory
implements ContainerInvoker
{
   
   // Metadata for the proxies
   public EJBMetaData ejbMetaData ;
   
   static Logger log = Logger.getLogger(ProxyFactory.class);
   EJBHome home;
   EJBObject statelessObject;
   
   // The name of the bean being deployed
   String jndiName;
   
   // The name of the delegate invoker
   Invoker invoker;
   
   // A pointer to the container this proxy factory is dedicated to
   Container container;
   
   
   // Container plugin implementation -----------------------------------------
   
   public void setContainer(Container con)
   {
      this.container = con;
   }
   
   public void create()
   throws Exception
   {
      Context ctx = new InitialContext();
      
      jndiName = container.getBeanMetaData().getJndiName();
      
      // Create metadata
      
      /**
      Constructor signature is
      
      public EJBMetaDataImpl(Class remote,
      Class home,
      Class pkClass,
      boolean session,
      boolean statelessSession,
      HomeHandle homeHandle)
      */      
      if (container.getBeanMetaData() instanceof EntityMetaData)
      {
         Class pkClass;
         EntityMetaData metaData = (EntityMetaData)container.getBeanMetaData();
         String pkClassName = metaData.getPrimaryKeyClass();
         try
         {
            if (pkClassName != null)
               pkClass = container.getClassLoader().loadClass(pkClassName);
            else
               pkClass = container.getClassLoader().loadClass(metaData.getEjbClass()).getField(metaData.getPrimKeyField()).getClass();
         } catch (NoSuchFieldException e)
         {
            log.error("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         } catch (NullPointerException e)
         {
            log.error("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         }
         ejbMetaData = new EJBMetaDataImpl(
            ((ContainerInvokerContainer)container).getRemoteClass(),
            ((ContainerInvokerContainer)container).getHomeClass(),
            pkClass,
            false, //Session
            false, //Stateless
            new HomeHandleImpl(jndiName));
      } else
      {
         if (((SessionMetaData)container.getBeanMetaData()).isStateless())
         {
            ejbMetaData = new EJBMetaDataImpl(
               ((ContainerInvokerContainer)container).getRemoteClass(),
               ((ContainerInvokerContainer)container).getHomeClass(),
               null, //No PK
               true, //Session
               true, //Stateless
               new HomeHandleImpl(jndiName));
         } else
         { // we are stateful
            ejbMetaData = new EJBMetaDataImpl(
               ((ContainerInvokerContainer)container).getRemoteClass(),
               ((ContainerInvokerContainer)container).getHomeClass(),
               null, //No PK
               true, //Session
               false,//Stateless
               new HomeHandleImpl(jndiName));
         }
      }
      
      
      log.debug("Proxy Factory for "+jndiName+" initialized");
   }
   
   
   public void start()
   throws Exception
   {
      try{

         // Get the local invoker
         invoker = (Invoker) Registry.lookup(new ObjectName("JBOSS-SYSTEM:service=invoker,type=jrmp"));
         
         // FIXME FIXME In the near future move to 
         // invoker = (Invoker) Registry.lookup(new ObjectName(container.getInvokerType()));
         
         // Create the EJBHome
         this.home = 
         (EJBHome)Proxy.newProxyInstance(
            // Class loader pointing to the right classes from deployment
            ((ContainerInvokerContainer)container).getHomeClass().getClassLoader(),
            // The classes we want to implement home and handle
            new Class[] { ((ContainerInvokerContainer)container).getHomeClass(), Class.forName("javax.ejb.Handle")},
            // The home proxy as invocation handler
            new HomeProxy(jndiName,invoker, ejbMetaData));
         
         // Create stateless session object
         // Same instance is used for all objects
         if (!(container.getBeanMetaData() instanceof EntityMetaData) &&
            ((SessionMetaData)container.getBeanMetaData()).isStateless())
         {
            this.statelessObject = 
            (EJBObject)Proxy.newProxyInstance(
               // Correct CL         
               ((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
               // Interfaces    
               new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() } ,
               // SLSB proxy as invocation handler
               new StatelessSessionProxy(jndiName, invoker)
            );
         }
         

         
         // Bind the home in the JNDI naming space
         rebind(
            // The context
            new InitialContext(),
            // Jndi name
            container.getBeanMetaData().getJndiName(),
            // The Home
            getEJBHome());
         
         
         log.debug("Bound "+container.getBeanMetaData().getEjbName() + " to " + container.getBeanMetaData().getJndiName());
      
      } catch (Exception e)
      {
         throw new ServerException("Could not bind home", e);
      }
   }
   
   public void stop()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind(container.getBeanMetaData().getJndiName());
      
      } 
      catch (Exception e)
      {
         // ignore.
      }
   }
   
   public void destroy()
   {
   }
   
   
   // Container invoker implementation -------------------------------------
   
   
   public EJBMetaData getEJBMetaData()
   {
      return ejbMetaData;
   }
   
   public Object getEJBHome()
   {
      return home;
   }
   
   public Object getStatelessSessionEJBObject()
   {
      
      return statelessObject;
   }
   
   public Object getStatefulSessionEJBObject(Object id)
   {
      // marcf fixme: for the jrmp stuff include this creation on the client side
      return (EJBObject)Proxy.newProxyInstance(
         // Classloaders
         ((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
         // Interfaces
         new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
         // Proxy as invocation handler
         new StatefulSessionProxy(jndiName, id, invoker));
   }
   
   public Object getEntityEJBObject(Object id)
   {
      // marcf fixme: for the jrmp stuff include this creation on the client side
      return (EJBObject)Proxy.newProxyInstance(
         // Classloaders
         ((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
         // Interfaces
         new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
         // Proxy as invocation handler
         new EntityProxy(jndiName, id,invoker));
   }
   
   public Collection getEntityCollection(Collection ids)
   {
      ArrayList list = new ArrayList(ids.size());
      Iterator idEnum = ids.iterator();
      
      if ((ids instanceof FinderResults) && ((FinderResults) ids).isReadAheadOnLoadUsed()) {
         long listId = ((FinderResults) ids).getListId();
         
         for (int i = 0; idEnum.hasNext(); i++)
         {
            list.add(Proxy.newProxyInstance(((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
                  new Class[] { ((ContainerInvokerContainer)container).getRemoteClass(), ReadAheadBuffer.class },
                  new ListEntityProxy(jndiName, invoker, idEnum.next(), list, listId, i)));        
         }
      } else {
         while(idEnum.hasNext())
         {
            list.add(Proxy.newProxyInstance(((ContainerInvokerContainer)container).getRemoteClass().getClassLoader(),
                  new Class[] { ((ContainerInvokerContainer)container).getRemoteClass() },
                  new EntityProxy(jndiName, idEnum.next(), invoker)));
         }
      }
      return list;
   }
   
   protected void rebind(Context ctx, String name, Object val)
   throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate contexts exist
      
      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context)ctx.lookup(ctxName);
         } catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }
      
      ctx.rebind(n.get(0), val);
   }

}
