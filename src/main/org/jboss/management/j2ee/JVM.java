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
* @author Marc Fleury
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
