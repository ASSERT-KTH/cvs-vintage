/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy.ejb.handle;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.InvokerInterceptor;
import org.jboss.invocation.PayloadKey;
import org.jboss.security.SecurityAssociation;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.15 $
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
      this.objectName = objectName;
      this.jndiName = jndiName;
      this.invoker = invoker;
      this.id = id;
      this.invokerProxyBinding = invokerProxyBinding;
      this.invokerID = invokerID;
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
      SecurityActions sa = SecurityActions.UTIL.getSecurityActions();
      try
      {
         Invocation invocation = new Invocation(
               null,
               GET_EJB_OBJECT,
               new Object[]{id},
               //No transaction set up in here? it will get picked up in the proxy
               null,
               // fix for bug 474134 from Luke Taylor
               sa.getPrincipal(),
               sa.getCredential());

         invocation.setObjectName(new Integer(objectName));
         invocation.setValue(InvocationKey.INVOKER_PROXY_BINDING,
            invokerProxyBinding, PayloadKey.AS_IS);

         // It is a home invocation
         invocation.setType(InvocationType.HOME);

         // Get the invoker to the target server (cluster or node)

         // Ship it
         if (isLocal())
            return (EJBObject) InvokerInterceptor.getLocal().invoke(invocation);
         else
            return (EJBObject) invoker.invoke(invocation);
      }
      catch(Exception e)
      {
         throw new ServerException("Could not get EJBObject", e);
      }
   }

   /**
    * Returns wether we are local to the originating container or not. 
    */
   protected boolean isLocal()
   {
      return invokerID != null && invokerID.equals(Invoker.ID);
   }

   interface SecurityActions
   {
      class UTIL
      {
         static SecurityActions getSecurityActions()
         {
            return System.getSecurityManager() == null ? NON_PRIVILEGED : PRIVILEGED;
         }
      }

      SecurityActions NON_PRIVILEGED = new SecurityActions()
      {
         public Principal getPrincipal()
         {
            return SecurityAssociation.getPrincipal();
         }

         public Object getCredential()
         {
            return SecurityAssociation.getCredential();
         }
      };

      SecurityActions PRIVILEGED = new SecurityActions()
      {
         private final PrivilegedAction getPrincipalAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getPrincipal();
            }
         };

         private final PrivilegedAction getCredentialAction = new PrivilegedAction()
         {
            public Object run()
            {
               return SecurityAssociation.getCredential();
            }
         };

         public Principal getPrincipal()
         {
            return (Principal)AccessController.doPrivileged(getPrincipalAction);
         }

         public Object getCredential()
         {
            return AccessController.doPrivileged(getCredentialAction);
         }
      };

      Principal getPrincipal();

      Object getCredential();
   }
}

