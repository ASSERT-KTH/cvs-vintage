/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdbc;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.3 $
 */
public interface JdbcProviderMBean
   extends org.jboss.util.ServiceMBean
{
   // Public --------------------------------------------------------
   public void setDrivers(String driverList);
   
   public String getDrivers();
}

