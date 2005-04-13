/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.monitor;

import java.util.Set;

import org.jboss.system.ServiceMBean;

/**
 * The JMX management interface for the {@link EntityLockMonitor} MBean.
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 1.5 $
 */
public interface EntityLockMonitorMBean extends ServiceMBean
{
   // Attributes
   public long getTotalContentions();
   public long getMedianWaitTime();
   public long getMaxContenders();
   public long getAverageContenders();

   // Operations
   public void clearMonitor();
   public String printLockMonitor();
   public Set listMonitoredBeans();
   public LockMonitor getLockMonitor(String jndiName);
   
}