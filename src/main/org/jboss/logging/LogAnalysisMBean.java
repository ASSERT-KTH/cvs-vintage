/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.logging;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;

/**
 * The log analysis service MBean interface. <p>
 *
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 *
 */
public interface LogAnalysisMBean
  extends ServiceMBean
{
  // Constants -----------------------------------------------------
  
  // Static --------------------------------------------------------
  
  // Public --------------------------------------------------------

  /**
   * Retrieve the analysis.
   */
  public String analyse();
}
