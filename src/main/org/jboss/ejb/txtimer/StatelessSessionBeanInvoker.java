/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: StatelessSessionBeanInvoker.java,v 1.1 2004/04/08 21:54:27 tdiesler Exp $

import org.jboss.ejb.StatelessSessionContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.logging.Logger;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;

/**
 * Invokes the ejbTimeout method on the TimedObject with the given id.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class StatelessSessionBeanInvoker implements TimedObjectInvoker
{
   // provide logging
   private static Logger log = Logger.getLogger(StatelessSessionBeanInvoker.class);

   private StatelessSessionContainer container;
   private Method method;

   public StatelessSessionBeanInvoker(StatelessSessionContainer container)
   {
      try
      {
         this.container = container;
         this.method = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
      }
      catch (NoSuchMethodException ignore)
      {
      }
   }

   /**
    * Invokes the ejbTimeout method on the TimedObject with the given id.
    *
    * @param timedObjectId The id of the TimedObject
    * @param timer         the Timer that is passed to ejbTimeout
    */
   public void invokeTimedObject(String timedObjectId, Timer timer) throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(container.getClassLoader());
         Invocation inv = new Invocation(null, method, new Object[]{timer}, null, null, null);
         inv.setType(InvocationType.LOCAL);
         container.invoke(inv);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
   }
}
