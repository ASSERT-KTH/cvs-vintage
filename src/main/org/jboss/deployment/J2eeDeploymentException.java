/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

/**
 * Exception thrown by {@link org.jboss.deployment.J2eeDeployer} on
 * deployment problems.
 *      
 * @author Daniel Schulze daniel.schulze@telekel.com
 * @author Toby Allsopp (toby.allsopp@peace.com)
 * @version $Revision: 1.4 $
 */
public class J2eeDeploymentException 
   extends DeploymentException
{
    // Constructors --------------------------------------------------
    public J2eeDeploymentException (String message)
    {
        super (message);
    }

    public J2eeDeploymentException (String message, Throwable e)
    {
        super (message, e);
    }
}


