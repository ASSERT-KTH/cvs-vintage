/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

import javax.management.ObjectName;
import javax.management.j2ee.StateManageable;

/**
 * MBean Mangement Inteface for {@link org.jboss.management.j2ee.JCAConnectionFactory
 * JCAConnectionFactory}.
 *
 * @author  <a href="mailto:mclaugs@comcast.net">Scott McLaughlin</a>.
 * @version $Revision: 1.1 $
 *   
 * <p><b>Revisions:</b>
 *
 * <p><b>20020303 Scott McLaughlin:</b>
 * <ul>
 * <li> Creation
 * </ul>
 **/
public interface JCAConnectionFactoryMBean extends StateManageable, J2EEManagedObjectMBean
{
    public ObjectName getManagedConnectionFactory();
}
