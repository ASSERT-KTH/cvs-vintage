/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.j2ee.ConnectorModule;
import javax.management.j2ee.ResourceAdapter;

import java.security.InvalidParameterException;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossConnectorModule
  extends JBossJ2EEModule
  implements ConnectorModule
{
  // Attributes ----------------------------------------------------

  private List mResourceAdapters = new ArrayList();

  // Constructors --------------------------------------------------

  /**
  * @param pName Name of the Web Module
  * @param pAdapters List of Servlets for this Module
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  public JBossConnectorModule( String pName, ResourceAdapter[] pAdapters ) {
    super( pName );
    setResourceAdapters( pAdapters );
  }

  // Public --------------------------------------------------------

  public ResourceAdapter[] getResourceAdapters() {
    return (ResourceAdapter[]) mResourceAdapters.toArray( new ResourceAdapter[ 0 ] );
  }
  
  public ResourceAdapter getResourceAdapter( int pIndex ) {
    if( pIndex >= 0 && pIndex < mResourceAdapters.size() )
    {
      return (ResourceAdapter) mResourceAdapters.get( pIndex );
    }
    else
    {
      return null;
    }
  }
  
  public String toString() {
    return "JBossConnectorModule[ " +
      "Resource Adapters: " + mResourceAdapters +
      " ]";
  }

  // Protected -----------------------------------------------------

  /**
  * Sets a new list of Servlets
  *
  * @param pAdapters New list of Resource Adapters to be set
  *
  * @throws InvalidParameterException If given list is null or empty
  **/
  protected void setResourceAdapters( ResourceAdapter[] pAdapters )
    throws
      InvalidParameterException
  {
    if( pAdapters == null || pAdapters.length == 0 ) {
      throw new InvalidParameterException( "Resource Adapters may not be null or empty" );
    } else {
      mResourceAdapters = new ArrayList( Arrays.asList( pAdapters ) );
    }
  }
   
}
