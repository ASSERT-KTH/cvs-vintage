/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.j2ee.J2EEServer;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
*/
public class J2EEApplication
  extends J2EEDeployedObject
  implements javax.management.j2ee.J2EEApplication
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private List mModules = new ArrayList();

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   * @param pDeploymentDescriptor
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public J2EEApplication( String pName, String pDeploymentDescriptor, J2EEModule[] pModules ) {
      super( pName, pDeploymentDescriptor );
      if( pModules == null || pModules.length == 0 ) {
         throw new InvalidParameterException( "At least one modules must be added to the application" );
      }
      mModules = new ArrayList( Arrays.asList( pModules ) );
   }
   
   // Public --------------------------------------------------------

   public javax.management.j2ee.J2EEModule[] getModules() {
      return (J2EEModule[]) mModules.toArray( new J2EEModule[ 0 ] );
   }
   
   public javax.management.j2ee.J2EEModule getModule( int pIndex ) {
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
      return "J2EEApplication [ " +
         ", Modules: " + mModules +
         " ]";
   }

}
