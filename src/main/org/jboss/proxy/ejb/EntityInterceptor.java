/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.util.HashMap;
import java.lang.reflect.Method;

import javax.naming.InitialContext;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

import org.jboss.ejb.CacheKey;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;

import org.jboss.proxy.ejb.handle.EntityHandleImpl;

/**
* An EJB entity bean proxy class.
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
*
* <p><b>2001/11/19: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class EntityInterceptor
extends GenericEJBInterceptor
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
//   private static final long serialVersionUID = -1523442773137704949L;
   
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public EntityInterceptor() {}
   
   // Public --------------------------------------------------------
   
   /**
   * InvocationHandler implementation.
   *
   * @param proxy   The proxy object.
   * @param m       The method being invoked.
   * @param args    The arguments for the method.
   *
   * @throws Throwable    Any exception or error thrown while processing.
   */
   public Object invoke(Invocation invocation) 
   throws Throwable
   {
      InvocationContext ctx = invocation.getInvocationContext();
      
      Method m = invocation.getMethod();
      
      // Implement local methods
      if (m.equals(TO_STRING)) {
         return ctx.getValue(JNDI_NAME).toString() + ":" + ((CacheKey)ctx.getCacheId()).getId().toString();
      }
      else if (m.equals(EQUALS)) {
        return new Boolean(invocation.getArguments()[0].toString()
        .equals(ctx.getValue(JNDI_NAME).toString() + ":" + ((CacheKey)ctx.getCacheId()).getId().toString()));
      }
      else if (m.equals(HASH_CODE)) {
         return new Integer(((CacheKey)ctx.getCacheId()).getId().hashCode());
      }
      
      // Implement local EJB calls
      else if (m.equals(GET_HANDLE)) {
         return new EntityHandleImpl((String)ctx.getValue(JNDI_NAME), ((CacheKey)ctx.getCacheId()).getId());
      }
      else if (m.equals(GET_PRIMARY_KEY)) {
         return ((CacheKey)ctx.getCacheId()).getId();
      }
      else if (m.equals(GET_EJB_HOME)) {
         return getEJBHome(invocation);
      }
      else if (m.equals(IS_IDENTICAL)) {
        return new Boolean(invocation.getArguments()[0].toString()
        .equals(ctx.getValue(JNDI_NAME).toString() + ":" + ((CacheKey)ctx.getCacheId()).getId().toString()));
      }
      
      // If not taken care of, go on and call the container
      else {
         
         // We are a Remote invocation
         invocation.setType(Invocation.REMOTE);
         // We pertain to this ID (represented by cache ID)
         invocation.setId(ctx.getCacheId());
         
         return getNext().invoke(invocation);
      }
   }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------//////
   
}


