package org.jboss.management.j2ee;

import javax.management.j2ee.J2EEServer;

/**
* Is the entry point of all managed objects in the management
* domain.
*
* @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
**/
public interface J2EEServerMBean
   extends J2EEServer, J2EEManagedObjectMBean
{
}
