/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/** The interface for the ServiceControl MBean. This service
manages the lifecycle of JBoss services.

 *   @see org.jboss.util.Service
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 1.4 $
 */
public interface ServiceControlMBean
   extends Service
{
   // Public --------------------------------------------------------
    public void register(Service service);
    public void unregister(Service service);
}

