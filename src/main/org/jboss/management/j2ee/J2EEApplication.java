/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEApplication J2EEApplication}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.11 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and implementing of the
 *      the destroy() helper method
 * </ul>
 *
 * @todo When all components of a J2EEApplication is state manageable
 *       this have to be too !!
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEDeployedObjectMBean"
 **/
public class J2EEApplication
   extends J2EEDeployedObject
   implements J2EEApplicationMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "J2EEApplication";
   
   // Attributes ----------------------------------------------------
   
   private List mModules = new ArrayList();
   
   // Static --------------------------------------------------------
   
   public static ObjectName create( MBeanServer pServer, String pName, URL pURL ) {
      Logger lLog = Logger.getLogger( J2EEApplication.class );
      String lDD = null;
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + J2EEServer.J2EE_TYPE + "," +
               "*"
             ),
             null
         ).iterator().next();
         // First get the deployement descriptor
         lDD = J2EEDeployedObject.getDeploymentDescriptor( pURL, J2EEDeployedObject.APPLICATION );
      }
      catch( Exception e ) {
         lLog.error( "Could not create JSR-77 J2EEApplication: " + pName, e );
         return null;
      }
      try {
         lLog.debug( "Create J2EE Application, name: " + pName +
            ", server: " + lServer
         );
         // Now create the J2EEApplication
         return pServer.createMBean(
            "org.jboss.management.j2ee.J2EEApplication",
            null,
            new Object[] {
               pName,
               lServer,
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
         lLog.error( "Could not create JSR-77 J2EEApplication: " + pName, e );
         return null;
      }
   }
   
   public static void destroy( MBeanServer pServer, String pName ) {
      Logger lLog = Logger.getLogger( J2EEApplication.class );
      try {
         ObjectName lApplication;
         if( pName.indexOf( J2EEManagedObject.TYPE + "=" + J2EEApplication.J2EE_TYPE ) >= 0 ) {
            lApplication = new ObjectName( pName );
         } else {
            // Find the Object to be destroyed
            ObjectName lSearch = new ObjectName(
               J2EEManagedObject.getDomainName() + ":" +
               J2EEManagedObject.TYPE + "=" + J2EE_TYPE + ",name=" + pName + "," +
               "*"
            );
            lApplication = (ObjectName) pServer.queryNames(
               lSearch,
               null
            ).iterator().next();
         }
         // Now remove the J2EEApplication
         pServer.unregisterMBean( lApplication );
      }
      catch( Exception e ) {
         lLog.error( "Could not destroy JSR-77 J2EEApplication: " + pName, e );
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
   public J2EEApplication( String pName, ObjectName pServer, String pDeploymentDescriptor )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pServer, pDeploymentDescriptor );
   }
   
   // Public --------------------------------------------------------
   
   // J2EEApplication implementation --------------------------------
   
   /**
    * @jmx:managed-attribute
    **/
   public ObjectName[] getModules() {
      return (ObjectName[]) mModules.toArray( new ObjectName[ 0 ] );
   }
   
   /**
    * @jmx:managed-operation
    **/
   public ObjectName getModule( int pIndex ) {
      if( pIndex >= 0 && pIndex < mModules.size() )
      {
         return (ObjectName) mModules.get( pIndex );
      }
      return null;
   }
   
   // J2EEManagedObjectMBean implementation -------------------------
   
   public void addChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( EjbModule.J2EE_TYPE.equals( lType ) ) {
         mModules.add( pChild );
      } else if( WebModule.J2EE_TYPE.equals( lType ) ) {
         mModules.add( pChild );
      } else if( ResourceAdapterModule.J2EE_TYPE.equals( lType ) ) {
         mModules.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      String lType = J2EEManagedObject.getType( pChild );
      if( EjbModule.J2EE_TYPE.equals( lType ) ) {
         mModules.remove( pChild );
      } else if( WebModule.J2EE_TYPE.equals( lType ) ) {
         mModules.remove( pChild );
      } else if( ResourceAdapterModule.J2EE_TYPE.equals( lType ) ) {
         mModules.remove( pChild );
      }
   }

   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "J2EEApplication { " + super.toString() + " } [ " +
         "modules: " + mModules +
         " ]";
   }

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
