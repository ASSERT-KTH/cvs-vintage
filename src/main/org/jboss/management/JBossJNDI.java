package org.jboss.management;

import javax.management.j2ee.JNDI;

/**
 * @author Marc Fleury
 **/
public class JBossJNDI
   extends JBossJ2EEResource
   implements JNDI
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JNDI
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJNDI( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossJNDI [ " +
// AS Later on must be set on again
//         "name: " + getName() +
         " ]";
   }
}
