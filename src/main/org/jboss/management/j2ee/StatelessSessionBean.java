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
public class StatelessSessionBean
   extends J2EEManagedObject
   implements javax.management.j2ee.StatelessSessionBean
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
   public StatelessSessionBean( String pName, ObjectName pEjbModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "StatelessSessionBean", pName, pEjbModule );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "StatelessSessionBean[ " + getName() + " ]";
   }
}
