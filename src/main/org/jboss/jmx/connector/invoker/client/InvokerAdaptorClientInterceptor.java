/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.invoker.client;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.PayloadKey;
import org.jboss.proxy.Interceptor;

import javax.management.ObjectName;

/**
* An Interceptor that plucks the object name out of the arguments
* into an unmarshalled part of the payload.
* 
* @author <a href="mailto:adrian.brock@happeningtimes.com">Adrian Brock</a>
* @version $Revision: 1.3 $
*/
public class InvokerAdaptorClientInterceptor
   extends Interceptor
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public InvokerAdaptorClientInterceptor()
   {
      // For externalization to work
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Invoke using the invoker for remote invocations
    */
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      // Retrieve any relevent object name for this invocation
      ObjectName objectName = getObjectNameFromArguments(invocation);
      if (objectName != null)
         invocation.setValue("JMX_OBJECT_NAME", objectName, PayloadKey.AS_IS);

      try
      {
         return getNext().invoke(invocation);
      }
      catch (InvokerAdaptorException e)
      {
         throw e.getWrapped();
      }
   }

   /**
    * Return any target object name relevent for this invocation.<p>
    *
    * Methods that don't pass arguments that could be custom classes are ignored.<p>
    *
    * Classloading and registerMBean are ignored, 
    * they shouldn't be available remotely
    */
   public ObjectName getObjectNameFromArguments(Invocation invocation)
   {
      String method = invocation.getMethod().getName();
      if (method.equals("invoke") ||
         method.equals("setAttribute") ||
         method.equals("setAttributes") ||
         method.equals("addNotificationListener") ||
         method.equals("removeNotificationListener"))
      {
         return (ObjectName) invocation.getArguments()[0];
      }

      return null;
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
