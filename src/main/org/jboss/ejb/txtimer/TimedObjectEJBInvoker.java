/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectEJBInvoker.java,v 1.1 2004/04/09 22:45:26 tdiesler Exp $

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.EntityMetaData;

import javax.ejb.TimedObject;
import javax.ejb.Timer;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.io.Serializable;

/**
 * Invokes the ejbTimeout method on the TimedObject with the given id.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 07-Apr-2004
 */
public class TimedObjectEJBInvoker implements TimedObjectInvoker
{

   private Container container;
   private Method method;

   public TimedObjectEJBInvoker(Container container)
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
    * @param id The combined TimedObjectId
    * @param timer The Timer that is passed to ejbTimeout
    */
   public void invokeTimedObject(TimedObjectId id, Timer timer)
           throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(container.getClassLoader());
         Invocation inv = new Invocation(id.getInstancePk(), method, new Object[]{timer}, null, null, null);
         inv.setType(InvocationType.LOCAL);
         container.invoke(inv);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(cl);
      }
   }
}
