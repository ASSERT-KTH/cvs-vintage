/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jmx.server;

import java.io.File;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
*   <description> 
*      
* @author Rickard Öberg (rickard.oberg@telkel.com)
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public class RMIConnectorService
	extends ServiceMBeanSupport
	implements RMIConnectorServiceMBean
{
	// Constants -----------------------------------------------------
	//AS I am not quite sure if this works but somehow the protocol should become
	//AS part of the JNDI name because there could be more than one protcol
	public static String JNDI_NAME = "jmx:rmi";
	
	// Attributes ----------------------------------------------------
	MBeanServer server;
	RMIConnectorImpl adaptor;
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	public ObjectName getObjectName(
		MBeanServer server, 
		ObjectName name
	) throws javax.management.MalformedObjectNameException {
		this.server = server;
		return new ObjectName( OBJECT_NAME );
	}
	
	public String getName() {
		return "JMX RMI Connector";
	}
	
	// Protected -----------------------------------------------------
	protected void initService() throws Exception {
		adaptor = new RMIConnectorImpl( server );
	}
	
	protected void startService() throws Exception {
		new InitialContext().bind( JNDI_NAME, adaptor );
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
