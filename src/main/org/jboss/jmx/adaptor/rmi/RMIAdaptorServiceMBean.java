/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.adaptor.rmi;

/**
* RMI Adaptor allowing an network aware client
* to work directly with a remote JMX Agent.
*
* @author <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
**/
public interface RMIAdaptorServiceMBean
	extends org.jboss.system.ServiceMBean
{
	// Constants -----------------------------------------------------
	public static final String OBJECT_NAME = "JMX:type=adaptor,protocol=RMI";

	// Public --------------------------------------------------------
    public String getJNDIName();
}
