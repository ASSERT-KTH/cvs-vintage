/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import javax.management.j2ee.JDBCDataSource;

/**
* @author Marc Fleury
**/
public class JDBCConnectionPool
   extends JDBCConnection
   implements javax.management.j2ee.JDBCConnectionPool
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
   * @param pName Name of the J2EEManagement
   * @param pDriver JDBC Driver to be set
   *
   * @throws InvalidParameterException If given driver is null
   **/
   public JDBCConnectionPool( String pName, JDBCDataSource pDriver ) {
      super( pName, pDriver );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JDBCConnectionPool[ " + super.toString() +
         " ]";
   }

}
