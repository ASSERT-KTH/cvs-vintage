/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.jboss.jmx.interfaces.JMXConnector;

/**
* Factory delivering a list of servers and its available protocol connectors
* and after selected to initiate the connection
*
* This is just the (incomplete) interface of it
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public class ConnectorFactoryImpl {

	// Constants -----------------------------------------------------
	
	// Static --------------------------------------------------------

	// Attributes ----------------------------------------------------
	/** Server this factory is registered at **/
	private MBeanServer				mServer;

	// Public --------------------------------------------------------
	
	public ConnectorFactoryImpl(
		MBeanServer pServer
	) {
		mServer = pServer;
	}
	
	/**
	* Returns a list of available servers
	*
	* @param pServerQuery		Query instance to filter the list of servers
	*
	* @return					A collection of available servers
	*							names/identifications (String)
	**/
	public Collection getServers(
//AS		ServerQuery pServerQuery
	) {
		return Arrays.asList(
			new String[] {
				"localhost"
			}
		);
	}
	
	/**
	* Returns a list of available protocols (connectors)
	*
	* @param pServer			Server name/identification to look up
	*
	* @return					A collection of available protocols (String)
	**/
	public Collection getProtocols(
		String pServer
	) {
		return Arrays.asList(
			new String[] {
				"rmi"
			}
		);
	}

	/**
	* Initiate a connection to the given server with the given
	* protocol
	*
	* @param pServer			Server name/identification to connect to
	* @param pProtocol			Protocol to use
	*
	* @return					JMX Connector or null if server or protocol is
	*							not available
	**/
	public JMXConnector createConnection(
		String pServer,
		String pProtocol
	) {
		JMXConnector lConnector = null;
		if( pServer.equals( "localhost" ) ) {
			if( pProtocol.equals( "rmi" ) ) {
				try {
					lConnector = new RMIClientConnectorImpl(
						pServer
					);
					mServer.registerMBean(
						lConnector,
						new ObjectName( "DefaultDomain:name=RMIConnectorTo" + pServer )
					);
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
		System.out.println( "ConnectorFactoryImpl.createConnection(), got connector: " + lConnector );
		return lConnector;
	}
	
	/**
	* Removes the given connection and frees the resources
	*
	* @param pSever				Server name/identification of the connectino
	* @param pProtocol			Protocol used
	**/
	public void removeConnection(
		String pServer,
		String pProtocol
	) {
		if( pServer.equals( "localhost" ) ) {
			if( pProtocol.equals( "rmi" ) ) {
				try {
					Set lConnectors = mServer.queryMBeans(
						new ObjectName( "DefaultDomain:name=RMIConnectorTo" + pServer ),
						null
					);
					System.out.println( "ConnectorFactoryImpl.removeConnection(), got connectors: " + lConnectors );
					if( !lConnectors.isEmpty() ) {
						Iterator i = lConnectors.iterator();
						while( i.hasNext() ) {
							ObjectInstance lConnector = (ObjectInstance) i.next();
							mServer.invoke(
								lConnector.getObjectName(),
								"stop",
								new Object[] {},
								new String[] {}
							);
							mServer.unregisterMBean(
								lConnector.getObjectName()
							);
							System.out.println( "ConnectorFactoryImpl.removeConnection(), " +
								"unregister MBean: " + lConnector.getObjectName()
							);
						}
					}
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
	}
}
