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

import java.security.InvalidParameterException;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.AppClientModule AppClientModule}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020307 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEModuleMBean"
 **/
public class AppClientModule
  extends J2EEModule
  implements AppClientModuleMBean
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
   public AppClientModule( String pName, ObjectName pApplication, ObjectName[] pJVMs, String pDeploymentDescriptor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "AppClientModule", pName, pApplication, pJVMs, pDeploymentDescriptor );
   }

   // -------------------------------------------------------------------------
   // AppClientModule Implementation
   // -------------------------------------------------------------------------  
   
   public String toString() {
      return "AppClientModule[ " + super.toString() +
         " ]";
   }

}
