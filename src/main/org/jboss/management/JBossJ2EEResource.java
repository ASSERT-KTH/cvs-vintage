package org.jboss.management;

import javax.management.j2ee.J2EEResource;
/**
 * @author Marc Fleury
 **/
public abstract class JBossJ2EEResource
   extends JBossJ2EEManagedObject
   implements J2EEResource
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the J2EEResource
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJ2EEResource( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

}
