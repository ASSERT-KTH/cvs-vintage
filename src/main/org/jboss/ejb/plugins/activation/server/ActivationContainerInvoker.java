/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.activation.server;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.rmi.ServerException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Name;

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerFilter;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.jrmp.interfaces.HomeProxy;
import org.jboss.ejb.jrmp.interfaces.StatelessSessionProxy;
import org.jboss.ejb.jrmp.interfaces.StatefulSessionProxy;
import org.jboss.ejb.jrmp.interfaces.EntityProxy;
import org.jboss.ejb.jrmp.interfaces.EntityProxy;
import org.jboss.ejb.jrmp.interfaces.ContainerRemote;
import org.jboss.ejb.jrmp.interfaces.MethodInvocation;
import org.jboss.ejb.jrmp.interfaces.IteratorImpl;

import com.dreambean.codegen.Main;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public class ActivationContainerInvoker
   implements ContainerInvoker
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   HashMap containers = new HashMap();
   HashMap names = new HashMap();
   
   boolean JDK13 = false;
    
   // Static --------------------------------------------------------
   static ContainerRemote ci; // Prevent DGC
   public static ContainerRemote getLocal() { return ci; }
   
   // Constructors --------------------------------------------------
   public ActivationContainerInvoker()
      throws RemoteException
   {
      ci = this;
   }
   
   // Public --------------------------------------------------------
   public void addContainer(Name name, Container con)
   {
      containers.put(name, con);
      
      if (con instanceof ContainerFilter)
         con = ((ContainerFilter)con).getLastContainer();
      names.put(con, name);
   }

   public void removeContainer(Name name, Container con)
   {
      containers.remove(name);
      
      if (con instanceof ContainerFilter)
         con = ((ContainerFilter)con).getLastContainer();
      names.remove(con);
   }
   
   public EJBHome getEJBHome(Container con)
   {
      if (con instanceof ContainerFilter)
         con = ((ContainerFilter)con).getLastContainer();
      return (EJBHome)Proxy.newProxyInstance(con.getHomeClass().getClassLoader(),
                                           new Class[] { con.getHomeClass() },
                                           new HomeProxy((Name)names.get(con), this));      
   }
   
   public EJBObject getStatelessSessionEJBObject(Container con)
   {
      if (con instanceof ContainerFilter)
         con = ((ContainerFilter)con).getLastContainer();
         
//      if (JDK13)
//      {
         return (EJBObject)Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new StatelessSessionProxy((Name)names.get(con), this));
/*      } else
      {
         String proxyName = names.get(con).toString()+"Proxy";
         Class proxyClass;
         try
         {
            proxyClass = Class.forName(proxyName);
            Constructor c = proxyClass.getConstructor(new Class[] { InvocationHandler.class });
            return (EJBObject)c.newInstance(new Object[] { new StatelessSessionProxy((Name)names.get(con), this) });
         } catch (Exception e)
         {
            Logger.exception(e);
            return null;
         }
      } */
   }

   public EJBObject getStatefulSessionEJBObject(Container con, Object id)
   {
      if (con instanceof ContainerFilter)
         con = ((ContainerFilter)con).getLastContainer();
      return (EJBObject)Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new StatefulSessionProxy((Name)names.get(con), this, id));
   }

   public EJBObject getEntityEJBObject(Container con, Object id)
   {
      return (EJBObject)Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new EntityProxy((Name)names.get(con), this, id));
   }

   public Iterator getEntityIterator(Container con, Iterator ids)
   {
      ArrayList list = new ArrayList();
      while (ids.hasNext())
      {
         list.add(Proxy.newProxyInstance(con.getRemoteClass().getClassLoader(),
                                           new Class[] { con.getRemoteClass() },
                                           new EntityProxy((Name)names.get(con), this, ids.next())));
      }
      return new IteratorImpl(list);
   }
   
   // ContainerRemote implementation --------------------------------
   public Object invokeHome(Name con, MethodInvocation mi, Object tx, Principal user)
      throws Exception
   {
      Container c = (Container)containers.get(con);

      return c.invokeHome(mi.getMethod(), mi.getArguments());
   }
      
   public Object invoke(Name con, MethodInvocation mi, Object tx, Principal user)
      throws Exception
   {
      Container c = (Container)containers.get(con);

      return c.invoke(mi.getId(), mi.getMethod(), mi.getArguments());
   }
    
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
