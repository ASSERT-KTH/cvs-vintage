/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import java.util.Collection;

import javax.management.MBeanServer;

import javax.management.j2ee.J2EEManagedObject;
import javax.management.j2ee.StatisticsProvider;
import javax.management.j2ee.Stats;

/**
 * Collector Interface which must be implemented by
 * any collector to lookup the management data
 *
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.4 $
 **/
public interface DataCollector {

   // -------------------------------------------------------------------------
   // Methods
   // -------------------------------------------------------------------------  

   /**
   * Is called when the data must be refreshed
   *
   * @param pServer MBean Server is used to get the information about the server
   *
   * @return Collection of elements found (must be of type J2EEManagedObject)
   **/
   public Collection refresh( MBeanServer pServer );

  /**
  * Returns the statistics for the given Statistics Provider
  *
  * @param pProvider Provider of the statistics
  * @param pServer MBean Server is used to get the information about the server
  * @return Information about the JVM this application server is running on
  */
  public Stats getStatistics( StatisticsProvider pProvider, MBeanServer pServer );
  
  /**
  * Resets the statiscs provider
  *
  * @param pProvider Statistics Provider to be reset
  * @param pServer MBean Server is used to get the information about the server
  **/
  public void resetStatistics( StatisticsProvider pProvider, MBeanServer pServer );

}
