/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.server;

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.rmi.ServerException;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Properties;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb.MethodInvocation;

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.plugins.jrmp.interfaces.RemoteMethodInvocation;
import org.jboss.ejb.plugins.jrmp.interfaces.HomeProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.HomeHandleImpl;
import org.jboss.ejb.plugins.jrmp.interfaces.StatelessSessionProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.StatefulSessionProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.GenericProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;
import org.jboss.ejb.plugins.jrmp.interfaces.IteratorImpl;
import org.jboss.ejb.plugins.jrmp.interfaces.EJBMetaDataImpl;
import org.jboss.ejb.plugins.jrmp.interfaces.SecureSocketFactory;

import org.jboss.system.SecurityAssociation;

import org.jboss.logging.Logger;

import org.jboss.ejb.DeploymentException;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.SessionMetaData;

import org.w3c.dom.Element;

/**
 *      <description>
 *
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *		@author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *      @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *      @version $Revision: 1.30 $
 */
public class JRMPContainerInvoker
   extends RemoteServer
   implements ContainerRemote, ContainerInvoker, XmlLoadable
{
   // Constants -----------------------------------------------------
   protected final static int ANONYMOUS_PORT = 0;

   // Attributes ----------------------------------------------------
   protected boolean optimize = false;
   protected int rmiPort = ANONYMOUS_PORT;
   protected boolean jdk122 = false;
   protected Container container;
   protected String jndiName;
   protected EJBMetaDataImpl ejbMetaData;
   // The home can be one.
   protected EJBHome home;
   // The Stateless Object can be one.
   protected EJBObject statelessObject;

   protected HashMap beanMethodInvokerMap;
   protected HashMap homeMethodInvokerMap;
   
   protected ContainerInvoker ciDelegate; // Delegate depending on JDK version

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
      //DEBUG		Logger.debug("Container Invoker optimize set to '"+optimize+"'");
   }

   public boolean isOptimized()
   {
      //DEBUG  Logger.debug("Optimize in action: '"+optimize+"'");
      return optimize;
   }
   
   public String getJndiName()
   {
      return jndiName;
   }

   // ContainerService implementation -------------------------------
   public void setContainer(Container con)
   {
      this.container = con;
      
      ciDelegate.setContainer(con);
   }

   public void init()
   throws Exception
   {
      jndiName = container.getBeanMetaData().getJndiName();
      
      // Set transaction manager
      GenericProxy.setTransactionManager(container.getTransactionManager());

      // Create method mappings for container invoker
      Method[] methods = ((ContainerInvokerContainer)container).getRemoteClass().getMethods();
      beanMethodInvokerMap = new HashMap();
      for (int i = 0; i < methods.length; i++)
      {
         beanMethodInvokerMap.put(new Integer(RemoteMethodInvocation.calculateHash(methods[i])), methods[i]);
      }

      methods = ((ContainerInvokerContainer)container).getHomeClass().getMethods();
      homeMethodInvokerMap = new HashMap();
      for (int i = 0; i < methods.length; i++)
      {
         homeMethodInvokerMap.put(new Integer(RemoteMethodInvocation.calculateHash(methods[i])), methods[i]);
      }

      try
      {

         // Get the getEJBObjectMethod
         Method getEJBObjectMethod = Class.forName("javax.ejb.Handle").getMethod("getEJBObject", new Class[0]);

         // Hash it
         homeMethodInvokerMap.put(new Integer(RemoteMethodInvocation.calculateHash(getEJBObjectMethod)),getEJBObjectMethod);
      } catch (Exception e)
      {
         Logger.exception(e);
      }


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
            if(pkClassName != null)
               pkClass = container.getClassLoader().loadClass(pkClassName);
            else
               pkClass = container.getClassLoader().loadClass(metaData.getEjbClass()).getField(metaData.getPrimKeyField()).getClass();
         } catch(NoSuchFieldException e)
         {
            Logger.error("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         } catch(NullPointerException e)
         {
            Logger.error("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         }
         ejbMetaData = new EJBMetaDataImpl(
            ((ContainerInvokerContainer)container).getRemoteClass(),
            ((ContainerInvokerContainer)container).getHomeClass(),
            pkClass,
            false, //Session
            false, //Stateless
            new HomeHandleImpl(jndiName));
      }
      else
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
         }
         // we are stateful
         else
         {

            ejbMetaData = new EJBMetaDataImpl(
               ((ContainerInvokerContainer)container).getRemoteClass(),
               ((ContainerInvokerContainer)container).getHomeClass(),
               null, //No PK
               true, //Session
               false,//Stateless
               new HomeHandleImpl(jndiName));
         }
      }
      
      ciDelegate.init();
   }

   public void start()
   throws Exception
   {
      try
      {
         // Export CI
         UnicastRemoteObject.exportObject(this, rmiPort);
         GenericProxy.addLocal(container.getBeanMetaData().getJndiName(), this);

         InitialContext context = new InitialContext();

         // Bind the home in the JNDI naming space
         rebind(
            // The context
            context,
            // Jndi name
            container.getBeanMetaData().getJndiName(),
            // The Home
            ((ContainerInvokerContainer)container).getContainerInvoker().getEJBHome());

         // Bind a bare bones invoker in the JNDI invoker naming space
         rebind(
            // The context
            context,
            // JNDI name under the invokers moniker
            "invokers/"+container.getBeanMetaData().getJndiName(),
            // The invoker
            ((ContainerInvokerContainer)container).getContainerInvoker());


         Logger.debug("Bound "+container.getBeanMetaData().getEjbName() + " to " + container.getBeanMetaData().getJndiName());
      } catch (IOException e)
      {
         throw new ServerException("Could not bind either home or invoker", e);
      }
   }

   public void stop()
   {
      try
      {
         InitialContext ctx = new InitialContext();
         ctx.unbind(container.getBeanMetaData().getJndiName());
         ctx.unbind("invokers/"+container.getBeanMetaData().getJndiName());

         UnicastRemoteObject.unexportObject(this, true);

      } catch (Exception e)
      {
         // ignore.
      }

      GenericProxy.removeLocal(container.getBeanMetaData().getJndiName());
   }

   public void destroy()
   {
   }
   
   // ContainerInvoker implementation -------------------------------
   public EJBMetaData getEJBMetaData()
   {
      return ejbMetaData;
   }

   public EJBHome getEJBHome()
   {
      return ciDelegate.getEJBHome();
   }

   public EJBObject getStatelessSessionEJBObject()
      throws RemoteException
   {
      return ciDelegate.getStatelessSessionEJBObject();
   }

   public EJBObject getStatefulSessionEJBObject(Object id)
      throws RemoteException
   {
      return ciDelegate.getStatefulSessionEJBObject(id);
   }

   public EJBObject getEntityEJBObject(Object id)
      throws RemoteException
   {
      return ciDelegate.getEntityEJBObject(id);
   }

   public Collection getEntityCollection(Collection ids)
      throws RemoteException
   {
      return ciDelegate.getEntityCollection(ids);
   }

   // ContainerRemote implementation --------------------------------
   public MarshalledObject invokeHome(MarshalledObject mimo)
   throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());

      try
      {
         RemoteMethodInvocation rmi = (RemoteMethodInvocation)mimo.get();
         rmi.setMethodMap(homeMethodInvokerMap);
         Transaction tx = rmi.getTransaction();

         return new MarshalledObject(container.invokeHome(new MethodInvocation(null, rmi.getMethod(), rmi.getArguments(), tx,
            rmi.getPrincipal(), rmi.getCredential() )));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public MarshalledObject invoke(MarshalledObject mimo)
   throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());

      try
      {
         RemoteMethodInvocation rmi = (RemoteMethodInvocation)mimo.get();
         rmi.setMethodMap(beanMethodInvokerMap);
         Transaction tx = rmi.getTransaction();

         return new MarshalledObject(container.invoke(new MethodInvocation(rmi.getId(), rmi.getMethod(), rmi.getArguments(), tx,
            rmi.getPrincipal(), rmi.getCredential() )));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public Object invokeHome(Method m, Object[] args, Transaction tx,
      Principal identity, Object credential)
   throws Exception
   {
      // Check if this call really can be optimized
      // If parent of callers classloader is != parent of our classloader -> not optimizable!
      //      if (Thread.currentThread().getContextClassLoader().getParent() != container.getClassLoader().getParent())
      if (!m.getDeclaringClass().isAssignableFrom(((ContainerInvokerContainer)container).getHomeClass()))
      {
         RemoteMethodInvocation rmi = new RemoteMethodInvocation(null, m, args);

         // Set the transaction context
         TransactionManager tm = container.getTransactionManager();
         rmi.setTransaction(tm != null? tm.getTransaction() : null);

         // Set the security stuff
         rmi.setPrincipal( SecurityAssociation.getPrincipal() );
         rmi.setCredential( SecurityAssociation.getCredential() );

         // Invoke on the container, enforce marshalling
         return invokeHome(new MarshalledObject(rmi)).get();
      }

      // Set the right context classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());

      try
      {
         return container.invokeHome(new MethodInvocation(null , m, args, tx,
            identity, credential));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public Object invoke(Object id, Method m, Object[] args, Transaction tx,
      Principal identity, Object credential )
   throws Exception
   {
      // Check if this call really can be optimized
      // If parent of callers classloader is != parent of our classloader -> not optimizable!
      //	   if (Thread.currentThread().getContextClassLoader().getParent() != container.getClassLoader().getParent())
      if (!m.getDeclaringClass().isAssignableFrom(((ContainerInvokerContainer)container).getRemoteClass()))
      {
         RemoteMethodInvocation rmi = new RemoteMethodInvocation(id, m, args);

         // Set the transaction context
         TransactionManager tm = container.getTransactionManager();
         rmi.setTransaction(tm != null? tm.getTransaction() : null);

         // Set the security stuff
         rmi.setPrincipal( SecurityAssociation.getPrincipal() );
         rmi.setCredential( SecurityAssociation.getCredential() );

         // Invoke on the container, enforce marshalling
         return invoke(new MarshalledObject(rmi)).get();
      }

      // Set the right context classloader
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());

      try
      {
         return container.invoke(new MethodInvocation(id, m, args, tx, identity, credential));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }


   // XmlLoadable implementation
   public void importXml(Element element) throws DeploymentException
   {
      String opt = MetaData.getElementContent(MetaData.getUniqueChild(element, "Optimized"));
      optimize = Boolean.valueOf(opt).booleanValue();
      
      String jdk = "122"; //MetaData.getElementContent(MetaData.getUniqueChild(element, "JDK"));
      if (jdk.equals("122"))
         jdk122 = true;
      else if (jdk.equals("auto"))
      {
         if (!(System.getProperty("java.vm.version").compareTo("1.3") >= 0))
            jdk122 = true;
      }
      
      // Create delegate depending on JDK version
      if (jdk122)
      {
         ciDelegate = new org.jboss.ejb.plugins.jrmp12.server.JRMPContainerInvoker(this);
      } else
      {/*
         ciDelegate = new org.jboss.ejb.plugins.jrmp13.server.JRMPContainerInvoker(this);
         */
      }
      
      try
      {
         String port = MetaData.getElementContent(MetaData.getUniqueChild(element, "RMIObjectPort"));
         rmiPort = Integer.parseInt(port);
      } catch(NumberFormatException e)
      {
         rmiPort = ANONYMOUS_PORT;
      } catch(DeploymentException e)
      {
         rmiPort = ANONYMOUS_PORT;
      }
      Logger.debug("Container Invoker RMI Port='"+(rmiPort == ANONYMOUS_PORT ? "Anonymous" : Integer.toString(rmiPort))+"'");
      Logger.debug("Container Invoker Optimize='"+optimize+"'");
   }


   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------
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

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

