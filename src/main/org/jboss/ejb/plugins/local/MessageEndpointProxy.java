
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.ejb.plugins.local;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import javax.transaction.xa.XAResource;
import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.logging.Logger;



/**
 * MessageEndpointProxy.java
 *
 *
 * Created: Sat May 31 21:10:19 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class MessageEndpointProxy
   implements InvocationHandler
{

   private final static Logger log = Logger.getLogger(MessageEndpointProxy.class);

   private final MessageDrivenContainer container;

   private final InvocationContext invocationContext = new InvocationContext();

   private ClassLoader oldCl; //This had better be single threaded!

   public MessageEndpointProxy(XAResource xaresource, MessageDrivenContainer container)
   {
      MessageEndpointContext mec = new MessageEndpointContext();
      mec.setXAResource(xaresource);
      invocationContext.setValue(InvocationKey.MESSAGE_ENDPOINT_CONTEXT, mec);
      invocationContext.setObjectName(new Integer(container.getJmxName().hashCode()));
      this.container = container;
   } // MessageEndpointProxy constructor


   // Implementation of java.lang.reflect.InvocationHandler

   /**
    * The <code>invoke</code> method
    *
    * @param object an <code>Object</code> value
    * @param method a <code>Method</code> value
    * @param objectArray an <code>Object[]</code> value
    * @return an <code>Object</code> value
    * @exception Throwable if an error occurs
    */
   public Object invoke(Object object, Method m, Object[] args) throws Throwable
   {
      if (m.equals(MessageEndpointContext.BEFORE_DELIVERY))
      {
         Thread currentThread = Thread.currentThread();
         oldCl = currentThread.getContextClassLoader();
         try
         {
            return invoke(m, args);
         }
         finally
         {
            currentThread.setContextClassLoader(container.getClassLoader());
         } // end of finally
      } // end of if ()
      if (m.equals(MessageEndpointContext.AFTER_DELIVERY))
      {
         Thread currentThread = Thread.currentThread();
         try
         {
            return invoke(m, args);
         }
         finally
         {
            currentThread.setContextClassLoader(oldCl);
         } // end of finally
      } // end of if ()
      return invoke(m, args);
   }

   private Object invoke(Method m, Object[] args) throws Throwable
   {
      try
      {
         Invocation invocation = new Invocation(
            null,//Maybe we get an id??
            m,
            args,
            null,
            null,//getPrincipal(), //this is ludicrous
            null);//getCredential());//as is this.
         invocation.setType(InvocationType.LOCAL);
         invocation.setInvocationContext(invocationContext);
         return container.invoke(invocation).getResponse();

      }
      catch (Exception e)
      {
         //following the great tradition of the JMSContainerInvoker
         log.info("Exception during message delivery, good luck", e);
         throw e;
      } // end of try-catch
   }


} // MessageEndpointProxy
