/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class DeploymentException
   extends Exception
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
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
   
   public String toString() { return super.toString()+", Cause:"+cause; }
}
