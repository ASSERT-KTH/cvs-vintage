/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

/**
 * Exception thrown by {@link org.jboss.deployment.J2eeDeployer} on
 * deployment problems.
 *      
 * @author <a href="mailto:daniel.schulze@telkel.com">Daniel Schulze</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.6 $
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


