package org.jboss.management;

import java.security.InvalidParameterException;

import javax.management.j2ee.IpAddress;
import javax.management.j2ee.Port;

/**
 * @author Marc Fleury
 **/
public class JBossIpAddress
   extends JBossJ2EEManagedObject
   implements IpAddress
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private String address = null;
   private Port[] ports;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the IpAddress
    * @param pAddress IP Address to be set
    * @param pPorts List of ports
    *
    * @throws InvalidParameterException If given list is null or empty
    **/
   public JBossIpAddress( String pName, String pAddress, Port[] pPorts ){
      super( pName );
      setAddress( pAddress );
      setPorts( pPorts );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return The actual address or null if not defined
    **/
   public String getAddress() {
      return address;
   }

   /**
    * Sets a new Address
    *
    * @param pAddress New address to be set
    **/
   public void setAddress( String pAddress ) {
      address = pAddress;
   }

   /**
    * @return The actual list of Ports which is never null
    **/
   public Port[] getPorts() {
      return ports;
   }

   /**
    * Sets a new list of Ports
    *
    * @param pPorts New list of Ports to be set. If null
    *              then the list will be set empty
    *
    * @throws InvalidParameterException If given list is null or empty
    **/
   public void setPorts( Port[] pPorts ) {
      if( pPorts == null || pPorts.length == 0 ) {
         throw new InvalidParameterException( "There must always be at least one Port defined" );
      }
      ports = pPorts;
   }

   public String toString() {
      return "JBossIpAddress [ " +
         "IpAddress: " + getAddress() +
         ", ports: " + java.util.Arrays.asList( getPorts() ) +
         " ]";
   }
}
