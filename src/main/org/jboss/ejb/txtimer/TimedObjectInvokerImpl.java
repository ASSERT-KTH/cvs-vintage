/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectInvokerImpl.java,v 1.4 2004/04/22 01:49:22 patriot1burke Exp $

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.ejb.StatelessSessionContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;

/**
 * An implementation of a TimedObjectInvoker, that can invoke deployed
 * EB, SLSB, and MDB
 *
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimedObjectInvokerImpl implements TimedObjectInvoker
{

   private Container container;
   private TimedObjectId timedObjectId;
   private Method method;

   public TimedObjectInvokerImpl(TimedObjectId timedObjectId, Container container)
   {
      try
      {
         this.container = container;
         this.timedObjectId = timedObjectId;
         this.method = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});

      }
      catch (NoSuchMethodException ignore)
      {
      }
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timer The Timer that is passed to ejbTimeout
    */
   public void callTimeout(Timer timer)
           throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(container.getClassLoader());
         Invocation inv = new Invocation(timedObjectId.getInstancePk(), method, new Object[]{timer}, null, null, null);
         inv.setValue(InvocationKey.INVOKER_PROXY_BINDING, null, PayloadKey.AS_IS);
         inv.setType(InvocationType.LOCAL);
         container.invoke(inv);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
   }
}
