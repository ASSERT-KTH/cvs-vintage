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
 * @version $Revision: 1.5 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Adjustments to the JBoss Guidelines
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.EJBMBean"
 **/
public class MessageDrivenBean
   extends EJB
   implements MessageDrivenBeanMBean
{
   
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "MessageDrivenBean";
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
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
      super( J2EE_TYPE, pName, pApplication );
   }
   
   // Public --------------------------------------------------------
   
   // Object overrides ---------------------------------------------------
   
   public String toString() {
      return "MessageDrivenBean[ " + getName() + " ]";
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
   
}
