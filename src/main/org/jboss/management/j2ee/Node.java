/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
   private List mIPAddressList = new ArrayList();
   private String mOSType;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * @param pName Name of the Node
   * @param pHardwareType Type of Hardware
   * @param pOsType Type of the OS
   *
   * @throws InvalidParameterException If list of IPAddresses or Java VMs is null or empty
   **/
   public Node( String pName, ObjectName pServer, String pHardwareType, String pOsType )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "Node", pName, pServer );
      mHardwareType = pHardwareType;
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
      return (ObjectName[]) mIPAddressList.toArray( new ObjectName[ 0 ] );
   }

   public ObjectName getIpAddress( int pIndex ) {
      if( pIndex >= 0 && pIndex < mIPAddressList.size() ) {
         return (ObjectName) mIPAddressList.get( pIndex );
      } else {
         return null;
      }
   }

   public void addChild( ObjectName pChild ) {
      Hashtable lProperties = pChild.getKeyPropertyList();
      String lType = lProperties.get( "type" ) + "";
      if( "IpAddress".equals( lType ) ) {
         mIPAddressList.add( pChild );
      }
   }
   
   public void removeChild( ObjectName pChild ) {
      Hashtable lProperties = pChild.getKeyPropertyList();
      String lType = lProperties.get( "type" ) + "";
      if( "IpAddress".equals( lType ) ) {
         mIPAddressList.remove( pChild );
      }
   }

   public String toString() {
      return "Node { " + super.toString() + " } [ " +
         "hardware type: " + getHardwareType() +
         ", os type: " + getOsType() +
         ", IP addresses: " + mIPAddressList +
         " ]";
   }
}
