/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector;

/**
* Client Side JMX Connector to be used in conjunction with
* a {@link JMXConnector server/protocol selector}. It contains the additional
* methods the client connector has to offer to open a connection to the server
* and at the end to close it.
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface JMXConnectorMBean 
	extends JMXConnector
{

	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------
	/**
	* Initialize the client connector to a given server side JMX
	* Connector
	*
	* @param pServer					Server indentification (because
	*									I do not know the necessary type
	*									for a general server ident. I
	*									choose Object)
	*
	* @throws IllegalArgumentException	If the given server is not valid
	*									or not recognized by the connector
	*									implementation
	*/
	public void start(
		Object pServer
	) throws IllegalArgumentException;
	/**
	* Stops the client connector, remove the remote connection and frees
	* the resources.
	*/
	public void stop();
	/**
	* Indicates if the connection is alive and ready to serve
	*
	* @return							True if the connection is alive
	*/
	public boolean isAlive();
	/**
	* Delivers the actual server description to which the client is
	* connected to
	*
	* @return							Description of the server the client
	*									is connected to or null if there is
	*									no connection
	*/
	public String getServerDescription();
}
