/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
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
 * and after selected to initiate the connection This is just the (incomplete)
 * interface of it
 *
 *@author    <A href="mailto:andreas.schaefer@madplanet.com">Andreas
 *      &quot;Mad&quot; Schaefer</A>
 *@created   May 2, 2001
 **/
public class ConnectorFactoryImpl {

   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Attributes ----------------------------------------------------

   private MBeanServer mServer;


   // Public --------------------------------------------------------

   public ConnectorFactoryImpl(
      MBeanServer pServer
   ) {
      mServer = pServer;
   }

   /**
    * Look up for all registered JMX Connector at a given JNDI server
    *
    * @param pProperties List of properties defining the JNDI server
    * @param pTester Connector Tester implementation to be used
    *
    * @return An iterator on the list of ConnectorNames representing
    *         the found JMX Connectors
    **/
   public Iterator getConnectors( Hashtable pProperties, IConnectorTester pTester ) {
      Vector lConnectors = new Vector();
      try {
         InitialContext lNamingServer = new InitialContext( pProperties );
         // Lookup the JNDI server
         NamingEnumeration enum = lNamingServer.list( "" );
         while( enum.hasMore() ) {
            NameClassPair lItem = ( NameClassPair ) enum.next();
            ConnectorName lName = pTester.check( lItem.getName(), lItem.getClass() );
            if( lName != null ) {
               lConnectors.add( lName );
            }
         }
      }
      catch( Exception e ) {
         e.printStackTrace();
      }

      return lConnectors.iterator();
   }

   /**
    * Initiate a connection to the given server with the given protocol
    *
    * @param pConnector Connector Name used to identify the remote JMX Connector
    *
    * @return JMX Connector or null if server or protocol is not supported
    **/
   public JMXConnector createConnection(
      ConnectorName pConnector
   ) {
      JMXConnector lConnector = null;
      // At the moment only RMI protocol is supported (on the client side)
      if( pConnector.getProtocol().equals( "rmi" ) ) {
         try {
            lConnector = new RMIClientConnectorImpl(
               pConnector.getServer()
            );
            mServer.registerMBean(
               lConnector,
               new ObjectName( "DefaultDomain:name=RMIConnectorTo" + pConnector.getServer() )
            );
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
      return lConnector;
   }

   /**
    * Removes the given connection and frees the resources
    *
    * @param pConnector Connector Name used to identify the remote JMX Connector
    **/
   public void removeConnection(
      ConnectorName pConnector
   ) {
      if( pConnector.getProtocol().equals( "rmi" ) ) {
         try {
            Set lConnectors = mServer.queryMBeans(
               new ObjectName( "DefaultDomain:name=RMIConnectorTo" + pConnector.getServer() ),
               null
            );
            if( !lConnectors.isEmpty() ) {
               Iterator i = lConnectors.iterator();
               while( i.hasNext() ) {
                  ObjectInstance lConnector = ( ObjectInstance ) i.next();
                  mServer.invoke(
                     lConnector.getObjectName(),
                     "stop",
                     new Object[] {},
                     new String[] {}
                  );
                  mServer.unregisterMBean(
                     lConnector.getObjectName()
                  );
               }
            }
         }
         catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }

   /**
    * Interface defined a Connector Tester to verify JMX Connectors
    * based on the information delivered by a JNDI server
    *
    * @author Andreas Schaefer (andreas.schaefer@madplanet.com)
    **/
   public static interface IConnectorTester {
      
      /**
       * Checks a given JNDI entry if it is a valid JMX Connector
       *
       * @param pName JNDI Name of the entry to test for
       * @param pClass Class of the entry
       *
       * @return Connector Name instance if valid otherwise null
       **/
      public ConnectorName check( String pName, Class pClass );
      
   }

   /**
    * Default implementation of the jBoss JMX Connector tester
    *
    * @author Andreas Schaefer (andreas.schaefer@madplanet.com)
    **/
   public static class JBossConnectorTester
      implements IConnectorTester
   {
      
      public ConnectorName check( String pName, Class pClass ) {
         ConnectorName lConnector = null;
         if( pName != null || pName.length() > 0 ) {
            StringTokenizer lName = new StringTokenizer( pName, ":" );
            if( lName.hasMoreTokens() && lName.nextToken().equals( "jmx" ) ) {
               if( lName.hasMoreTokens() ) {
                  String lServer = lName.nextToken();
                  if( lName.hasMoreTokens() ) {
                     lConnector = new ConnectorName( lServer, lName.nextToken(), pName );
                  }
               }
            }
         }
         return lConnector;
      }
      
   }

   /**
    * Container for a JMX Connector representation
    *
    * @author Andreas Schaefer (andreas.schaefer@madplanet.com)
    **/
   public static class ConnectorName {
      
      private String mServer;
      private String mProtocol;
      private String mJNDIName;
      
      /**
       * Creates a Connector Name instance
       *
       * @param pServer Name of the Server the JMX Connector is registered at
       * @param pProtocol Name of the Protocol the JMX Connector supports
       * @param pJNDIName JNDI Name the JMX Connector can be found
       **/
      public ConnectorName( String pServer, String pProtocol, String pJNDIName ) {
         mServer = pServer;
         mProtocol = pProtocol;
         mJNDIName = pJNDIName;
      }
      
      /**
       * @return Name of the Server the JMX Connector is registered at
       **/
      public String getServer() {
         return mServer;
      }
      
      /**
       * @return Name of the Protocol the JMX Connector supports
       **/
      public String getProtocol() {
         return mProtocol;
      }
      
      /**
       * @return JNDI Name the JMX Connector can be found
       **/
      public String getJNDIName() {
         return mJNDIName;
      }
      
      /**
       * @return Debug information about this instance
       **/
      public String toString() {
         return "ConnectorName [ server: " + mServer +
            ", protocol: " + mProtocol +
            ", JNDI name: " + mJNDIName + " ]";
      }
   }

}

