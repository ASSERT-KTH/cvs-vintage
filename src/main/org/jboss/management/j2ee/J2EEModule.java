/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEServer;
import javax.management.j2ee.JVM;


/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.2 $
*/
public abstract class J2EEModule
   extends J2EEDeployedObject
   implements javax.management.j2ee.J2EEModule
{
   // Attributes ----------------------------------------------------

   private List mApplications = new ArrayList();
   private List mServers = new ArrayList();
   private JVM mJVM;

   // Constructors --------------------------------------------------

   /**
   * Constructor taking the Name of this Object
   *
   * @param pName Name to be set which must not be null
   * @param pDeploymentDescriptor
   *
   * @throws InvalidParameterException If the given Name is null
   **/
   public J2EEModule(
      String pType,
      String pName,
      ObjectName pApplication,
      String pDeploymentDescriptor
   )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pApplication, pDeploymentDescriptor );
/*
      if( pApplications == null || pApplications.length == 0 ) {
         throw new InvalidParameterException( "At least one applications must be set at the module" );
      }
      mApplications = new ArrayList( Arrays.asList( pApplications ) );
      if( pServer != null ) {
         mServers = new ArrayList( Arrays.asList( pServer ) );
      }
      mJVM = pJVM;
*/
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

   public JVM getJVM() {
      return mJVM;
   }

}
