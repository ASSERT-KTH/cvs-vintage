/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

/**
* An InvokerInterceptor that does not optimize in VM invocations
*
* @author Scott.Stark@jboss.org
* @version $Revision: 1.4 $
*/
public class MarshallingInvokerInterceptor
   extends InvokerInterceptor
{
   /** Serial Version Identifier. @since 1.1.4.1 */
   private static final long serialVersionUID = -6473336704093435358L;

   public MarshallingInvokerInterceptor()
   {
      // For externalization to work
   }
   
   // Public --------------------------------------------------------

   /**
    * Use marshalled invocations when the target is colocated.
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      if(isLocal(invocation))
         return invokeMarshalled(invocation);
      else
         return invokeInvoker(invocation);
   }
}
