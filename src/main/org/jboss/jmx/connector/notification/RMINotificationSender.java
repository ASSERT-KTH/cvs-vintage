/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.notification;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.Notification;

/**
* Remote interface of the RMI Notification Sender using
* RMI to transport the notifications over the wire.
*
* @author <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface RMINotificationSender
	extends Remote, Serializable
{

	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------
	/**
	* Handles the given notifcation event and passed it to the registered
	* listener
	*
	* @param pNotification				NotificationEvent
	* @param pHandback					Handback object
	*
	* @throws RemoteException			If a Remote Exception occurred
	*/
	public void handleNotification(
		Notification pNotification,
		Object pHandback
	) throws 
		RemoteException;
}
