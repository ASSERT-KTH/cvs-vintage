/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;

/**
 * Root class of the JBoss JSR-77 implementation of
 * {@link javax.management.j2ee.Servlet Servlet}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.2 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 *
 * @jmx:mbean extends="org.jboss.management.j2ee.J2EEManagedObjectMBean"
 **/
public abstract class Servlet
   extends J2EEManagedObject
   implements ServletMBean
{
   // Constants -----------------------------------------------------
   
   public static final String J2EE_TYPE = "Servlet";
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
    * @param pName Name of the Servlet
    *
    * @throws InvalidParameterException If list of nodes or ports was null or empty
    **/
   public Servlet( String pName, ObjectName pWebModule )
      throws
         MalformedObjectNameException,
         InvalidParentException
   {
      super( J2EE_TYPE, pName, pWebModule );
   }

   // java.lang.Object overrides --------------------------------------

   public String toString() {
      return "Servlet { " + super.toString() + " } []";
   }
}
