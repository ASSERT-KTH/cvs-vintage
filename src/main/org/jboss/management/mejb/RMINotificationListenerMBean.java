/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.mejb;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
* MBean Interface of a Notification Listener MBean
* using RMI Callback Objects to send the notifications
* back to the client.
* <br>
* This interface is only necessary because of the naming
* conventions for standard MBeans.
*
* @author <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface RMINotificationListenerMBean
   extends ListenerMBean
{

	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------

}
