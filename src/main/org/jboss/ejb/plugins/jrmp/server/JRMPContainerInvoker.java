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
import org.jboss.ejb.deployment.jBossEntity;
import org.jboss.ejb.deployment.jBossSession;
import org.jboss.ejb.deployment.ContainerConfiguration;
import org.jboss.ejb.deployment.JRMPContainerInvokerConfiguration;

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

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.11 $
 */
public abstract class JRMPContainerInvoker
   extends RemoteServer
   implements ContainerRemote, ContainerInvoker
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected boolean optimize = false;
   protected Container container;
   protected String jndiName;
   protected EJBMetaDataImpl ejbMetaData;
   protected EJBHome home;
	
	protected HashMap beanMethodInvokerMap;
	protected HashMap homeMethodInvokerMap;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
   }
   
   public boolean isOptimized()
   {
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
   public Object invokeHome(MarshalledObject mimo)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());
		
      try
      {
	      RemoteMethodInvocation rmi = (RemoteMethodInvocation)mimo.get();
	      rmi.setMethodMap(homeMethodInvokerMap);
		
			Transaction tx = rmi.getTransaction();
//DEBUG	        System.out.println("The home transaction is "+tx);
    
			System.out.println(container.getTransactionManager());
			if (tx == null)
				tx = container.getTransactionManager().getTransaction();
		
			return invokeHome(rmi.getMethod(), rmi.getArguments(), tx,
        rmi.getPrincipal(), rmi.getCredential() );
      } catch (Exception e)
      {
      	e.printStackTrace();
			throw e;
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
      
   public Object invoke(MarshalledObject mimo)
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
         if (tx == null)
         	tx = container.getTransactionManager().getTransaction();
				
         return invoke(rmi.getId(), rmi.getMethod(), rmi.getArguments(), tx,
          rmi.getPrincipal(), rmi.getCredential() );
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
    
   public Object invokeHome(Method m, Object[] args, Transaction tx,
    Principal identity, Object credential)
      throws Exception
   {
	   return container.invokeHome(new MethodInvocation(null , m, args, tx,
      identity, credential));
   }

   public Object invoke(Object id, Method m, Object[] args, Transaction tx,
    Principal identity, Object credential )
      throws Exception
   {
	   return container.invoke(new MethodInvocation(id, m, args, tx, identity, credential));
   }
   
   // ContainerService implementation -------------------------------
   public void setContainer(Container con)
   {
      this.container = con;
      jndiName = container.getMetaData().getJndiName();
   }
   
   public void init()
      throws Exception
   {
		// Set transaction manager
      GenericProxy.setTransactionManager(container.getTransactionManager());
      // Unfortunately this be a problem if many TM's are to be used
      // How to solve???
		
      ContainerConfiguration conConf = container.getMetaData().getContainerConfiguration();
      if (conConf != null)
      {
         JRMPContainerInvokerConfiguration conf = (JRMPContainerInvokerConfiguration)conConf.getContainerInvokerConfiguration();
         optimize = conf.isOptimized();
      }
      
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
		
      // Create metadata
      if (container.getMetaData() instanceof jBossEntity)
      {
         ejbMetaData = new EJBMetaDataImpl(((ContainerInvokerContainer)container).getRemoteClass(), ((ContainerInvokerContainer)container).getHomeClass(), container.getClassLoader().loadClass(((jBossEntity)container.getMetaData()).getPrimaryKeyClass()), false, false, new HomeHandleImpl(jndiName));
      }
      else
      {
         if (((jBossSession)container.getMetaData()).getSessionType().equals("Stateless"))
            ejbMetaData = new EJBMetaDataImpl(((ContainerInvokerContainer)container).getRemoteClass(), ((ContainerInvokerContainer)container).getHomeClass(), null, true, false, new HomeHandleImpl(jndiName));
         else
            ejbMetaData = new EJBMetaDataImpl(((ContainerInvokerContainer)container).getRemoteClass(), ((ContainerInvokerContainer)container).getHomeClass(), null, true, true, new HomeHandleImpl(jndiName));
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
         GenericProxy.addLocal(container.getMetaData().getJndiName(), this);
         
	      rebind(new InitialContext(), container.getMetaData().getJndiName(), ((ContainerInvokerContainer)container).getContainerInvoker().getEJBHome());
			
			Logger.log("Bound "+container.getMetaData().getEjbName() + " to " + container.getMetaData().getJndiName());
      } catch (IOException e)
      {
         throw new ServerException("Could not create secure socket factory", e);
      }
   }
   
   public void stop()
   {
      GenericProxy.removeLocal(container.getMetaData().getJndiName());
   }

   public void destroy()
   {
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
