/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author Marc Fleury
 **/
public class JNDI
   extends J2EEResource
   implements javax.management.j2ee.JNDI
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
   public JNDI( String pName, ObjectName pServer )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "JNDI", pName, pServer );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "JNDI [ " +
// AS Later on must be set on again
//         "name: " + getName() +
         " ]";
   }
}
