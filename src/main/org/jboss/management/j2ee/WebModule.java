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
import javax.management.j2ee.Servlet;

import java.security.InvalidParameterException;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.2 $
*/
public class WebModule
  extends J2EEModule
  implements javax.management.j2ee.WebModule
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private List mServlets = new ArrayList();

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
   public WebModule( String pName, ObjectName pApplication, String pDeploymentDescriptor, J2EEApplication[] pApplications, J2EEServer[] pServer, JVM pJVM, Servlet[] pServlets )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "WebModule", pName, pApplication, pDeploymentDescriptor );
      if( pServlets == null || pServlets.length == 0 ) {
         throw new InvalidParameterException( "Servlet list may not be null or empty" );
      }
      mServlets = new ArrayList( Arrays.asList( pServlets ) );
   }

   // -------------------------------------------------------------------------
   // WebModule Implementation
   // -------------------------------------------------------------------------  

   public Servlet[] getServlets() {
      return (Servlet[]) mServlets.toArray( new Servlet[ 0 ] );
   }
   
   public Servlet getServlet( int pIndex ) {
      if( pIndex >= 0 && pIndex < mServlets.size() )
      {
         return (Servlet) mServlets.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   public String toString() {
      return "WebModule[ " + super.toString() +
         ", Servlets: " + mServlets +
         " ]";
   }

}
