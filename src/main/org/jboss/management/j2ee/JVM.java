/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JVM JVM}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.5 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public class JVM
   extends J2EEManagedObject
   implements JVMMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "JVM";
   
   // Attributes ----------------------------------------------------
   
   private String mJavaVendor;
   private String mJavaVersion;
   private String mNode;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * @param pName Name of the JVM
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JVM( String pName, ObjectName pServer, String pJavaVersion, String pJavaVendor, String pNode )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pServer );
      mJavaVendor = pJavaVendor;
      mJavaVersion = pJavaVersion;
      mNode = pNode;
   }
   
   // Public --------------------------------------------------------
   
   /**
    * @jmx:managed-attribute
    **/
   public String getJavaVendor() {
      return mJavaVendor;
   }
   
   /**
    * @jmx:managed-attribute
    **/
   public String getJavaVersion() {
      return mJavaVersion;
   }
   
   /**
    * @jmx:managed-attribute
    **/
   public String getNode() {
      return mNode;
   }
   
   // java.lang.Object overrides ------------------------------------
   
   public String toString() {
      return "JVM [ " +
         ", java vendor: " + getJavaVendor() +
         ", java version: " + getJavaVersion() +
         ", node: " + getNode() +
         " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
