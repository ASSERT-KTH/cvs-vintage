/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.util.Iterator;
import java.net.MalformedURLException;

import javax.management.ObjectName;
import org.jboss.util.jmx.ObjectNameFactory;

import org.jboss.deployment.DeployerMBean;
import org.jboss.deployment.DeploymentException;
import org.jboss.system.ServiceMBean;

/**
 * This is the interface of the EJBDeployer that is exposed
 * for administration.
 *
 * @see EJBDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:juha.lindfors@jboss.org">Juha Lindfors</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.5 $
 * 
 * <p><b>20011227 marc fleury:</b>
 * <ul>
 *    <li>Deployer and ClassLoader unification extends DeployerMBean
 * </ul>
 */
public interface EJBDeployerMBean
   extends DeployerMBean
{
   /** The default object name. */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss.ejb:service=EJBDeployer");
   
   /**
    * Returns the applications deployed by the container factory
    */
   Iterator getDeployedApplications();
   
   /**
    * Enable/disable bean verification upon deployment.
    *
    * @param verify    true to enable the verifier; false to disable
    */
   void setVerifyDeployments(boolean verify);
   
   /**
    * Returns the state of the verifier (enabled/disabled)
    *
    * @return   true if verifier is enabled; false otherwise
    */
   boolean getVerifyDeployments();
   
   /**
    * Enable/disable bean verifier verbose mode.
    *
    * @param verbose    true to enable verbose mode; false to disable
    */
   void setVerifierVerbose(boolean verbose);
   
   /**
    * Returns the state of the verifier (verbose/non-verbose mode).
    *
    * @return  true if the verbose mode is enabled; false otherwise
    */
   boolean getVerifierVerbose();
   
   /**
    * Enables/disables the metrics interceptor for containers.
    *
    * @param enable    true to enable; false to disable
    */
   void setMetricsEnabled(boolean enable);
   
   /**
    * Checks if this container factory initializes the metrics interceptor.
    *
    * @return   true if metrics are enabled; false otherwise
    */
   boolean isMetricsEnabled();
   
   /**
    * Is the aplication with this url deployed.
    *
    * @param url
    * 
    * @throws MalformedURLException
    */
   boolean isDeployed(String url) throws MalformedURLException;
   
   /**
    * Set the JMS monitoring of the bean cache.
    */
   void setBeanCacheJMSMonitoringEnabled(boolean enable);
   
   boolean isBeanCacheJMSMonitoringEnabled();

   /**
    * Get the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   boolean getValidateDTDs();
   
   /**
    * Set the flag indicating that ejb-jar.dtd, jboss.dtd &
    * jboss-web.dtd conforming documents should be validated
    * against the DTD.
    */
   void setValidateDTDs(boolean validate);
}

/* Change log.
 * 
 * Author: starksm, Date: Thu Jun 14 17:14:14  2001 GMT
 * Added getValidateDTDs/setValidateDTDs methods.
 */
