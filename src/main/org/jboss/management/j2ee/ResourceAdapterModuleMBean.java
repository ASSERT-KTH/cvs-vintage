/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management.j2ee;

//import javax.management.j2ee.ResourceAdapterModule;
import javax.management.j2ee.StateManageable;
import javax.management.ObjectName;

/**
 * MBean Mangement Inteface for {@link org.jboss.management.j2ee.ResourceAdapterModule
 * ResourceAdapterModule}.
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
public interface ResourceAdapterModuleMBean
   extends StateManageable,J2EEManagedObjectMBean
{
   public ObjectName[] getResourceAdapters();
   public ObjectName getResourceAdapter( int pIndex );
   public String getDeploymentDescriptor();
}
