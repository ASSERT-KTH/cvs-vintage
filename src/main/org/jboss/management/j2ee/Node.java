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

/**
* @author Marc Fleury
**/
public class Node
   extends J2EEManagedObject
   implements NodeMBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String mHardwareType;
   private ObjectName[] mIPAddressList;
   private String mOSType;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * @param pName Name of the Node
   * @param pHardwareType Type of Hardware
   * @param pOsType Type of the OS
   * @param pIpAddresses List of IP Addresses
   *
   * @throws InvalidParameterException If list of IPAddresses or Java VMs is null or empty
   **/
   public Node( String pName, ObjectName pServer, String pHardwareType, String pOsType, ObjectName[] pIpAddresses )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "Node", pName, pServer );
      mHardwareType = pHardwareType;
/*
      if( pIpAddresses == null || pIpAddresses.length == 0 ) {
         throw new InvalidParameterException( "There must always be at least one IpAddress defined" );
      }
*/
      mIPAddressList = pIpAddresses;
      mOSType = pOsType;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String getHardwareType() {
      return mHardwareType;
   }

   public String getOsType() {
      return mOSType;
   }

   public ObjectName[] getIpAddresses() {
      return mIPAddressList;
   }

   public ObjectName getIpAddress( int pIndex ) {
      if( pIndex >= 0 && pIndex < mIPAddressList.length ) {
         return mIPAddressList[ pIndex ];
      } else {
         return null;
      }
   }

   public void addChild( ObjectName pChild ) {
/*
      Hashtable lProperties = pChild.getKeyPropertyList();
      String lType = lProperties.get( "type" ) + "";
      if( "J2EEApplication".equals( lType ) ) {
         m
         mApplications.add( pChild );
      } else if( "J2EEDeployments".equals( lType ) ) {
         mDeployments.add( pChild );
      } else if( "J2EEServer".equals( lType ) ) {
         mServers.add( pChild );
      }
*/
   }
   
   public void removeChild( ObjectName pChild ) {
      //AS ToDo
   }

   public String toString() {
      return "Node [ " +
         "name: " + getName() +
         ", hardware type: " + getHardwareType() +
         ", os type: " + getOsType() +
         ", IP addresses: " + getIpAddresses() +
         " ]";
   }
}
