/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.JDBCConnection;

/**
* @author Marc Fleury
**/
public class JDBC
   extends J2EEResource
   implements javax.management.j2ee.JDBC
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private JDBCConnection[] mConnections;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JDBC
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JDBC( String pName, ObjectName pServer, JDBCConnection[] pConnections )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JDBC", pName, pServer );
      if( pConnections == null ) {
         mConnections = new JDBCConnection[ 0 ];
      }
      else {
         mConnections = pConnections;
      }
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public JDBCConnection[] getJDBCConnections() {
      return mConnections;
   }
   
   public JDBCConnection getJDBCConnection( int pIndex ) {
      if( pIndex >= 0 && pIndex < mConnections.length ) {
         return mConnections[ pIndex ];
      }
      else {
         return null;
      }
   }
   
   public String toString() {
      return "JDBC[ " + super.toString() +
         ", JDBC Connection: " + java.util.Arrays.asList( getJDBCConnections() ) +
         " ]";
   }

}
