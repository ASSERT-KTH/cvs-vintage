/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.j2ee.JMS;
import javax.management.j2ee.StateManageable;

/**
 * MBean Mangement Inteface for {@link org.jboss.management.j2ee.JMS
 * JMS}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020301 Scott McLaughlin:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public interface JMSResourceMBean
   extends JMS, StateManageable, J2EEManagedObjectMBean
{
}
