/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectInvokerImpl.java,v 1.7 2004/12/20 03:25:05 starksm Exp $

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationType;
import org.jboss.invocation.PayloadKey;
import org.jboss.security.RunAsIdentity;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;

/**
 * An implementation of a TimedObjectInvoker, that can invoke deployed
 * EB, SLSB, and MDB
 *
 * @author Thomas.Diesler@jboss.org
 * @author Scott.Stark@jboss.org
 * @since 07-Apr-2004
 * @version $Revision: 1.7 $
 */
public class TimedObjectInvokerImpl implements TimedObjectInvoker
{
   private static RunAsIdentity TIMEOUT_RUNAS = new RunAsIdentity("ejbTimeout", "ejbTimeout");

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
      ClassLoader callerClassLoader = SecurityActions.getContextClassLoader();
      SecurityActions.setContextClassLoader(container.getClassLoader());
      try
      {
         Invocation inv = new Invocation(timedObjectId.getInstancePk(), method, new Object[]{timer}, null, null, null);
         inv.setValue(InvocationKey.INVOKER_PROXY_BINDING, null, PayloadKey.AS_IS);
         inv.setType(InvocationType.LOCAL);
         SecurityActions.pushRunAsIdentity(TIMEOUT_RUNAS);
         container.invoke(inv);
      }
      finally
      {
         SecurityActions.popRunAsIdentity();
         SecurityActions.setContextClassLoader(callerClassLoader);
      }
   }
}
