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
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.J2EEResource J2EEResource}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.4 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 **/
public abstract class J2EEResource
   extends J2EEManagedObject
   implements javax.management.j2ee.J2EEResource
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructor ---------------------------------------------------
   
   /**
    * @param pName Name of the J2EEResource
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public J2EEResource( String pType, String pName, ObjectName pServer )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( pType, pName, pServer );
   }

   // Z implementation ----------------------------------------------
   
   // Y overrides ---------------------------------------------------
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}
