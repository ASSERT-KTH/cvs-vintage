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
public class EntityBean
   extends J2EEManagedObject
   implements javax.management.j2ee.EntityBean
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
   public EntityBean( String pName, ObjectName pEJBModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "EntityBean", pName, pEJBModule );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "EntityBean[ " + getName() + " ]";
   }
}
