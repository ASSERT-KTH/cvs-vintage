/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.client;

import org.jboss.jmx.interfaces.JMXConnector;

/**
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface RMIClientConnectorImplMBean
	extends JMXConnector
{
	/**
	* Starts the connection to the given server
	*
	* @param pServer						Server the connector connects to
	*
	* @throw IllegalArgumentException		If the server is null
	**/
	public void start(
		Object pServer
	) throws IllegalArgumentException;
	/**
	* Stops the connection to the given server and remove all the registered
	* listeners
	**/
	public void stop();
}
