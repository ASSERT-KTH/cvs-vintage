package org.jboss.management;

import javax.management.j2ee.JDBCDriver;

/**
 * @author Marc Fleury
 **/
public class JBossJDBCDriver
   extends JBossJ2EEManagedObject
   implements JDBCDriver
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JDBCDriver
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJDBCDriver( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossJDBCDriver[ name: " + getName() +
         " ]";
   }
}
