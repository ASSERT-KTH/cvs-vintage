/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

/**
 *   Thrown by a deployer if an application component could not be
 *   deployed.
 *
 *   @see DeployerMBean
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.2 $
 */
public class DeploymentException
   extends Exception
{
   // Attributes ----------------------------------------------------

   /** The root cause of this exception */
   protected Throwable cause;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public DeploymentException(String message)
   {
      super(message);
   }
   
   public DeploymentException(String message, Throwable cause)
   {
      super(message);
      
      this.cause = cause;
   }
   
   // Public --------------------------------------------------------

   public Throwable getCause() { return cause; }
   
   public String toString()
   {
      return cause == null ? super.toString()
         : super.toString() + ", Cause: " + cause;
   }
}
