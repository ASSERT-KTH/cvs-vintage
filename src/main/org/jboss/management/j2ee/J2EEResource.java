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
public abstract class J2EEResource
   extends J2EEManagedObject
   implements javax.management.j2ee.J2EEResource
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
   public J2EEResource( String pName ) {
      super( pName );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

}
