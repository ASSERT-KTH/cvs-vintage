/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.interfaces;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.Notification;

/**
* Remote RMI client notification listener inferface.
* The interface will be registered on the server side.
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface RMINotificationListener 
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
