/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.ejb;

import org.jboss.jmx.connector.RemoteMBeanServer;

/**
* MBean Interface for the EJB-Connector (client-side) to
* be added to the ConnectorFactory MBeanServer.
*
* @author <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface EJBConnectorMBean
	extends RemoteMBeanServer
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
