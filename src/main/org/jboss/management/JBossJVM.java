package org.jboss.management;

import javax.management.j2ee.JVM;

/**
 * @author Marc Fleury
 **/
public class JBossJVM
   extends JBossJ2EEManagedObject
   implements JVM
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String classpath;
   private int heapUsage;
   private String javaVendor;
   private String javaVersion;
   private int maxHeap;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JVM
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJVM( String pName, int pHeapUsage, int pMaxHeap, String pJavaVersion, String pJavaVendor, String pClasspath ) {
      super( pName );
      setHeapUsage( pHeapUsage );
      setMaxHeap( pMaxHeap );
      setJavaVersion( pJavaVersion );
      setJavaVendor( pJavaVendor );
      setClasspath( pClasspath );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return The value of Classpath
    **/
   public String getClasspath() {
      return classpath;
   }

   /**
    * Sets the new value of Classpath
    *
    * @param pClasspath New value of Classpath to be set
    **/
   public void setClasspath( String pClasspath ) {
       classpath = pClasspath;
   }

   /**
    * @return The value of heapUsage
    **/
   public int getHeapUsage() {
      return heapUsage;
   }

   /**
    * Sets the new value of HeapUsage
    *
    * @param pHeapUsage New value of HeapUsage to be set
    **/
   public void setHeapUsage( int pHeapUsage ) {
       heapUsage = pHeapUsage;
   }

   /**
    * @return The value of JavaVendor
    **/
   public String getJavaVendor() {
      return javaVendor;
   }

   /**
    * Sets the new value of JavaVendor
    *
    * @param pJavaVendor New value of JavaVendor to be set
    **/
   public void setJavaVendor( String pJavaVendor ) {
       javaVendor = pJavaVendor;
   }

   /**
    * @return The value of JavaVersion
    **/
   public String getJavaVersion() {
      return javaVersion;
   }

   /**
    * Sets the new value of JavaVersion
    *
    * @param pJavaVersion New value of JavaVersion to be set
    **/
   public void setJavaVersion( String pJavaVersion ) {
       javaVersion = pJavaVersion;
   }

   /**
    * @return The value of MaxHeap
    **/
   public int getMaxHeap() {
      return maxHeap;
   }

   /**
    * Sets the new value of MaxHeap
    *
    * @param pMaxHeap New value of MaxHeap to be set
    **/
   public void setMaxHeap( int pMaxHeap ) {
       maxHeap = pMaxHeap;
   }
   
   public String toString() {
      return "JBossJVM [ " +
         "heap usage: " + getHeapUsage() +
         ", max. heap: " + getMaxHeap() +
         ", java version: " + getJavaVersion() +
         ", java vendor: " + getJavaVendor() +
         ", classpath: " + getClasspath() +
         " ]";
   }

}
