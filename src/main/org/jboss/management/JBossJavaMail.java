package org.jboss.management;

import javax.management.j2ee.JavaMail;

/**
 * @author Marc Fleury
 **/
public class JBossJavaMail
   extends JBossJ2EEResource
   implements JavaMail
{
   // -------------------------------------------------------------------------
   // Members
   // -------------------------------------------------------------------------  

   // -------------------------------------------------------------------------
   // Constructors
   // -------------------------------------------------------------------------

   /**
    * @param pName Name of the JavaMail
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public JBossJavaMail( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JBossJavaMail [ " +
         "name: " + getName() +
         " ]";
   }
}
