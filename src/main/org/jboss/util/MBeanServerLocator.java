/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.util;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
   
/**
 * A helper class to locate a MBeanServer.
 *      
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.1 $
 */
public class MBeanServerLocator
{
   public static MBeanServer locate(final String agentID) {
      MBeanServer server = (MBeanServer)
         MBeanServerFactory.findMBeanServer(agentID).iterator().next();
      
      return server;
   }

   public static MBeanServer locate() {
      return locate(null);
   }
}
