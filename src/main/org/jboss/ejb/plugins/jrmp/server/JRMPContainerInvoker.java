/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *      @version $Revision: 1.23 $
 */
public abstract class JRMPContainerInvoker
   extends RemoteServer
   implements ContainerRemote, ContainerInvoker, XmlLoadable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   protected boolean optimize = false;
   protected Container container;
   protected String jndiName;
   protected EJBMetaDataImpl ejbMetaData;
   // The home can be one.
   protected EJBHome home;
   // The Stateless Object can be one.
   protected EJBObject statelessObject;

    protected HashMap beanMethodInvokerMap;
    protected HashMap homeMethodInvokerMap;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
System.out.println("Container Invoker optimize set to '"+optimize+"'");
   }

   public boolean isOptimized()
   {
System.out.println("Optimize in action: '"+optimize+"'");
      return optimize;
   }

   public EJBMetaData getEJBMetaData()
   {
      return ejbMetaData;
   }

   public abstract EJBHome getEJBHome();

   public abstract EJBObject getStatelessSessionEJBObject();

   public abstract EJBObject getStatefulSessionEJBObject(Object id);

   public abstract EJBObject getEntityEJBObject(Object id);

   public abstract Collection getEntityCollection(Collection ids);

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
//DEBUG	        Logger.log("The home transaction is "+tx);

         //Logger.log(container.getTransactionManager().toString());
         // MF FIXME: the following don't belong here, the transaction is
        // passed by the call, not implicitely...
        //if (tx == null)
         // tx = container.getTransactionManager().getTransaction();

                                
     //DEBUG Logger.log("JRMPCI:invokeHome "+m.getName());
	 Logger.log("JRMPCI:invokeHome "+rmi.getMethod().getName());
	 
         return new MarshalledObject(invokeHome(rmi.getMethod(), rmi.getArguments(), tx,
        rmi.getPrincipal(), rmi.getCredential() ));
      } catch (Exception e)
      {
        // DEBUG Logger.exception(e);
         throw e;
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
        // MF FIXME: there should be no implicit thread passing of the transaction
         //if (tx == null)
         //   tx = container.getTransactionManager().getTransaction();

         Object id = rmi.getId();
         Method m = rmi.getMethod();
         Object[] args = rmi.getArguments();


	   //DEBUG Logger.log("JRMPCI:invoke "+m.getName());
	 	Logger.log("JRMPCI:invoke "+m.getName());
	 
         return new MarshalledObject(invoke(id, m, args, tx,
          rmi.getPrincipal(), rmi.getCredential()));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }

   public Object invokeHome(Method m, Object[] args, Transaction tx,
    Principal identity, Object credential)
      throws Exception
   {
	   //DEBUG
       Logger.log("JRMPCI (local) :invokeHome "+m.getName());
       if (tx != null) Logger.log("Tx is "+tx.toString());
          else Logger.log("Tx is null");
		  
	   //DEBUG
       return container.invokeHome(new MethodInvocation(null , m, args, tx,
        identity, credential));
   }

   public Object invoke(Object id, Method m, Object[] args, Transaction tx,
    Principal identity, Object credential )
      throws Exception
   {
	   // DEBUG
	     Logger.log("JRMPCI (local) :invoke "+m.getName());
       if (tx != null) Logger.log("Tx is "+tx.toString());
          else Logger.log("Tx is null");
		//DEBUG
       return container.invoke(new MethodInvocation(id, m, args, tx, identity, credential));
   }

   // ContainerService implementation -------------------------------
   public void setContainer(Container con)
   {
      this.container = con;
      jndiName = container.getBeanMetaData().getJndiName();
   }

   public void init()
      throws Exception
   {
       // Set transaction manager
      GenericProxy.setTransactionManager(container.getTransactionManager());
      // Unfortunately this be a problem if many TM's are to be used
      // How to solve???

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

      // MF FIXME: I suspect this is boloney... why do we need ALL these maps
      // There is one in the container and one in here...
      // Can't we unify ... these guys????
      try {

        // Get the getEJBObjectMethod
        Method getEJBObjectMethod = Class.forName("javax.ejb.Handle").getMethod("getEJBObject", new Class[0]);

        // Hash it
        homeMethodInvokerMap.put(new Integer(RemoteMethodInvocation.calculateHash(getEJBObjectMethod)),getEJBObjectMethod);
      }
      catch (Exception e) {Logger.exception(e);}


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
         try {
         if(pkClassName != null)
            pkClass = container.getClassLoader().loadClass(pkClassName);
         else
            pkClass = container.getClassLoader().loadClass(metaData.getEjbClass()).getField(metaData.getPrimKeyField()).getClass();
         } catch(NoSuchFieldException e) {
            System.out.println("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
            throw new RuntimeException("Primary Key Problem");
         } catch(NullPointerException e) {
            System.out.println("Unable to identify Bean's Primary Key class!  Did you specify a primary key class and/or field?  Does that field exist?");
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
         if (((SessionMetaData)container.getBeanMetaData()).isStateless()) {

            ejbMetaData = new EJBMetaDataImpl(
                           ((ContainerInvokerContainer)container).getRemoteClass(),
                           ((ContainerInvokerContainer)container).getHomeClass(),
                           null, //No PK
                           true, //Session
                           true, //Stateless
                           new HomeHandleImpl(jndiName));
         }
         // we are stateful
         else  {

            ejbMetaData = new EJBMetaDataImpl(
                           ((ContainerInvokerContainer)container).getRemoteClass(),
                                        ((ContainerInvokerContainer)container).getHomeClass(),
                                        null, //No PK
                                        true, //Session
                                        false,//Stateless
                                        new HomeHandleImpl(jndiName));
        }
      }

   }

    public void start()
    throws Exception
    {
        try
        {
            /*         UnicastRemoteObject.exportObject(this,
            4444,
            new SecureSocketFactory(),
            new SecureSocketFactory());
            */

            UnicastRemoteObject.exportObject(this,4444);
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


            Logger.log("Bound "+container.getBeanMetaData().getEjbName() + " to " + container.getBeanMetaData().getJndiName());
        } catch (IOException e)
        {
            throw new ServerException("Could not bind either home or invoker", e);
        }
    }

   public void stop()
   {
      try {
         InitialContext ctx = new InitialContext();
         ctx.unbind(container.getBeanMetaData().getJndiName());
         ctx.unbind("invokers/"+container.getBeanMetaData().getJndiName());

         UnicastRemoteObject.unexportObject(this, true);

      } catch (Exception e) {
         // ignore.
      }

      GenericProxy.removeLocal(container.getBeanMetaData().getJndiName());
   }

   public void destroy()
   {
   }

   // XmlLoadable implementation
   public void importXml(Element element) throws DeploymentException {
       String opt = MetaData.getElementContent(MetaData.getUniqueChild(element, "Optimized"));
       optimize = Boolean.valueOf(opt).booleanValue();
System.out.println("Container Invoker Optimize='"+optimize+"'");
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
