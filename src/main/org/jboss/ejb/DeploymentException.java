/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 *	This exception is thrown by the ContainerFactory if an EJB application
 *	could not be deployed
 *
 *   @see ContainerFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.4 $
 */
public class DeploymentException
   extends Exception
{
   // Attributes ----------------------------------------------------
	// The root cause of this exception
   Exception cause;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public DeploymentException(String message)
   {
      super(message);
   }
   
   public DeploymentException(String message, Exception e)
   {
      super(message);
      
      cause = e;
   }
   
   // Public --------------------------------------------------------
   public Exception getCause() { return cause; }
   
   public String toString() { return cause == null ? super.toString() : super.toString()+", Cause:"+cause; }
}
