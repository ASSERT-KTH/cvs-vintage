/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.j2ee.J2EEApplication;

/**
 * MBean Mangement Inteface for {@link org.jboss.management.j2ee.J2EEApplication
 * J2EEApplication}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.3 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public interface J2EEApplicationMBean
   extends J2EEApplication, J2EEManagedObjectMBean
{
}
