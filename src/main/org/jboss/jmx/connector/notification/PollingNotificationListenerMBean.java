/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.notification;

import java.util.List;

import javax.management.Notification;
import javax.management.NotificationListener;

/**
* MBean Interface of a Notification Listener MBean
* using Polling to send the notifications back to the client.
* <br>
* This interface is only necessary because of the naming
* conventions for standard MBeans.
*
* @author <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface PollingNotificationListenerMBean
   extends ListenerMBean
{

	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------

   public List getNotifications();

   public List getNotifications( int pMaxiumSize );

}
