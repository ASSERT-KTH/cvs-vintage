package org.jboss.management;

import javax.management.j2ee.SynchronousBean;

/**
 * @author Marc Fleury
 **/
public class JBossEntityBean
   extends JBossJ2EEManagedObject
   implements SynchronousBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the EntityBean
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossEntityBean( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossEntityBean[ " + getName() + " ]";
   }
}
