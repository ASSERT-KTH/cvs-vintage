package org.jboss.management;

import java.security.InvalidParameterException;

import javax.management.j2ee.IpAddress;
import javax.management.j2ee.JVM;
import javax.management.j2ee.Node;

/**
 * @author Marc Fleury
 **/
public class JBossNode
   extends JBossJ2EEManagedObject
   implements Node
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String hardwareType;
   private IpAddress[] ipAddresses;
   private JVM[] javaVMs;
   private String osType;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the Node
    * @param pHardwareType Type of Hardware
    * @param pOsType Type of the OS
    * @param pJavaVMs List of Java VMs
    * @param pIpAddresses List of IP Addresses
    *
    * @throws InvalidParameterException If list of IPAddresses or Java VMs is null or empty
    **/
   public JBossNode( String pName, String pHardwareType, String pOsType, JVM[] pJavaVMs, IpAddress[] pIpAddresses ) {
      super( pName );
      setHardwareType( pHardwareType );
      setIpAddresses( pIpAddresses );
      setJavaVMs( pJavaVMs );
      setOsType( pOsType );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return The value of HardwareType
    **/
   public String getHardwareType() {
      return hardwareType;
   }

   /**
    * Sets the new value of HardwareType
    *
    * @param pHardwareType New value of HardwareType to be set
    **/
   public void setHardwareType( String pHardwareType ) {
       hardwareType = pHardwareType;
   }

   /**
    * @return The actual list of IpAddresses which is never empty
    **/
   public IpAddress[] getIpAddresses() {
      return ipAddresses;
   }

   /**
    * Sets a new list of IpAddresss
    *
    * @param pIpAddresss New list of IpAddresss to be set
    *
    * @throws InvalidParameterException If given list is null or empty
    **/
   public void setIpAddresses( IpAddress[] pIpAddresses ) {
      if( pIpAddresses == null || pIpAddresses.length == 0 ) {
         throw new InvalidParameterException( "There must always be at least one IpAddress defined" );
      }
      ipAddresses = pIpAddresses;
   }

   /**
    * @return The actual list of JavaVMs which is never empty
    **/
   public JVM[] getJavaVMs() {
      return javaVMs;
   }

   /**
    * Sets a new list of JavaVMs
    *
    * @param pJavaVMs New list of JavaVMs to be set
    *
    * @throws InvalidParameterException If given list is null or empty
    **/
   public void setJavaVMs( JVM[] pJavaVMs ) {
      if( pJavaVMs == null || pJavaVMs.length == 0 ) {
         throw new InvalidParameterException( "There must always be at least one JavaVM defined" );
      }
      javaVMs = pJavaVMs;
   }

   /**
    * @return The value of OsType
    **/
   public String getOsType() {
      return osType;
   }

   /**
    * Sets the new value of OsType
    *
    * @param pOsType New value of OsType to be set
    **/
   public void setOsType( String pOsType ) {
       osType = pOsType;
   }

   public String toString() {
      return "JBossNode [ " +
         "name: " + getName() +
         ", hardware type: " + getHardwareType() +
         ", os type: " + getOsType() +
         ", JVMs: " + java.util.Arrays.asList( getJavaVMs() ) +
         // Do not change to java.util.Arrays.asList() because this
         // would create an endless loop !!
         ", IP addresses: " + getIpAddresses() +
         " ]";
   }
}
