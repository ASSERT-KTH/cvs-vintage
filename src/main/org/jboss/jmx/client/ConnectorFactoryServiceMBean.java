/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.client;

import java.util.Arrays;
import java.util.Collection;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;

import org.jboss.jmx.interfaces.JMXConnector;
import org.jboss.util.ServiceMBean;

/**
* Factory delivering a list of servers and its available protocol connectors
* and after selected to initiate the connection
*
* This is just the (incomplete) interface of it
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface ConnectorFactoryServiceMBean
	extends ServiceMBean
{

	// Constants -----------------------------------------------------
	public static final String OBJECT_NAME = "Factory:name=JMX";
	
	// Public --------------------------------------------------------
	/**
	* Returns a list of available servers
	*
	* @param pProtocol			Servers supporting this protocol if not null
	*							or empty otherwise it will be ignored
	* @param pServerQuery		Query instance to filter the list of servers
	*
	* @return					A collection of available servers
	*							names/identifications (String)
	**/
	public Collection getServers(
		String pProtocol
//AS		ServerQuery pServerQuery
	);
	
	/**
	* Returns a list of available protocols (connectors)
	*
	* @param pServer			Server name/identification to look up
	*
	* @return					A collection of available protocols (String)
	*/
	public Collection getProtocols(
		String pServer
	);

	/**
	* Initiate a connection to the given server with the given
	* protocol
	*
	* @param pServer			Server name/identification to connect to
	* @param pProtocol			Protocol to use
	*/
	public JMXConnector createConnection(
		String pServer,
		String pProtocol
	);
	
	/**
	* Removes the given connection and frees the resources
	*
	* @param pSever				Server name/identification of the connectino
	* @param pProtocol			Protocol used
	*/
	public void removeConnection(
		String pServer,
		String pProtocol
	);
}
