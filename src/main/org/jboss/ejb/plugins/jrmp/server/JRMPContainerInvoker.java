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

import org.jboss.ejb.deployment.jBossEntity;
import org.jboss.ejb.deployment.jBossSession;
import org.jboss.ejb.deployment.ContainerConfiguration;
import org.jboss.ejb.deployment.JRMPContainerInvokerConfiguration;

import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.plugins.jrmp.interfaces.HomeProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.StatelessSessionProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.StatefulSessionProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.EntityProxy;
import org.jboss.ejb.plugins.jrmp.interfaces.ContainerRemote;
import org.jboss.ejb.plugins.jrmp.interfaces.MethodInvocation;
import org.jboss.ejb.plugins.jrmp.interfaces.IteratorImpl;
import org.jboss.ejb.plugins.jrmp.interfaces.EJBMetaDataImpl;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.6 $
 */
public abstract class JRMPContainerInvoker
   extends RemoteServer
   implements ContainerRemote, ContainerInvoker
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected boolean optimize = false;
   protected Container con;
   protected String jndiName;
   protected EJBMetaDataImpl ejbMetaData;
   protected EJBHome home;
   
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
   public Object invokeHome(MarshalledObject mimo, Object tx, Principal user)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(con.getClassLoader());
      
      MethodInvocation mi = (MethodInvocation)mimo.get();
      
      try
      {
		 
		  Method m = mi.getMethod();
		 System.out.println("In invoke Home "+m.getDeclaringClass()+m.getName()+m.getParameterTypes().length);
	   
         return con.invokeHome(mi.getMethod(), mi.getArguments());
//         return new MarshalledObject(con.invoke(mi.getId(), mi.getMethod(), mi.getArguments()));
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
      
   public Object invoke(MarshalledObject mimo, Object tx, Principal user)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(con.getClassLoader());
      try
      {
         
         MethodInvocation mi = (MethodInvocation)mimo.get();
      
         return con.invoke(mi.getId(), mi.getMethod(), mi.getArguments());
//         return new MarshalledObject(con.invoke(mi.getId(), mi.getMethod(), mi.getArguments()));
      } catch (Throwable e)
      {
         e.printStackTrace();
         throw (Exception)e;
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
    
   public Object invokeHome(Method m, Object[] args, Object tx, Principal user)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(con.getClassLoader());
      
      try
      {
         return con.invokeHome(m, args);
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
   }
      
   public Object invoke(Object id, Method m, Object[] args, Object tx, Principal user)
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(con.getClassLoader());
      
      try
      {
         return con.invoke(id, m, args);
      } finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
      }
      
   }
   
   // ContainerService implementation -------------------------------
   public void setContainer(Container con)
   {
      this.con = con;
      jndiName = con.getMetaData().getJndiName();
   }
   
   public void init()
      throws Exception
   {
      ContainerConfiguration conConf = con.getMetaData().getContainerConfiguration();
      if (conConf != null)
      {
         JRMPContainerInvokerConfiguration conf = (JRMPContainerInvokerConfiguration)conConf.getContainerInvokerConfiguration();
         optimize = conf.isOptimized();
      }
      
      // Create metadata
      if (con.getMetaData() instanceof jBossEntity)
      {
         ejbMetaData = new EJBMetaDataImpl(con.getRemoteClass(), con.getHomeClass(), con.getClassLoader().loadClass(((jBossEntity)con.getMetaData()).getPrimaryKeyClass()), false, false, getEJBHome());
      }
      else
      {
         if (((jBossSession)con.getMetaData()).getSessionType().equals("Stateless"))
            ejbMetaData = new EJBMetaDataImpl(con.getRemoteClass(), con.getHomeClass(), null, true, false, getEJBHome());
         else
            ejbMetaData = new EJBMetaDataImpl(con.getRemoteClass(), con.getHomeClass(), null, true, true, getEJBHome());
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
         UnicastRemoteObject.exportObject(this);
         MethodInvocation.addLocal(con.getMetaData().getJndiName(), this);
         
      } catch (IOException e)
      {
         throw new ServerException("Could not create secure socket factory", e);
      }
   }
   
   public void stop()
   {
      MethodInvocation.removeLocal(con.getMetaData().getJndiName());
   }

   public void destroy()
   {
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
 
   // Inner classes -------------------------------------------------
}
