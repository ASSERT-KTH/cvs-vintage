/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: EntityBeanInvoker.java,v 1.1 2004/04/08 21:54:27 tdiesler Exp $

import org.jboss.ejb.EntityContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;

/**
 * Invokes the ejbTimeout method on the TimedObject with the given id.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class EntityBeanInvoker implements TimedObjectInvoker
{

   private EntityContainer container;
   private Method method;

   public EntityBeanInvoker(EntityContainer container)
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
   public void invokeTimedObject(String timedObjectId, Timer timer)
           throws Exception
   {
      int index = timedObjectId.lastIndexOf("#");
      String key = timedObjectId.substring(index + 1);

      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(container.getClassLoader());
         Invocation inv = new Invocation(key, method, new Object[]{timer}, null, null, null);
         inv.setType(InvocationType.LOCAL);
         container.invoke(inv);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
   }
}
