package org.jboss.management.j2ee;

import javax.management.j2ee.J2EEManagement;

/**
* Is the entry point of all managed objects in the management
* domain.
*
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public interface J2EEManagementMBean
   extends J2EEManagement, J2EEManagedObjectMBean
{
}
