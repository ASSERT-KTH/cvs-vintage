/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.deployment;

import org.jboss.system.ServiceMBean;

/**
 * This is the interface of the AutoDeployer that is exposed for
 * administration
 *      
 * @see AutoDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.2 $
 */
public interface AutoDeployerMBean
   extends ServiceMBean
{
   /** The default object name. */
   String OBJECT_NAME = "EJB:service=AutoDeployer";

   /**
    * Set the list of urls to watch.
    *
    * @param urlList    The list of urls to watch.
    */
   void setURLs(String urlList);

   /**
    * Get the list of urls that are currently being watched.
    *
    * @return   The list of urls that are currently being watched.
    */
   String getURLs();

   /**
    * Set the list of deployers that will be used.
    *
    * @param deployers    The list of deployers that will be used.
    */
   void setDeployers(String deployers);
   
   /**
    * Get the list of deployers that is currently being used.
    *
    * @return   The list of deployers that is currently being used.
    */
   String getDeployers();

   /**
    * Set the time in milli seconds the AutoDeployer have to sleep before
    * looking again for new files.
    *
    * @param to Timeout in miliseconds
    */
   void setTimeout(int to);

   /**
    * Return the time in milli seconds the AutoDeployer have to sleep before
    * looking again for new files.
    *
    * @return The timeout in miliseconds
    */
   int getTimeout();
   
   /**
    * @return True if this Auto Deployer runs a initial deployment in the starting
    *         thread
    **/
   public boolean isWithInitialRun();
   
   /**
    * Set if the AutoDeployer should run an intial deployment in the starting
    * thread or wait until the deployer thread picks it up. This is important
    * to be set to false when the AutoDeployer is deployed within a service
    * deployed by the AutoDeployer because this causes the entire auto deployment
    * to hand.
    *
    * @param pWithInitialRun True if the initial run should be performed by the
    *                        starting thread.
    **/
   public void setWithInitialRun( boolean pWithInitialRun );

}

