/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.security.InvalidParameterException;

import javax.management.ObjectName;

import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEResource;
import javax.management.j2ee.J2EEServer;
import javax.management.j2ee.Node;
import javax.management.j2ee.Port;

import org.jboss.jmx.interfaces.JMXConnector;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossJ2EEServer
  extends JBossJ2EEManagedObject
  implements J2EEServer
{
  // Attributes ----------------------------------------------------

  private List mApplications = new ArrayList();
  private List mResources = new ArrayList();
  private List mNodes = new ArrayList();
  private List mPorts = new ArrayList();
   
  private JMXConnector mServerConnector;

  // Constructors --------------------------------------------------

  /**
  * @param pName Name of the Server
  **/
  public JBossJ2EEServer( String pName, JMXConnector pConnector ) {
    super( pName );
    mServerConnector = pConnector;
  }

  /**
  * @param pApplications List of applications
  * @param pNodes List of nodes supported
  * @param pPorts List of ports
  * @param pResources List of resources
  *
  * @throws InvalidParameterException If list of nodes or ports was null or empty
  **/
  public void init( J2EEApplication[] pApplications, Node[] pNodes, Port[] pPorts, J2EEResource[] pResources ) {
    setApplications( pApplications );
    setNodes( pNodes );
    setPorts( pPorts );
    setResources( pResources );
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

  public Port[] getPorts() {
    return (Port[]) mPorts.toArray( new Port[ 0 ] );
  }

  public Port getPort( int pIndex ) {
    if( pIndex >= 0 && pIndex < mPorts.size() )
    {
      return (Port) mPorts.get( pIndex );
    }
    else
    {
      return null;
    }
  }

  public Node[] getNodes() {
    return (Node[]) mNodes.toArray( new Node[ 0 ] );
  }

  public Node getNode( int pIndex ) {
    if( pIndex >= 0 && pIndex < mNodes.size() )
    {
      return (Node) mNodes.get( pIndex );
    }
    else
    {
      return null;
    }
  }

  public J2EEResource[] getResources() {
    return (J2EEResource[]) mApplications.toArray( new J2EEResource[ 0 ] );
  }

  public J2EEResource getResource( int pIndex ) {
    if( pIndex >= 0 && pIndex < mResources.size() )
    {
      return (J2EEResource) mResources.get( pIndex );
    }
    else
    {
      return null;
    }
  }

  public void refresh() {
    try {
      // Create the Applications, Resources and Nodes
      ObjectName lCollector = new ObjectName( "Management", "service", "Collector" );
      // Because of unsolved timing issues call refresh first
      mServerConnector.invoke(
        lCollector,
        "refreshNow",
        new Object[] {},
        new String[] {}
      );
      List lApplications = (List) mServerConnector.getAttribute(
        lCollector,
        "Applications"
      );
      mApplications = lApplications;
      List lResources = (List) mServerConnector.getAttribute(
        lCollector,
        "Resources"
      );
      mResources = lResources;
      List lNodes = (List) mServerConnector.getAttribute(
        lCollector,
        "Nodes"
      );
      mNodes = lNodes;
      
      System.out.println( "Refresh() created: " + this );
    }
    catch( Exception e ) {
      e.printStackTrace();
    }
  }
  
  public String toString() {
    return "JBossJ2EEServer[ " +
      "applications: " + mApplications +
      ", resources: " + mResources +
      ", ports: " + Arrays.asList( getPorts() ) +
      ", nodes: " + Arrays.asList( getNodes() ) +
      " ]";
  }

  // Protected -----------------------------------------------------

  /**
  * Sets a new list of Applications
  *
  * @param pApplications New list of Applications to be set. If null
  *                      then the list will be set empty
  **/
  protected void setApplications( J2EEApplication[] pApplications ) {
    if( pApplications == null ) {
      mApplications = new ArrayList();
    } else {
      mApplications = new ArrayList( Arrays.asList( pApplications ) );
    }
  }

  /**
  * Sets a new list of Ports
  *
  * @param pPorts New list of Ports to be set
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  protected void setPorts( Port[] pPorts ) {
    if( pPorts == null || pPorts.length == 0 ) {
      throw new InvalidParameterException( "There must always be at least one Port defined" );
    }
    mPorts = new ArrayList( Arrays.asList( pPorts ) );
  }

  /**
  * Sets a new list of Nodes
  *
  * @param pNodes New list of Nodes to be set
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  protected void setNodes( Node[] pNodes ) {
    if( pNodes == null || pNodes.length == 0 ) {
      throw new InvalidParameterException( "There must always be at least one Node defined" );
    }
    mNodes = new ArrayList( Arrays.asList( pNodes ) );
  }

  /**
  * Sets a new list of Resources
  *
  * @param pResources New list of Resources to be set. If null
  *                   then the list will be set empty
  **/
  protected void setResources( J2EEResource[] pResources ) {
    if( pResources == null ) {
      mResources = new ArrayList();
    } else {
      mResources = new ArrayList( Arrays.asList( pResources ) );
    }
  }

}
