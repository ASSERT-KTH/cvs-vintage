/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import javax.management.j2ee.EJB;

/**
 * @author Marc Fleury
 **/
public class MessageDrivenBean
   extends J2EEManagedObject
   implements javax.management.j2ee.MessageDrivenBean
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
   public MessageDrivenBean( String pName, ObjectName pApplication )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( "MessageDrivenBean", pName, pApplication );
   }

   // -------------------------------------------------------------------------
   // Properties (Getters/Setters)
   // -------------------------------------------------------------------------  

   public String toString() {
      return "MessageDrivenBean[ " + getName() + " ]";
   }
}
