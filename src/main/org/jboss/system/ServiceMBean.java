/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

/**
 * An interface describing a JBoss service MBean.
 * 
 * @see Service
 * @see ServiceMBeanSupport
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.2 $
 */
public interface ServiceMBean
   extends Service
{
   // Constants -----------------------------------------------------
   
   String[] states = { "Stopped", "Stopping", "Starting", "Started" };
   
   int STOPPED  = 0;
   int STOPPING = 1;
   int STARTING = 2;
   int STARTED  = 3;
   
   // Public --------------------------------------------------------
   
   String getName();
   int getState();
   String getStateString();
}
