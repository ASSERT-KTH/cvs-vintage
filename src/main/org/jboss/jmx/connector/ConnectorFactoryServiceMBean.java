/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.connector;

import java.util.Iterator;
import java.util.Hashtable;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.util.ObjectNameFactory;
import org.jboss.jmx.connector.RemoteMBeanServer;
import org.jboss.system.ServiceMBean;

/**
 * Factory delivering a list of servers and its available protocol connectors
 * and after selected to initiate the connection 
 *
 * This is just the (incomplete) interface of it
 *
 * @version $Revision: 1.5 $
 * @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 */
public interface ConnectorFactoryServiceMBean
   extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.rmi.connector:name=JMX");
	
   int getNotificationType();
   
   void setNotificationType( int pNotificationType );

   /**
    * @return JMS Queue Name and if not null then JMS will be used
    **/
   String getJMSName();
   
   /**
    * Sets the JMS Queue Factory Name which allows the server to send
    * the notifications asynchronous to the client
    *
    * @param pName If null the notification will be transferred
    *              by using RMI Callback objects otherwise it
    *              will use JMS
    **/
   void setJMSName( String pName );
   
   /**
    * @return EJB Adaptor JNDI Name used by the EJB-Connector
    **/
   String getEJBAdaptorName();
   
   /**
    * Sets the JNDI Name of the EJB-Adaptor
    *
    * @param pName If null the default JNDI name (ejb/jmx/ejb/adaptor) will
    *              be used for EJB-Connectors otherwise it will use this one
    **/
   void setEJBAdaptorName( String pName );
   
   /**
    * Look up for all registered JMX Connector at a given JNDI server
    *
    * @param pProperties List of properties defining the JNDI server
    * @param pTester Connector Tester implementation to be used
    *
    * @return An iterator on the list of ConnectorNames representing
    *         the found JMX Connectors
    **/
   Iterator getConnectors( Hashtable pProperties, ConnectorFactoryImpl.IConnectorTester pTester );

   /**
    * Initiate a connection to the given server with the given protocol
    *
    * @param pConnector Connector Name used to identify the remote JMX Connector
    *
    * @return JMX Connector or null if server or protocol is not supported
    **/
   RemoteMBeanServer createConnection(ConnectorFactoryImpl.ConnectorName pConnector);

   /**
    * Removes the given connection and frees the resources
    *
    * @param pConnector Connector Name used to identify the remote JMX Connector
    **/
   void removeConnection(ConnectorFactoryImpl.ConnectorName pConnector);
}
