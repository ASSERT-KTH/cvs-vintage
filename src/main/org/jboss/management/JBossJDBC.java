package org.jboss.management;

import javax.management.j2ee.JDBC;
import javax.management.j2ee.JDBCConnectionPool;

/**
 * @author Marc Fleury
 **/
public class JBossJDBC
   extends JBossJ2EEResource
   implements JDBC
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   private JDBCConnectionPool[] mPools;

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JDBC
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJDBC( String pName, JDBCConnectionPool[] pPools ) {
      super( pName );
      if( pPools == null ) {
         mPools = new JDBCConnectionPool[ 0 ];
      }
      else {
         mPools = pPools;
      }
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public JDBCConnectionPool[] getJDBCConnectionPools() {
      return mPools;
   }
   
   public JDBCConnectionPool getJDBCConnectionPool( int pIndex ) {
      if( pIndex >= 0 && pIndex < mPools.length ) {
         return mPools[ pIndex ];
      }
      else {
         return null;
      }
   }
   
   public String toString() {
      return "JBossJDBC[ name: " + getName() +
         ", JDBC Connection Pools: " + java.util.Arrays.asList( getJDBCConnectionPools() ) +
         " ]";
   }

}
