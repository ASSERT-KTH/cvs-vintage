package org.jboss.management;

import java.security.InvalidParameterException;

import javax.management.j2ee.JDBCConnectionPool;
import javax.management.j2ee.JDBCDriver;

/**
 * @author Marc Fleury
 **/
public class JBossJDBCConnectionPool
   extends JBossJ2EEManagedObject
   implements JDBCConnectionPool
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private JDBCDriver driver;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the J2EEManagement
    * @param pDriver JDBC Driver to be set
    *
    * @throws InvalidParameterException If given driver is null
    **/
   public JBossJDBCConnectionPool( String pName, JDBCDriver pDriver ) {
      super( pName );
      setDriver( pDriver );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   /**
    * @return The actual driver which is always defined
    **/
   public JDBCDriver getDriver() {
      return driver;
   }

   /**
    * Sets a driver
    *
    * @param pDriver New driver to be set
    *
    * @throws InvalidParameterException If given driver is null
    **/
   public void setDriver( JDBCDriver pDriver ) {
      if( pDriver == null ) {
         throw new InvalidParameterException( "There driver must always be defined" );
      }
      driver = pDriver;
   }
   
   public String toString() {
      return "JBossJDBCConnectionPool[ name: " + getName() +
         ", driver: " + getDriver() +
         " ]";
   }

}
