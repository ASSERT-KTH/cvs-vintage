/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
* This class is the connection between the JSR and the
* JBoss specific implementation.
*
* @author <a href="mailto:andreas@jboss.org">Andreas Schafer</a>
* @version $Revision: 1.3 $
*/
public class J2EEApplication
  extends J2EEDeployedObject
  implements J2EEApplicationMBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private List mModules = new ArrayList();
   
   public static ObjectName create( MBeanServer pServer, String pName, URL pURL ) {
      String lDD = null;
      ObjectName lServer = null;
      try {
         lServer = (ObjectName) pServer.queryNames(
             new ObjectName( J2EEManagedObject.getDomainName() + ":type=J2EEServer,*" ),
             null
         ).iterator().next();
         // First get the deployement descriptor
         System.out.println( "URL: " + pURL.getFile() );
         JarFile lFile = new JarFile( pURL.getFile() );
         JarEntry lDDEntry = lFile.getJarEntry( "META-INF/application.xml" );
         if( lDDEntry != null ) {
            InputStreamReader lInput = new InputStreamReader( lFile.getInputStream( lDDEntry ) );
            StringWriter lOutput = new StringWriter();
            char[] lBuffer = new char[ 1024 ];
            int lLength = 0;
            while( ( lLength = lInput.read( lBuffer ) ) > 0 ) {
               lOutput.write( lBuffer, 0, lLength );
            }
            lDD = lOutput.toString();
         }
      }
      catch( Exception e ) {
         e.printStackTrace();
      }
      try {
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
         return null;
      }
   }

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
   
   public void addChild( ObjectName pChild ) {
      Hashtable lProperties = pChild.getKeyPropertyList();
      String lType = lProperties.get( "type" ) + "";
      if( "EJBModule".equals( lType ) ) {
         mModules.add( pChild );
      } else if( "WebModule".equals( lType ) ) {
         mModules.add( pChild );
      } else if( "ConnectorModule".equals( lType ) ) {
         mModules.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      //AS ToDo
   }

   public String toString() {
      return "J2EEApplication { " + super.toString() + " } [ " +
         "modules: " + mModules +
         " ]";
   }

}
