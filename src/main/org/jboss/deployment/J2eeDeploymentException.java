/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

/**
 *	<description> 
 *      
 *	@author Daniel Schulze daniel.schulze@telekel.com
 *	@version $Revision: 1.2 $
 */
public class J2eeDeploymentException 
	extends Exception
{
   Exception exception;

   // Constructors --------------------------------------------------
	public J2eeDeploymentException (String message)
   {
		super (message);
	}
	
	public J2eeDeploymentException (String message, Exception e)
   {
		super (message);
      this.exception = e;
	}
   
   public Exception getException()
   {
      return exception;
   }
}


