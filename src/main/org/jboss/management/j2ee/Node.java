/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.security.InvalidParameterException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.IpAddress;
import javax.management.j2ee.JVM;

/**
* @author Marc Fleury
**/
public class Node
   extends J2EEManagedObject
   implements javax.management.j2ee.Node
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
   public Node( String pName, ObjectName pServer, String pHardwareType, String pOsType, JVM[] pJavaVMs, IpAddress[] pIpAddresses )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "Node", pName, pServer );
      hardwareType = pHardwareType;
      if( pIpAddresses == null || pIpAddresses.length == 0 ) {
         throw new InvalidParameterException( "There must always be at least one IpAddress defined" );
      }
      ipAddresses = pIpAddresses;
       osType = pOsType;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String getHardwareType() {
      return hardwareType;
   }

   public IpAddress[] getIpAddresses() {
      return ipAddresses;
   }

   public IpAddress getIpAddress( int pIndex ) {
      if( pIndex >= 0 && pIndex < ipAddresses.length ) {
         return ipAddresses[ pIndex ];
      } else {
         return null;
      }
   }

   public String getOsType() {
      return osType;
   }

   public String toString() {
      return "Node [ " +
         "name: " + getName() +
         ", hardware type: " + getHardwareType() +
         ", os type: " + getOsType() +
         // Do not change to java.util.Arrays.asList() because this
         // would create an endless loop !!
         ", IP addresses: " + getIpAddresses() +
         " ]";
   }
}
