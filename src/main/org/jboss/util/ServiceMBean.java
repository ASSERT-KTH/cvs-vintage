/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/** An interface describing a JBoss service MBean.

@see org.jboss.util.Service
@see org.jboss.util.ServiceMBeanSupport

 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 1.5 $
 */
public interface ServiceMBean
	extends Service
{
   // Constants -----------------------------------------------------
   public static final String[] states = {"Stopped","Stopping","Starting","Started"};
   public static final int STOPPED  = 0;
   public static final int STOPPING = 1;
   public static final int STARTING = 2;
   public static final int STARTED  = 3;

   // Public --------------------------------------------------------
   public String getName();
   public int getState();
   public String getStateString();
}
