package org.jboss.management;

import javax.management.j2ee.SessionBean;

/**
 * @author Marc Fleury
 **/
public class JBossStatefulSessionBean
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
    * @param pName Name of the StatefulSessionBean
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossStatefulSessionBean( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossStatefulSessionBean[ " + getName() + " ]";
   }
}
