/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;
import org.jboss.system.server.ServerConfig;
import org.jboss.system.server.ServerConfigUtil;

/**
 * The meta data object for the cluster-config element.
 * This element only defines the HAPartition name at this time.  It will be
 * expanded to include other cluster configuration parameters later on.

 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>.
 * @version $Revision: 1.8 $
 */
public class ClusterConfigMetaData extends MetaData
{
   public final static String JNDI_PREFIX_FOR_SESSION_STATE = "/HASessionState/";
   public final static String DEFAULT_SESSION_STATE_NAME = JNDI_PREFIX_FOR_SESSION_STATE + "Default";

   private String partitionName = ServerConfigUtil.getDefaultPartitionName();
   private String homeLoadBalancePolicy = null;
   private String beanLoadBalancePolicy = null;

   private String haSessionStateName = DEFAULT_SESSION_STATE_NAME;

   public String getPartitionName()
   {
      return partitionName;
   }

   public String getHomeLoadBalancePolicy()
   {
      return homeLoadBalancePolicy;
   }

   public String getBeanLoadBalancePolicy()
   {
      return beanLoadBalancePolicy;
   }

   // SFSB only
   //
   public String getHaSessionStateName()
   {
      return this.haSessionStateName;
   }

   public void init(BeanMetaData data)
   {
      homeLoadBalancePolicy = "org.jboss.ha.framework.interfaces.RoundRobin";
      if (beanLoadBalancePolicy == null)
      {
         if (data.isSession())
         {
            if (((SessionMetaData) data).isStateful())
            {
               beanLoadBalancePolicy = "org.jboss.ha.framework.interfaces.FirstAvailable";
            }
            else
            {
               beanLoadBalancePolicy = "org.jboss.ha.framework.interfaces.RoundRobin";
            }
         }
         else if (data.isEntity())
         {
            beanLoadBalancePolicy = "org.jboss.ha.framework.interfaces.FirstAvailable";
         }
         else
         {
            beanLoadBalancePolicy = "org.jboss.ha.framework.interfaces.FirstAvailable";
         }
      }
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      partitionName = getElementContent(getOptionalChild(element, "partition-name"), null);
      if (partitionName == null)
         partitionName = ServerConfigUtil.getDefaultPartitionName();
      homeLoadBalancePolicy = getElementContent(getOptionalChild(element, "home-load-balance-policy"), homeLoadBalancePolicy);
      beanLoadBalancePolicy = getElementContent(getOptionalChild(element, "bean-load-balance-policy"), beanLoadBalancePolicy);

      // SFSB settings only
      //
      haSessionStateName = getElementContent(getOptionalChild(element, "session-state-manager-jndi-name"), DEFAULT_SESSION_STATE_NAME);
   }
}
