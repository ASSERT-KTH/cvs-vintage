/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.Node;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.JVM JVM}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 **/
public class JVM
   extends J2EEManagedObject
   implements javax.management.j2ee.JVM
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mClasspath;
   private String mJavaVendor;
   private String mJavaVersion;
   private Node mNode;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * @param pName Name of the JVM
   *
   * @throws InvalidParameterException If list of nodes or ports was null or empty
   **/
   public JVM( String pName, ObjectName pNode, String pClasspath, String pJavaVendor, String pJavaVersion )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JVM", pName, pNode );
      mClasspath = pClasspath;
      mJavaVendor = pJavaVendor;
      mJavaVersion = pJavaVersion;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String getClasspath() {
      return mClasspath;
   }

   public String getJavaVendor() {
      return mJavaVendor;
   }

   public String getJavaVersion() {
      return mJavaVersion;
   }

   public Node getNode() {
      return mNode;
   }

   public String toString() {
      return "JBossJVM [ " +
         ", classpath: " + getClasspath() +
         ", java vendor: " + getJavaVendor() +
         ", java version: " + getJavaVersion() +
         ", node: " + getNode() +
         " ]";
   }

}
