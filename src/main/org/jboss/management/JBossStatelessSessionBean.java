package org.jboss.management;

import javax.management.j2ee.SessionBean;

/**
 * @author Marc Fleury
 **/
public class JBossStatelessSessionBean
   extends JBossJ2EEManagedObject
   implements SessionBean
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the StatelessSessionBean
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossStatelessSessionBean( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossStatelessSessionBean[ " + getName() + " ]";
   }
}
