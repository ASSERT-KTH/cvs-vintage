/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

// import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
// import java.util.jar.JarEntry;
// import java.util.jar.JarFile;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.jboss.logging.Logger;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEDeployedObject J2EEDeployedObject}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.6 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011123 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines and adding the static method
 *      to load a Deployment Descriptor
 * </ul>
 **/
public abstract class J2EEDeployedObject
   extends J2EEManagedObject
   implements javax.management.j2ee.J2EEDeployedObject
{
   // Constants -----------------------------------------------------
   
   public static final int APPLICATION = 0;
   public static final int WEB = 1;
   public static final int EJB = 2;
   public static final int RAR = 3;
   
   private static final String[] sDescriptors = new String[] {
                                                   "META-INF/application.xml",
                                                   "WEB-INF/web.xml",
                                                   "META-INF/ejb-jar.xml",
                                                   "META-INF/??AS??.xml"
                                                };
   
   // Attributes ----------------------------------------------------

   /** Class logger. */
   private static final Logger log =
      Logger.getLogger(J2EEDeployedObject.class);

   private String mDeploymentDescriptor;

   // Static --------------------------------------------------------
   
   public static String getDeploymentDescriptor( URL pJarUrl, int pType ) {
      if( pJarUrl == null ) {
         // Return if the given URL is null
         return "";
      }
      String lDD = null;
      InputStreamReader lInput = null;
      StringWriter lOutput = null;
      try {
         // First get the deployement descriptor
         log.debug( "File: " + pJarUrl + ", descriptor: " + sDescriptors[ pType ] );
         ClassLoader localCl = new URLClassLoader( new URL[] { pJarUrl } );
         lInput = new InputStreamReader( localCl.getResourceAsStream( sDescriptors[ pType ] ) );
         lOutput = new StringWriter();
         char[] lBuffer = new char[ 1024 ];
         int lLength = 0;
         while( ( lLength = lInput.read( lBuffer ) ) > 0 ) {
            lOutput.write( lBuffer, 0, lLength );
         }
         lDD = lOutput.toString();
      }
      catch( Exception e ) {
         log.error("failed to get deployment descriptor", e);
      }
      finally {
         if( lInput != null ) {
            try {
               lInput.close();
            }
            catch( Exception e ) {
            }
         }
         if( lOutput != null ) {
            try {
               lOutput.close();
            }
            catch( Exception e ) {
            }
         }
      }
      return lDD;
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
   public J2EEDeployedObject(
      String pType,
      String pName,
      ObjectName pParent,
      String pDeploymentDescriptor
   )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pParent );
      mDeploymentDescriptor = pDeploymentDescriptor;
   }
   
   // Public --------------------------------------------------------
   
   // javax.management.j2ee.J2EEDeployedObject implementation -------
   
   public String getDeploymentDescriptor() {
      return mDeploymentDescriptor;
   }
   
   public ObjectName getServer1() {
      //AS ToDo: Need to be implemented
      return null;
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "J2EEDeployedObject { " + super.toString() + " } [ " +
         "deployment descriptor: " + mDeploymentDescriptor +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
