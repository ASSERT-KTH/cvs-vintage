/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.client;

import java.util.Arrays;
import java.util.Collection;

import javax.management.DynamicMBean;
import javax.management.ObjectName;
import javax.management.MBeanServer;

import javax.naming.InitialContext;

import org.jboss.jmx.interfaces.JMXConnector;
import org.jboss.util.ServiceMBeanSupport;

/**
* Factory delivering a list of servers and its available protocol connectors
* and after selected to initiate the connection
*
* This is just the (incomplete) interface of it
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public class ConnectorFactoryService
	extends ServiceMBeanSupport
	implements ConnectorFactoryServiceMBean
{

	// Constants -----------------------------------------------------
	private static final String				JNDI_NAME = "jxm:connector:factory";
	
	// Static --------------------------------------------------------

	// Attributes ----------------------------------------------------
	/** Local MBeanServer this service is registered to **/
	private MBeanServer				mServer;
	/** Connector Factory instance **/
	private ConnectorFactoryImpl	mFactory;

	// Public --------------------------------------------------------
	
	public ConnectorFactoryService(
	) {
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
		return mFactory.getServers( pProtocol );
	}
	
	/**
	* Returns a list of available protocols (connectors)
	*
	* @param pServer			Server name/identification to look up
	*
	* @return					A collection of available protocols (String)
	*/
	public Collection getProtocols(
		String pServer
	) {
		return mFactory.getProtocols( pServer );
	}

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
	) {
		return mFactory.createConnection( pServer, pProtocol );
	}
	
	/**
	* Removes the given connection and frees the resources
	*
	* @param pSever				Server name/identification of the connectino
	* @param pProtocol			Protocol used
	*/
	public void removeConnection(
		String pServer,
		String pProtocol
	) {
		mFactory.removeConnection( pServer, pProtocol );
	}

	public ObjectName getObjectName(
		MBeanServer pServer, 
		ObjectName pName
	) throws javax.management.MalformedObjectNameException {
		mServer = pServer;
		System.out.println( "ConnectorFactoryService.getObjectName(), server: " + mServer +
			", object name: " + OBJECT_NAME +
			", instance: " + new ObjectName( OBJECT_NAME ) );
		return new ObjectName( OBJECT_NAME );
	}
	
	public String getName() {
		return "JMX Client Connector Factory";
	}
	
	// Protected -----------------------------------------------------
	protected void initService() throws Exception {
		try {
		System.out.println( "ConnectorFactoryService.initService(), server: " + mServer );
		mFactory = new ConnectorFactoryImpl( mServer );
		System.out.println( "ConnectorFactoryService.initService(), server: " + mServer +
			", factory: " + mFactory );
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	}
	
	protected void startService() throws Exception {
		new InitialContext().bind( JNDI_NAME, mFactory );
	}
	
	protected void stopService() {
		try {
			new InitialContext().unbind(JNDI_NAME);
		}
		catch( Exception e )	{
			log.exception( e );
		}
	}
}
