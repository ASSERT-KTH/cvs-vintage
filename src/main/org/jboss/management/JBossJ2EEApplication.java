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

import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEModule;
import javax.management.j2ee.J2EEServer;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision$
*/
public class JBossJ2EEApplication
  extends JBossJ2EEModule
  implements J2EEApplication
{
  // Attributes ----------------------------------------------------

   private List mModules = new ArrayList();

  // Constructors --------------------------------------------------

   /**
    * @param pName Name of the J2EEApplication
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJ2EEApplication( String pName, J2EEServer[] pServers, J2EEModule[] pModules ) {
      super( pName );
      setServers( pServers );
      setModules( pModules );
   }

  // Public --------------------------------------------------------

  public J2EEModule[] getModules() {
    return (J2EEModule[]) mModules.toArray( new J2EEModule[ 0 ] );
  }
  
  public J2EEModule getModule( int pIndex ) {
    if( pIndex >= 0 && pIndex < mModules.size() )
    {
      return (J2EEModule) mModules.get( pIndex );
    }
    else
    {
      return null;
    }
  }
  
  public String toString() {
    return "JBossJ2EEApplication [ " +
      "Servers: " + Arrays.asList( getServers() ) +
      ", Modules: " + Arrays.asList( getModules() ) +
      " ]";
  }

  // Protected -----------------------------------------------------

  /**
  * Sets a new list of Modules
  *
  * @param pModules New list of Modules to be set. If null
  *                 then the list will be set empty
  **/
  public void setModules( J2EEModule[] pModules ) {
    if( pModules == null ) {
      mModules = new ArrayList();
    } else {
      mModules = new ArrayList( Arrays.asList( pModules ) );
    }
  }

}
