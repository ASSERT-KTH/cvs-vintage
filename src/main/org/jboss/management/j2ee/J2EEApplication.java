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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:andreas@jboss.org">Andreas Schafer</a>
* @version $Revision: 1.2 $
*/
public class J2EEApplication
  extends J2EEDeployedObject
  implements J2EEApplicationMBean
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
   public J2EEApplication( String pName, ObjectName pServer, String pDeploymentDescriptor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "J2EEApplication", pName, pServer, pDeploymentDescriptor );
   }
   
   // Public --------------------------------------------------------

   public ObjectName[] getModules() {
      return (ObjectName[]) mModules.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getModule( int pIndex ) {
      if( pIndex >= 0 && pIndex < mModules.size() )
      {
         return (ObjectName) mModules.get( pIndex );
      }
      return null;
   }
   
   public String toString() {
      return "J2EEApplication { " + super.toString() + " } [ " +
         "modules: " + mModules +
         " ]";
   }

}
