/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.deployment;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Logger;
import org.jboss.mx.util.ObjectNameConverter;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBeanSupport;

/**
 * An EAR Deployment 
 *
 * @see EARDeployer
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian.Brock</a>
 * @version $Revision: 1.1 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class EARDeployment
   extends ServiceMBeanSupport
   implements EARDeploymentMBean
{
   // Constants -----------------------------------------------------

   public static final String BASE_EAR_DEPLOYMENT_NAME = "jboss.j2ee:service=EARDeployment";

   public static final ObjectName EAR_DEPLOYMENT_QUERY_NAME = ObjectNameFactory.create(BASE_EAR_DEPLOYMENT_NAME + ",*");

   private static final Logger log = Logger.getLogger(EARDeployment.class);
   
   // Attributes ----------------------------------------------------

   private String name;
   private EARDeployer deployer;
   private DeploymentInfo deploymentInfo;
   
   // Static --------------------------------------------------------
   
   public static String getJMXName(J2eeApplicationMetaData metaData, DeploymentInfo di)
   {
      String name = metaData.getJMXName();
      if( name == null )
         name = BASE_EAR_DEPLOYMENT_NAME + ",url='" + di.shortName + "'";
      return name;
   }
   
   // Constructors --------------------------------------------------

   public EARDeployment(final DeploymentInfo di)
   {
      this.deployer = (EARDeployer) di.deployer;
      this.deploymentInfo = di;
      String name = deploymentInfo.url.toString();
      if (name.endsWith("/"))
         name = name.substring(0, name.length() - 1);
      this.name = name;
   }
   
   // Public --------------------------------------------------------
   
   public String getJMXName() throws Exception
   {
      J2eeApplicationMetaData metaData = (J2eeApplicationMetaData) deploymentInfo.metaData;
      return getJMXName(metaData, deploymentInfo);
   }
}
