/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.naming;

import javax.management.ObjectName;

import org.jnp.server.MainMBean;

import org.jboss.util.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * The JMX management interface for the {@link NamingService} MBean.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.9 $
 */
public interface NamingServiceMBean
   extends ServiceMBean, MainMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=Naming");
}

