/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.adaptor.rmi;

import javax.management.ObjectName;
import org.jboss.util.jmx.ObjectNameFactory;

/**
 * RMI Adaptor allowing an network aware client
 * to work directly with a remote JMX Agent.
 *
 * @version $Revision: 1.4 $
 * @author  <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 */
public interface RMIAdaptorServiceMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.jmx:type=adaptor,protocol=RMI");

   String getJNDIName();
}
