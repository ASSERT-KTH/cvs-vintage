/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.invoker.client;

import java.io.Serializable;

/**
 * An exception for holding jmx exception so the invokers
 * don't unwrap them.
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.2 $
 */
public class InvokerAdaptorException extends Exception implements Serializable
{
   // Constants -----------------------------------------------------
   
   private static final long serialVersionUID = 24842201105890823L;
   
   // Attributes ----------------------------------------------------
   
   /** The wrapped exception */
   private Throwable wrapped;
   
   // Constructors --------------------------------------------------

   public InvokerAdaptorException()
   {
      // For serialization
   }
   
   public InvokerAdaptorException(Throwable wrapped)
   {
      this.wrapped = wrapped;
   }
   
   // Public --------------------------------------------------------

   public Throwable getWrapped() 
   {
      return wrapped;
   }

   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
