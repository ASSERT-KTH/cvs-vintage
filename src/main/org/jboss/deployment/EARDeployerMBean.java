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

import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * The JMX management interface for the {@link EARDeployer} MBean.
 * 
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.3 $
 */
public interface EARDeployerMBean
   extends DeployerMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("J2EE:service=EARDeployer");
}
