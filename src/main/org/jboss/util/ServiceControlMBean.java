/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/** The interface for the ServiceControl MBean. This service
manages the lifecycle of JBoss services.

 *   @see org.jboss.util.Service
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author Scott_Stark@displayscape.com
 *   @version $Revision: 1.2 $
 */
public interface ServiceControlMBean
   extends Service
{
   // Public --------------------------------------------------------
    public void register(Service service);
    public void unregister(Service service);
}

