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

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.naming.InitialContext;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.naming.Name;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;

import org.jboss.proxy.ejb.handle.StatefulHandleImpl;

/**
* An EJB stateful session bean proxy class.
*   
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
*
* <p><b>2001/11/23: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class StatefulSessionInterceptor
extends GenericEJBInterceptor
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
  // private static final long serialVersionUID = 1379411137308931705L;
   
   // Attributes ----------------------------------------------------
   
   /** JBoss generated identifier. */
   public static final Integer SESSION_ID = new Integer(new String("SESSION_ID"));

   // Static --------------------------------------------------------
  
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public StatefulSessionInterceptor() {}
   
   // Public --------------------------------------------------------
   
   /**
   * InvocationHandler implementation.
   *
   * @throws Throwable    Any exception or error thrown while processing.
   */
   public  Object invoke(Invocation invocation)
   throws Throwable
   {
      InvocationContext ctx = invocation.getInvocationContext();
            
      Method m = invocation.getMethod();
      
      // Implement local methods
      if (m.equals(TO_STRING)) {
         return ctx.getValue(JNDI_NAME) + ":" + ctx.getCacheId().toString();
      }
      else if (m.equals(EQUALS)) {

         return new Boolean(invocation.getArguments()[0].toString().equals(ctx.getValue(JNDI_NAME).toString() + ":" + ctx.getCacheId().toString()));
      }
      else if (m.equals(HASH_CODE)) {
         return new Integer(ctx.getCacheId().hashCode());
      }

      // Implement local EJB calls
      else if (m.equals(GET_HANDLE)) {
         
         return new StatefulHandleImpl(((Integer) ctx.getObjectName()).intValue(), (String) ctx.getValue(JNDI_NAME), ctx.getInvoker(), ctx.getCacheId());
      }
      else if (m.equals(GET_EJB_HOME)) {

         return getEJBHome(invocation);
      }
      else if (m.equals(GET_PRIMARY_KEY)) {

         return ctx.getCacheId();
      }
      else if (m.equals(IS_IDENTICAL)) {

         return new Boolean(invocation.getArguments()[0].toString().equals(ctx.getValue(JNDI_NAME).toString() + ":" + ctx.getCacheId().toString()));
      }

      // If not taken care of, go on and call the container
      else {
         
         // It is a remote invocation
         invocation.setType(Invocation.REMOTE);
         
         // On this entry in cache
         invocation.setId(ctx.getCacheId());
         
         return getNext().invoke(invocation);
      }
   }
}
