/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEServer;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossJ2EEModule
   extends JBossJ2EEManagedObject
{
  // Attributes ----------------------------------------------------

  private J2EEApplication mApplication;
  private String mDeploymentDescriptor;
  private List mServers = new ArrayList();

  // Constructors --------------------------------------------------

  /**
  * @param pName Name of the J2EEModule
  *
  * @throws InvalidParameterException If list of nodes or ports was null or empty
  **/
  public JBossJ2EEModule( String pName ) {
    super( pName );
  }

  // Public --------------------------------------------------------

  public J2EEApplication getApplication() {
    return mApplication;
  }
  
  public String getDeploymentDescriptor() {
    return mDeploymentDescriptor;
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

  // Protected -----------------------------------------------------

  /**
  * Sets a Application
  *
  * @param pApplication New Application to be set
  **/
  protected void setApplication( J2EEApplication pApplication ) {
    mApplication = pApplication;
  }
  
  /**
  * Sets a Deployment Descriptor
  *
  * @param pDeploymentDescriptor New Deployment Descriptor to be set
  **/
  public void setDeploymentDescriptor( String pDeploymentDescriptor ) {
    mDeploymentDescriptor = pDeploymentDescriptor;
  }
  
  /**
  * Sets a new list of Servers
  *
  * @param pServers New list of Servers to be set. If null
  *                 then the list will be set empty
  **/
  public void setServers( J2EEServer[] pServers ) {
    if( pServers == null ) {
      mServers = new ArrayList();
    } else {
      mServers = new ArrayList( Arrays.asList( pServers ) );
    }
  }

}
