/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * This exception is thrown by the ContainerFactory if an EJB application
 * could not be deployed.
 *
 * @see ContainerFactory
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.8 $
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
   
   public Throwable getCause() {
      return cause;
   }
   
   public String toString() {
      return
         cause == null ?
         super.toString() :
         super.toString() + ", Cause: " + cause;
   }
}
