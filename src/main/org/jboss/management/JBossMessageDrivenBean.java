package org.jboss.management;

import javax.management.j2ee.EJB;

/**
 * @author Marc Fleury
 **/
public class JBossMessageDrivenBean
   extends JBossJ2EEManagedObject
   implements EJB
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the MessageDrivenBean
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossMessageDrivenBean( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossMessageDrivenBean[ " + getName() + " ]";
   }
}
