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
public class StatefulSessionBean
   extends J2EEManagedObject
   implements javax.management.j2ee.SessionBean
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
   public StatefulSessionBean( String pName, ObjectName pEjbModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "StatefulSessionBean", pName, pEjbModule );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "StatefulSessionBean[ " + getName() + " ]";
   }
}
