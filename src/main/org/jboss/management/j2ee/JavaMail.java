/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

/**
 * @author Marc Fleury
 **/
public class JavaMail
   extends J2EEResource
   implements javax.management.j2ee.JavaMail
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
   public JavaMail( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JavaMail [ " +
         "name: " + getName() +
         " ]";
   }
}
