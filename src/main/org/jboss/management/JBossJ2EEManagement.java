/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.RuntimeMBeanException;

import org.jboss.jmx.client.ConnectorFactoryImpl;
import org.jboss.jmx.interfaces.JMXConnector;
import org.jboss.util.ServiceMBean;

import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEDeployer;
import javax.management.j2ee.J2EEManagement;
import javax.management.j2ee.J2EEServer;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossJ2EEManagement
  extends JBossJ2EEManagedObject
  implements J2EEManagement
{
  // Attributes ----------------------------------------------------

  private MBeanServer mLocalServer;
  private Hashtable mJNDIProperties;

  private List mApplications = new ArrayList();
  private J2EEDeployer deployer;
  private List mServers = new ArrayList();

  // Constructors --------------------------------------------------

  /**
  * @param pName Name of the J2EEManagement
  * @param pJNDIProperties JNDI Properties
  **/
  public JBossJ2EEManagement( String pName, MBeanServer pServer, Hashtable pJNDIProperties )
    throws
      InvalidParameterException
  {
     super( pName );
     if( pServer == null )
     {
        throw new InvalidParameterException( "MBeanServer must be specified" );
     }
     mLocalServer = pServer;
     mJNDIProperties = pJNDIProperties;
  }

  // Public --------------------------------------------------------

  public J2EEApplication[] getApplications() {
    return (J2EEApplication[]) mApplications.toArray( new J2EEApplication[ 0 ] );
  }

  public J2EEApplication getApplication( int pIndex ) {
    if( pIndex >= 0 && pIndex < mApplications.size() )
    {
      return (J2EEApplication) mApplications.get( pIndex );
    }
    else
    {
      return null;
    }
  }

  public J2EEDeployer getDeployer() {
    return deployer;
  }

  public J2EEServer[] getServers() {
    return (J2EEServer[]) mServers.toArray( new J2EEServer[ 0 ] );
  }

  public J2EEServer getServer( int pIndex ) {
    if( pIndex >= 0 && pIndex < mServers.size() )
    {
      return (J2EEServer) mServers.get( pIndex );
    }
    else
    {
      return null;
    }
  }

  // MBean Methods --------------------------------------------------------

  public void refresh() {
    try {
      Iterator i = getConnectors();
      // List containing all the server
      mServers = new ArrayList();
      // Loop through the servers, create the Management Representation and saved them
      while( i.hasNext() ) {
        JMXConnector lConnector = (JMXConnector) i.next();
        mServers.add(
           new JBossJ2EEServer( "J2EEServer", lConnector )
        );
      }
      // Make the initial refresh
      i = mServers.iterator();
      while( i.hasNext() ) {
        JBossJ2EEServer lServer = (JBossJ2EEServer) i.next();
        lServer.refresh();
        mApplications.addAll(
          Arrays.asList( lServer.getApplications() )
        );
      }
    }
    catch( RuntimeMBeanException rme ) {
      rme.printStackTrace();
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }

  /**
  * Looks up all the servers having a JBoss instance running
  * through the given JNDI server
  *
  * @return Iterator on the list of active JMX Connections to a MBeanServer
  *         with a running JBoss instance (element of type JMXConnector).
  **/
  public Iterator getConnectors() {
    List lConnectors = new ArrayList();
    try {
      ObjectInstance lFactory = null;
      ObjectName lFactoryName = new ObjectName( "DefaultDomain", "name", "ConnectorFactory" );
      if( mJNDIProperties == null ) {
        // Create a plain InitialContext and take the Properties from there
        Context lPlain = new InitialContext();
        mJNDIProperties = lPlain.getEnvironment();
      }
      // Check if the Connector factory already exists otherwise create it
      if( mLocalServer.isRegistered( lFactoryName ) ) {
        lFactory = mLocalServer.getObjectInstance( lFactoryName );
      }
      else
      {
        lFactory = mLocalServer.createMBean(
          "org.jboss.jmx.client.ConnectorFactoryService",
          lFactoryName
        );
      }
      // Start the Connector Factory
      mLocalServer.invoke(
        lFactoryName,
        "init",
        new Object[] {},
        new String[] {}
      );
      mLocalServer.invoke(
        lFactoryName,
        "start",
        new Object[] {},
        new String[] {}
      );
      // Lookup all available remote JMX Servers
      ConnectorFactoryImpl.JBossConnectorTester lTester = new ConnectorFactoryImpl.JBossConnectorTester();
      Iterator i = (Iterator) mLocalServer.invoke(
        lFactoryName,
        "getConnectors",
        new Object[] {
          mJNDIProperties,
          lTester
        },
        new String[] {
           Hashtable.class.getName(),
           ConnectorFactoryImpl.IConnectorTester.class.getName()
        }
      );
      // Create for each Remote Connector found a JMX Connection to the remote site
      while( i.hasNext() ) {
        ConnectorFactoryImpl.ConnectorName lName = (ConnectorFactoryImpl.ConnectorName) i.next();
        try {
          lConnectors.add(
            mLocalServer.invoke(
              lFactoryName,
              "createConnection",
              new Object[] {
                lName
              },
              new String[] {
                lName.getClass().getName()
              }
            )
          );
        }
        catch( Exception e ) {
          e.printStackTrace();
          // Keep the loop up and running
        }
      }
      // No check all the connections and disconnect all not containing a JBoss instance
      // and remove them from the list
      i = lConnectors.iterator();
      ObjectName lCollector = new ObjectName( "Management", "service", "Collector" );
      while( i.hasNext() ) {
        JMXConnector lConnector = (JMXConnector) i.next();
        if( !lConnector.isRegistered( lCollector ) ) {
          mLocalServer.invoke(
            lFactoryName,
            "removeConnection",
            new Object[] {
              lConnector
            },
            new String[] {
              lConnector.getClass().getName()
            }
          );
          i.remove();
        }
      }
      // Now we have a list of connectors with at least one JBoss instance running there
      // Thus we can get the servers informations
      System.out.println( "Actual JBoss Servers found: " + lConnectors );
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
    return lConnectors.iterator();
  }
}
