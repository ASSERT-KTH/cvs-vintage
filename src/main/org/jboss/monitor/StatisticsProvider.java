/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.monitor;

import java.util.List;

/**
 * Interface defining the methods necessary for a JSR-77 Performance Monitoring
 * data provider.
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.3 $
 */
public interface StatisticsProvider
{
	// Constants ----------------------------------------------------
	
	// Public -------------------------------------------------------
   
   /**
    * Add its own statistics informations to the
    * given Data Set. For any chain of components
    * like the interceptors this method must traverse
    * the entire chain by calling this method on the
    * entrance in the chain.
    *
    * @param container Data set used to add its statistics informations
    *                  of type {@link org.jboss.management.j2ee.SampleData
    *                  org.jboss.management.j2ee.SampleData}.
    * @param reset If set to true class has to reset the statistical
    *              informations gathered after the data are added to the container
    **/
   public void retrieveStatistics( List container, boolean reset );
   
}
