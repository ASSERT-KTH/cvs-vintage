/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;

import javax.ejb.TimerHandle;

/**
 * This Interceptor validates the incomming arguments and the return value of the call.
 *
 * Here is the place where you want to make sure that local object don't pass through
 * the remote interface. 
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class CallValidationInterceptor
        extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation --------------------------------------
   
   public Object invokeHome(final Invocation mi) throws Exception
   {
      validateArguments(mi);
      Object obj = getNext().invokeHome(mi);
      return validateReturnValue(mi, obj);
   }

   public Object invoke(final Invocation mi) throws Exception
   {
      validateArguments(mi);
      Object obj = getNext().invoke(mi);
      return validateReturnValue(mi, obj);
   }

   /** Do some validation of the incoming parameters */
   protected void validateArguments(Invocation mi)
   {
      if (mi.getType() == InvocationType.REMOTE)
      {
         Object[] params = mi.getArguments();
         for (int i = 0; i < params.length; i++)
         {
            Object obj = params[i];
            if (obj instanceof TimerHandle)
               throw new IllegalArgumentException("Cannot pass TimerHandle through remote interface");
         }
      }
   }

   /** Do some validation of the return value */
   protected Object validateReturnValue(Invocation mi, Object retValue)
   {
      if (mi.getType() == InvocationType.REMOTE)
      {
         if (retValue instanceof TimerHandle)
            throw new IllegalArgumentException("Cannot return TimerHandle from remote interface");
      }
      return retValue;
   }

}
