/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.management.j2ee.EJB;
import javax.management.j2ee.J2EEApplication;
import javax.management.j2ee.J2EEServer;
import javax.management.j2ee.JVM;

import java.security.InvalidParameterException;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.EjbModule EjbModule}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.8 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and implementing of the
 *      the create() and destroy() helper method
 * </ul>
 **/
public class EjbModule
  extends J2EEModule
  implements EjbModuleMBean
{

   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   private List mEJBs = new ArrayList();

   // Static --------------------------------------------------------
   
   public static ObjectName create( MBeanServer pServer, String pApplicationName, String pName, URL pURL ) {
      Logger lLog = Logger.getLogger( EjbModule.class );
      String lDD = null;
      ObjectName lApplication = null;
      try {
         ObjectName lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
         String lServerName = lServer.getKeyPropertyList().get( "type" ) + "=" +
                              lServer.getKeyPropertyList().get( "name" );
         lLog.debug( "EjbModule.create(), server name: " + lServerName );
         lApplication = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEApplication" +
                ",name=" + pApplicationName + "," + lServerName + ",*"
             ),
             null
         ).iterator().next();
         // First get the deployement descriptor
         lDD = J2EEDeployedObject.getDeploymentDescriptor( pURL, J2EEDeployedObject.EJB );
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 EjbModule: " + pApplicationName, e );
         return null;
      }
      try {
         // Now create the J2EEApplication
         lLog.debug(
            "Create EJB-Module, name: " + pName +
            ", application: " + lApplication +
            ", dd: " + lDD
         );
         return pServer.createMBean(
            "org.jboss.management.j2ee.EjbModule",
            null,
            new Object[] {
               pName,
               lApplication,
               lDD
            },
            new String[] {
               String.class.getName(),
               ObjectName.class.getName(),
               String.class.getName()
            }
         ).getObjectName();
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not create JSR-77 EjbModule: " + pApplicationName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pModuleName ) {
      Logger lLog = Logger.getLogger( EjbModule.class );
      try {
         // Now remove the EjbModule
         pServer.unregisterMBean( new ObjectName( pModuleName ) );
      }
      catch( Exception e ) {
//AS         lLog.error( "Could not destory JSR-77 EjbModule: " + pModuleName, e );
      }
   }
   
   // Constructors --------------------------------------------------
   
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
      super( "EJBModule", pName, pApplication, pDeploymentDescriptor );
   }

   // Public --------------------------------------------------------
   
   // EJBModule implementation --------------------------------------
   
   public ObjectName[] getEjbs() {
      return (ObjectName[]) mEJBs.toArray( new ObjectName[ 0 ] );
   }
   
   public ObjectName getEjb( int pIndex ) {
      if( pIndex >= 0 && pIndex < mEJBs.size() )
      {
         return (ObjectName) mEJBs.get( pIndex );
      }
      else
      {
         return null;
      }
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "EntityBean".equals( lType ) ||
         "StatelessSessionBean".equals( lType ) ||
         "StatefulSessionBean".equals( lType ) ||
         "MessageDrivenBean".equals( lType )
      ) {
         mEJBs.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( "EntityBean".equals( lType ) ||
         "StatelessSessionBean".equals( lType ) ||
         "StatefulSessionBean".equals( lType ) ||
         "MessageDrivenBean".equals( lType )
      ) {
         mEJBs.remove( pChild );
      }
   }

   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "EJBModule[ " + super.toString() +
         "EJBs: " + mEJBs +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
