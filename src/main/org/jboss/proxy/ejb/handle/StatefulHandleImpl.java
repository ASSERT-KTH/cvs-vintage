/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy.ejb.handle;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.Method;

import java.util.Hashtable;

import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.invocation.Invoker;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.security.SecurityAssociation;

/**
 * An EJB stateful session bean handle.
 *
 * @author  <a href="mailto:marc.fleury@jboss.org>Marc Fleury</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.7 $
 *
 * <p><b>Revisions:</b><br>
 * <p><b>2002/01/09: billb</b>
 * <ol>
 *   <li>Don't go to JNDI to get Invoker, instead, store invoker directly in handle.
 * </ol>
 * 
 */
public class StatefulHandleImpl
   implements Handle
{
   /** A reference to {@link Handle#getEJBObject}. */
   protected static final Method GET_EJB_OBJECT;
   
   /**
    * Initialize <tt>Handle</tt> method references.
    */
   static {
      try {
         GET_EJB_OBJECT = Handle.class.getMethod("getEJBObject", new Class[0]);
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   /** The identity of the bean. */
   public int objectName;
   public String jndiName;
   public Invoker invoker;
   public Object id;
   
   /**
    * Construct a <tt>StatefulHandleImpl</tt>.
    *
    * @param handle    The initial context handle that will be used
    *                  to restore the naming context or null to use
    *                  a fresh InitialContext object.
    * @param name      JNDI name.
    * @param id        Identity of the bean.
    */
   public StatefulHandleImpl(int objectName, String jndiName, Invoker invoker, Object id)
   {
      this.objectName = objectName;
      this.jndiName= jndiName;
      this.invoker = invoker;
      this.id = id;
   }
   
   /**
    * Handle implementation.
    *
    * This differs from Stateless and Entity handles which just invoke standard methods
    * (<tt>create</tt> and <tt>findByPrimaryKey</tt> respectively) on the Home interface (proxy).
    * There is no equivalent option for stateful SBs, so a direct invocation on the container has to
    * be made to locate the bean by its id (the stateful SB container provides an implementation of
    * <tt>getEJBObject</tt>).
    *
    * This means the security context has to be set here just as it would be in the Proxy.
    *
    * @return  <tt>EJBObject</tt> reference.
    *
    * @throws ServerException    Could not get EJBObject.
    */
   public EJBObject getEJBObject() throws RemoteException {
      try {
         Invocation invocation = 
         new Invocation(
            null,
            GET_EJB_OBJECT,
            new Object[] {id},
            //No transaction set up in here? it will get picked up in the proxy
            null,
            // fix for bug 474134 from Luke Taylor
            SecurityAssociation.getPrincipal(),
            SecurityAssociation.getCredential());
         
         invocation.setObjectName(new Integer(objectName));
         
         // It is a home invocation
         invocation.setType(Invocation.HOME);
         
         // Get the invoker to the target server (cluster or node)
        
         // Ship it
         return (EJBObject) invoker.invoke(invocation);
      }
      catch (Exception e) {
         throw new ServerException("Could not get EJBObject", e);
      }
   }
}

