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
 * {@link javax.management.j2ee.MessageDrivenBean MessageDrivenBean}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 **/
public class MessageDrivenBean
   extends EJB
   implements MessageDrivenBeanMBean
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
