/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm.plugins.tyrex;

/**
 *   MBean interface for the Tyrex TransactionManager
 *   (not all calls are implemented)
 *
 *   @see TransactionManagerService
 *   @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 *   @version $Revision: 1.2 $
 */
public interface TransactionManagerServiceMBean
   extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=TransactionManager";

   // Public --------------------------------------------------------
   public String getConfigFileName();

   public void setConfigFileName(String name);
}

