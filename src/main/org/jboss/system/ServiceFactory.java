/*
* JBoss, the OpenSource J2EE server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.system;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/** The ServiceFactory interface is used to obtain a Service
proxy instance for a named MBean.

@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.1 $
*/
public interface ServiceFactory
{
   /** Create a Service proxy instance for the MBean given by name.
   @param server, the MBeanServer instance
   @param name, the name of the MBean that wishes to be managed by
   the JBoss ServiceControl service.
   */
   public Service createService(MBeanServer server, ObjectName name);
}
