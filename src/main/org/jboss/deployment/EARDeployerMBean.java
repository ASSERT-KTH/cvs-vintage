/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;


import java.io.IOException;
import java.net.MalformedURLException;
import javax.management.ObjectName;
import org.jboss.system.ServiceMBean;

/**
 *   @see 
 *   @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 *   @version $Revision: 1.1 $
 */
public interface EARDeployerMBean
extends DeployerMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = "J2EE:service=EARDeployer";
    
   // Public --------------------------------------------------------
}
