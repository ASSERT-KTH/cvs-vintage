/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.j2ee.J2EEDomain;

/**
 * MBean Mangement Inteface for {@link org.jboss.management.j2ee.J2EEDomain
 * J2EEDomain}.
 *
 * @author  <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20011126 Andreas Schaefer:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public interface J2EEDomainMBean
   extends J2EEDomain, J2EEManagedObjectMBean
{
}
