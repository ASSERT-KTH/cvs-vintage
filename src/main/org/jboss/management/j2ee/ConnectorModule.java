/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEServer;
import javax.management.j2ee.JVM;
import javax.management.j2ee.ResourceAdapter;

import java.security.InvalidParameterException;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.3 $
*/
public class ConnectorModule
  extends J2EEModule
  implements javax.management.j2ee.ConnectorModule
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private List mResourceAdapters = new ArrayList();

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
   public ConnectorModule( String pName, ObjectName pApplication, String pDeploymentDescriptor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "ConnectorModule", pName, pApplication, pDeploymentDescriptor );
/*
      if( pAdapters == null || pAdapters.length == 0 ) {
         throw new InvalidParameterException( "Resource Adapters may not be null or empty" );
      }
      mResourceAdapters = new ArrayList( Arrays.asList( pAdapters ) );
*/
   }

   // -------------------------------------------------------------------------
   // ConnectorModule Implementation
   // -------------------------------------------------------------------------  

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
      return "ConnectorModule[ " + super.toString() +
         ", Resource Adapters: " + mResourceAdapters +
         " ]";
   }

}
