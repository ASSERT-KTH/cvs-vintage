/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.util.ServiceMBean;

/**
 * This is the interface of the AutoDeployer that is exposed for
 * administration
 *      
 * @see AutoDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.7 $
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
}

