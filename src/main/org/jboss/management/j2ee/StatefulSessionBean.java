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
 * {@link javax.management.j2ee.StatefulSessionBean StatefulSessionBean}.
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
public class StatefulSessionBean
   extends EJB
   implements StatefulSessionBeanMBean
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
