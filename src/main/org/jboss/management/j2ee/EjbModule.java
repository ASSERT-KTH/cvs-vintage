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

import javax.management.j2ee.EJB;
import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEServer;
import javax.management.j2ee.JVM;

import java.security.InvalidParameterException;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.2 $
*/
public class EjbModule
  extends J2EEModule
  implements javax.management.j2ee.EjbModule
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private List mEJBs = new ArrayList();

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
   public EjbModule( String pName, ObjectName pApplication, String pDeploymentDescriptor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "EjbModule", pName, pApplication, pDeploymentDescriptor );
/*
      if( pEJBs == null || pEJBs.length == 0 ) {
         throw new InvalidParameterException( "EJB list may not be null or empty" );
      }
      mEJBs = new ArrayList( Arrays.asList( pEJBs ) );
*/
   }

   // -------------------------------------------------------------------------
   // EjbModule Implementation
   // -------------------------------------------------------------------------  

   public EJB[] getEjbs() {
      return (EJB[]) mEJBs.toArray( new EJB[ 0 ] );
   }
   
   public EJB getEjb( int pIndex ) {
      if( pIndex >= 0 && pIndex < mEJBs.size() )
      {
         return (EJB) mEJBs.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   public String toString() {
      return "EJBModule[ " + super.toString() +
         "EJBs: " + mEJBs +
         " ]";
   }

}
