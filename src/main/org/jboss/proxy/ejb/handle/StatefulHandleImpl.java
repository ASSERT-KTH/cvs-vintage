/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy.ejb.handle;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Principal;
import java.util.Hashtable;

import javax.ejb.EJBHome;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;

import org.jboss.invocation.Invoker;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.PayloadKey;
import org.jboss.naming.NamingContextFactory;
import org.jboss.security.SecurityAssociation;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.16 $
 */
public class StatefulHandleImpl
   implements Handle
{
   /** Serial Version Identifier. */
   static final long serialVersionUID = -6324520755180597156L;

   /** A reference to {@link Handle#getEJBObject}. */
   protected static final Method GET_EJB_OBJECT;

   /** The value of our local Invoker.ID to detect when we are local. */
   private Object invokerID = null;

   /**
    * Initialize <tt>Handle</tt> method references.
    */
   static
   {
      try
      {
         GET_EJB_OBJECT = Handle.class.getMethod("getEJBObject", new Class[0]);
      }
      catch(Exception e)
      {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }

   /** The identity of the bean. */
   public int objectName;
   public String jndiName;
   public String invokerProxyBinding;
   public Invoker invoker;
   public Object id;

   /** The JNDI env in effect when the home handle was created */
   private Hashtable jndiEnv;

   /** Create an ejb handle for a stateful session bean.
    * @param objectName - the session container jmx name
    * @param jndiName - the session home ejb name
    * @param invoker - the invoker to request the EJBObject from
    * @param invokerProxyBinding - the type of invoker binding
    * @param id - the session id
    */ 
   public StatefulHandleImpl(
      int objectName,
      String jndiName,
      Invoker invoker,
      String invokerProxyBinding,
      Object id,
      Object invokerID)
   {
      this.jndiName = jndiName;
      this.id = id;
      this.jndiEnv = (Hashtable) NamingContextFactory.lastInitialContextEnv.get();
      try
      {
         String property = System.getProperty("org.jboss.ejb.sfsb.handle.V327");
         if (property != null)
         {
            this.invokerProxyBinding = invokerProxyBinding;
            this.invokerID = invokerID;
            this.objectName = objectName;
            this.invoker = invoker;
         }
      }
      catch (AccessControlException ignored)
      {
      }

   }

   /**
    * @return the internal session identifier
    */
   public Object getID()
   {
      return id;
   }

   /**
    * @return the jndi name
    */
   public String getJNDIName()
   {
      return jndiName;
   }

   /**
    * Handle implementation.
    *
    * This differs from Stateless and Entity handles which just invoke
    * standard methods (<tt>create</tt> and <tt>findByPrimaryKey</tt>
    * respectively) on the Home interface (proxy).
    * There is no equivalent option for stateful SBs, so a direct invocation
    * on the container has to be made to locate the bean by its id (the
    * stateful SB container provides an implementation of
    * <tt>getEJBObject</tt>).
    *
    * This means the security context has to be set here just as it would
    * be in the Proxy.
    *
    * @return  <tt>EJBObject</tt> reference.
    *
    * @throws ServerException    Could not get EJBObject.
    */
   public EJBObject getEJBObject() throws RemoteException
   {
      try
      {
         InitialContext ic = null;
         if( jndiEnv != null )
            ic = new InitialContext(jndiEnv);
         else
            ic = new InitialContext();
         Proxy proxy = (Proxy) ic.lookup(jndiName);

         // call findByPrimary on the target
         InvocationHandler ih = Proxy.getInvocationHandler(proxy);
         return (EJBObject) ih.invoke(proxy, GET_EJB_OBJECT, new Object[] {id});
      }
      catch (RemoteException e)
      {
         throw e;
      }
      catch (Throwable t)
      {
         throw new RemoteException("Error during getEJBObject", t);
      }
   }
}

