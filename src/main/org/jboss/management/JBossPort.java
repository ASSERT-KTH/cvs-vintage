package org.jboss.management;

import org.jboss.jmx.interfaces.JMXConnector;

import javax.management.j2ee.IpAddress;
import javax.management.j2ee.Port;

/**
 * @author Marc Fleury
 **/
public class JBossPort
   extends JBossJ2EEManagedObject
   implements Port
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private IpAddress ipAddress;
   private int port;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the Port
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossPort( String pName, int pPort, IpAddress pIpAddress ) {
      super( pName );
      setPort( pPort );
      setIpAddress( pIpAddress );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return The value of IpAddress
    **/
   public IpAddress getIpAddress() {
      return ipAddress;
   }

   /**
    * Sets the new value of IpAddress
    *
    * @param pIpAddress New value of IpAddress to be set
    **/
   public void setIpAddress( IpAddress pIpAddress ) {
       ipAddress = pIpAddress;
   }

   /**
    * @return The value of Port
    **/
   public int getPort() {
      return port;
   }

   /**
    * Sets the new value of Port
    *
    * @param pPort New value of Port to be set
    **/
   public void setPort( int pPort ) {
       port = pPort;
   }

   public String toString() {
      return "JBossPort [ " +
         "IpAddress : " + getIpAddress() +
         ", port: " + getPort() +
         " ]";
   }
}
