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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * An EAR Deployment 
 *
 * @see EARDeployer
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian.Brock</a>
 * @version $Revision: 1.2 $
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
   private ConcurrentReaderHashMap metadata = new ConcurrentReaderHashMap();

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

   /**
    * @jmx:managed-operation
    *
    */
   public Object resolveMetaData(Object key)
   {
      return metadata.get(key);
   }

    /**
     * @jmx:managed-operation
     *
     */
   public void addMetaData(Object key, Object value)
   {
      metadata.put(key, value);
   }

    /**
     * @jmx:managed-operation
     *
     */
   public Map getMetaData()
   {
      return metadata;
   }
}
