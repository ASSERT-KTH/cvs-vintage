/*
* JBoss, the OpenSource J2EE webOS
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.ejb;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;
import javax.naming.InitialContext; 

import org.jboss.jmx.connector.ejb.EJBConnector;

/**
* Test Client for the JMX EJB-Client Connector. It takes the arguments
* from the application call which has to look like:
*
* java -jar jmx-ejb-connector.jar [ -type JMX|RMI ] [ -queue <JMS Queue Name> ]
*           [ -server <JNDI Server Name> ] [ -ejb <EJB Connector JNDI Name> ]
*
* After the EJB-Connector is create it will show the offered MBean on the
* remote server and its attributes and operations will be listed and at
* the end this test client will add a notification listener to all MBeans
* on the remote server. The user can now wait for a notification because
* the client will be up and running. If the user stops the client by
* <CTRL>-C it will stop and deregister the selected Connector (locally)
* and this will remove all the registered listeners.
*
* @author Andreas Schaefer (andreas.schaefer@madplanet.com)
**/
public class TestClient {
	// Constants -----------------------------------------------------> 
	// Attributes ----------------------------------------------------> 
	// Static --------------------------------------------------------
	public static void main(String[] args)
		throws Exception
	{
      int lType = EJBConnector.NOTIFICATION_TYPE_JMS;
      String lJMSQueueName = "";
      String lJNDIServer = null;
      String lEJBAdaptorJNDIName = null;
      if( args.length > 0 ) {
         if( args[ 0 ].equals( "-help" ) || ( args.length % 2 ) == 1 ) {
            System.out.println( "Usage: java -jar jmx-ejb-connector.jar [ -type JMX|RMI ] " +
               "[ -queue <JMS Queue Name> ] [ -server <JNDI Server Name> ] " +
               "[ -ejb <EJB Connector JNDI Name> ]"
            );
            return;
         }
         for( int i = 0; i < ( args.length / 2 ); i++ ) {
            String lFirst = args[ 2 * i ];
            String lSecond = args[ 2 * i + 1 ];
            System.out.println( "Index: " + i + ", first: " + lFirst + ", second: " + lSecond );
            if( lFirst.equals( "-type" ) ) {
               if( lSecond.equals( "RMI" ) ) {
                  lType = EJBConnector.NOTIFICATION_TYPE_RMI;
               }
            }
            if( lFirst.equals( "-queue" ) ) {
               lJMSQueueName = lSecond;
            }
            if( lFirst.equals( "-server" ) ) {
               lJNDIServer = lSecond;
            }
            if( lFirst.equals( "-ejb" ) ) {
               lEJBAdaptorJNDIName = lSecond;
            }
         }
      }
      final int lOne = lType;
      final String lTwo = lJMSQueueName;
      final String lThree = lJNDIServer;
      final String lFour = lEJBAdaptorJNDIName;
		// Start server - Main does not have the proper permissions
		AccessController.doPrivileged(
			new PrivilegedAction() {
				public Object run() {
					new TestClient( lOne, lTwo, lThree, lFour );
					return null;
				}
			}
		);
   }
   
   public TestClient( int pType, String pQueue, String pJNDIServer, String pEJB ) {
		try {
			System.out.println( "Server: " + pJNDIServer );
			System.out.println(  );
			getUserInput(
				"Testing EJB-Adaptor from client to server\n" +
				"===========================================\n\n" +
				"1. Instantiate the EJB-Adaptor Client\n" +
				"=> hit any key to proceed"
			);
         MBeanServer lClient = new EJBConnector(
            pType,
            new String[] { pQueue },
            pEJB,
            pJNDIServer
         );
			getUserInput(
				"\n" +
				"2. List all available MBeans and its attributes\n" +
				"=> hit any key to proceed"
			);
			// List all services
			listServices( lClient );
			getUserInput(
				"\n" +
				"3. Try to add a listener to all available MBeans.\n" +
				"Please note that this will keep this test client up and running waiting\n" +
				"for an event from the server. If there is no event comming then try\n" +
				"to shutdown the JBoss server.\n" +
				"To end this test client just hit <CTRL>-C\n"+
				"=> hit any key to proceed"
			);
			// Try to register to all available, remote MBeans
			registerListeners( lClient );
		}
		catch( RuntimeMBeanException rme ) {
			System.err.println( "TestClient.main(), caught: " + rme +
				", target: " + rme.getTargetException() );
			rme.printStackTrace();
		}
		catch( MBeanException me ) {
			System.err.println( "TestClient.main(), caught: " + me +
				", target: " + me.getTargetException() );
			me.printStackTrace();
		}
		catch( RuntimeErrorException rte ) {
			System.err.println( "TestClient.main(), caught: " + rte +
				", target: " + rte.getTargetError() );
			rte.printStackTrace();
		}
		catch( ReflectionException re ) {
			System.err.println( "TestClient.main(), caught: " + re +
				", target: " + re.getTargetException() );
			re.printStackTrace();
		}
		catch( Exception e ) {
			System.err.println( "TestClient.main(), caught: " + e );
			e.printStackTrace();
		}
	}
	
	// Constructors --------------------------------------------------> 
	// Public --------------------------------------------------------
	public static void listServices( MBeanServer pClient )
		throws Exception
	{
		try {
			Iterator i = pClient.queryMBeans( null, null ).iterator();
			while( i.hasNext() ) {
				MBeanInfo info = pClient.getMBeanInfo( ( (ObjectInstance) i.next() ).getObjectName() );
				System.out.println( "MBean: " + info.getClassName() );
				MBeanAttributeInfo[] aInfos = info.getAttributes();
				for( int k = 0; k < aInfos.length; k++ ) {
					System.out.println( "\t" + k + ". Attribute: " +
					aInfos[ k ].getName() );
				}
				MBeanOperationInfo[] oInfos = info.getOperations();
				for( int k = 0; k < oInfos.length; k++ ) {
					System.out.println( "\t" + k + ". Operation: " +
					oInfos[ k ].getName() );
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void registerListeners( MBeanServer pClient )
		throws Exception
	{
		try {
			// Add a listener to all of the available MBeans
			Iterator i = pClient.queryMBeans( null, null ).iterator();
			int j = 0;
			while( i.hasNext() ) {
				ObjectInstance lBean = (ObjectInstance) i.next();
				try {
					pClient.addNotificationListener(
						lBean.getObjectName(),
						(NotificationListener) new Listener(),
						(NotificationFilter) null,
						new NotSerializableHandback(
							lBean.getObjectName() + "" + j++
						)
					);
					System.out.println( "Added notification listener to: " + lBean.getObjectName() );
				}
				catch( RuntimeOperationsException roe ) {
					System.out.println( "Could not add listener to: " + lBean.getObjectName() +
						", reason could be that it is not a broadcaster" );
				}
				catch( Exception e ) {
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	// Protected -----------------------------------------------------

	// Private -------------------------------------------------------
	
	private static int getUserInput( String lMessage ) {
		int lReturn = -1;
		try {
			System.out.println( lMessage );
			lReturn = System.in.read();
			System.in.skip( System.in.available() );
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
		return lReturn;
	}
	
	private static class Listener implements NotificationListener {
		
		public void handleNotification(
			Notification pNotification,
			Object pHandback
		) {
			System.out.println( "Got notification: " + pNotification +
				", from: " + pHandback );
		}
	}
	
	private static class NotSerializableHandback {
		
		private static int			miCounter = 0;
		
		private String				mName;
		
		public NotSerializableHandback( String pName ) {
			mName = pName + "[ " + miCounter++ + " ]";
		}
		
		public String toString() {
			return "NotSerializableHandback[ " + mName + " ]";
		}
	}
}
