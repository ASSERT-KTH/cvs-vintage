/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import java.security.InvalidParameterException;
import java.util.Arrays;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.IpAddress;

public class Port
   extends J2EEManagedObject
   implements javax.management.j2ee.Port
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private IpAddress[] mIpAddresses;
   private int mPort;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public Port( String pName, ObjectName pIpAddress, int pPort, IpAddress[] pIpAddresses )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "IpAddress", pName, pIpAddress );
      mPort = pPort;
      if( pIpAddresses == null || pIpAddresses.length == 0 ) {
         throw new InvalidParameterException( "At least one IP Addresses must be defined" );
      }
      mIpAddresses = pIpAddresses;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public IpAddress[] getIpAddresses() {
      return mIpAddresses;
   }

   public IpAddress getIpAddress( int pIndex ) {
      if( pIndex >= 0 && pIndex < mIpAddresses.length ) {
         return mIpAddresses[ pIndex ];
      }
      else {
         return null;
      }
   }

   public int getPort() {
      return mPort;
   }

   public String toString() {
      return "Port [ " +
         "IpAddress : " + Arrays.asList( getIpAddresses() ) +
         ", port: " + getPort() +
         " ]";
   }
}
