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

import javax.management.j2ee.JDBCDataSource;

/**
 * @author Marc Fleury
 **/
public abstract class JDBCConnection
   extends J2EEManagedObject
   implements javax.management.j2ee.JDBCConnection
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private JDBCDataSource mDriver;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the J2EEManagement
    * @param pDriver JDBC Driver to be set
    *
    * @throws InvalidParameterException If given driver is null
    **/
   public JDBCConnection( String pType, String pName, ObjectName pServer, JDBCDataSource pDriver )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pServer );
      if( pDriver == null ) {
         throw new InvalidParameterException( "There driver must always be defined" );
      }
      mDriver = pDriver;
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public JDBCDataSource getJDBCDataSource() {
      return mDriver;
   }
   
   public boolean isPooled() {
      return this instanceof javax.management.j2ee.JDBCConnectionPool;
   }

   public String toString() {
      return "JDBCConnection[ " + super.toString() +
         ", driver: " + mDriver +
         " ]";
   }

}
