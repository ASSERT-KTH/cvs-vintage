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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

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
	) {
		// Check if there is a protocol given to query for
		boolean lProtocolQuery = pProtocol != null && pProtocol.length() > 0;
		// Get all available connectors from the JNDI server
		Iterator lConnectors = getConnectorList();
		Vector lServers = new Vector();
		// Go through the connectors list and check if part of the list
		while( lConnectors.hasNext() ) {
			ConnectorObject lConnector = (ConnectorObject) lConnectors.next();
			if( !lProtocolQuery || lConnector.getProtocol().equalsIgnoreCase( pProtocol ) ) {
				lServers.add( lConnector.getServer() );
			}
		}
		return lServers;
	}
	
	/**
	* Returns a list of available protocols (connectors)
	*
	* @param pServer			Server name/identification to look up if not
	*							null or empty otherwise it will be ignored
	*
	* @return					A collection of available protocols (String)
	**/
	public Collection getProtocols(
		String pServer
	) {
		// Check if there is a protocol given to query for
		boolean lServerQuery = pServer != null && pServer.length() > 0;
		// Get all available connectors from the JNDI server
		Iterator lConnectors = getConnectorList();
		Vector lProtocols = new Vector();
		// Go through the connectors list and check if part of the list
		while( lConnectors.hasNext() ) {
			ConnectorObject lConnector = (ConnectorObject) lConnectors.next();
			if( !lServerQuery || lConnector.getServer().equalsIgnoreCase( pServer ) ) {
				lProtocols.add( lConnector.getProtocol() );
			}
		}
		return lProtocols;
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
		// At the moment only RMI protocol is supported (on the client side)
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
	
	private Iterator getConnectorList() {
		Vector lServers = new Vector();
		try {
			InitialContext lNamingServer = new InitialContext();
			// Lookup the JNDI server
			NamingEnumeration enum = lNamingServer.list( "" );
			while( enum.hasMore() ) {
				NameClassPair lItem = (NameClassPair) enum.next();
				System.out.println( "Naming Server item: " + lItem );
				ConnectorObject lConnector = new ConnectorObject( lItem.getName() );
				if( lConnector.isValid() ) {
					lServers.add( lConnector );
				}
			}
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		
		return lServers.iterator();
	}
	/**
	* If valid then it will containg the informations about a
	* remote JMX Connector
	**/
	private class ConnectorObject {
		private String				mServerName = "";
		private String				mProtocolName = "";
		private boolean				mIsValid = false;
		
		public ConnectorObject( String pName ) {
			if( pName != null || pName.length() > 0 ) {
				StringTokenizer lName = new StringTokenizer( pName, ":" );
				if( lName.countTokens() == 3 ) {
					// Ignore "jmx"
					lName.nextToken();
					mServerName = lName.nextToken();
					mProtocolName = lName.nextToken();
					mIsValid = true;
				}
			}
		}
		
		public boolean isValid() {
			return mIsValid;
		}
		
		public String getProtocol() {
			return mProtocolName;
		}
		
		public String getServer() {
			return mServerName;
		}
	}
}
