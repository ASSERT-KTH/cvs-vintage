/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.ejb.plugins.inflow;

import javax.jms.Session;

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.ActivationConfigPropertyMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

/**
 * Hacked version of message endpoint factory for backwards compatibility
 *
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a> .
 * @version <tt>$Revision: 1.1 $</tt>
 */
public class JBossJMSMessageEndpointFactory
   extends JBossMessageEndpointFactory
{
   // Constants -----------------------------------------------------
   
   /** The JBoss resource adapter deployment name */
   protected static String jmsra = "jms-ra.rar";
   
   // Attributes ----------------------------------------------------
   
   // Static --------------------------------------------------------
         
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   // Protected -----------------------------------------------------

   protected String resolveResourceAdapterName() throws DeploymentException
   {
      String result = super.resolveResourceAdapterName();
      if (result == null)
         result = jmsra;
      return result;
   }
   
   /**
    * Add activation config properties
    * 
    * @throws DeploymentException for any error
    */
   protected void augmentActivationConfigProperties() throws DeploymentException
   {
      super.augmentActivationConfigProperties();
      
      // Hack for old style deployments (jms)
      if (metaData.isJMSMessagingType())
      {
         checkActivationConfig("destination", metaData.getDestinationJndiName());
         checkActivationConfig("destinationType", metaData.getDestinationType());
         checkActivationConfig("messageSelector", metaData.getMessageSelector());
         if (Session.DUPS_OK_ACKNOWLEDGE == metaData.getAcknowledgeMode())
            checkActivationConfig("acknowledgeMode", "DUPS_OK_ACKNOWLEDGE");
         else
            checkActivationConfig("acknowledgeMode", "AUTO_ACKNOWELDGE");
         if (MessageDrivenMetaData.DURABLE_SUBSCRIPTION == metaData.getSubscriptionDurability())
            checkActivationConfig("subscriptionDurability", "Durable");
         else
            checkActivationConfig("subscriptionDurability", "NonDurable");
         checkActivationConfig("clientID", metaData.getClientId());
         checkActivationConfig("subscriptionName", metaData.getSubscriptionId());
         
         // Only for JBoss's resource adapter
         if (jmsra.equals(resourceAdapterName))
         {
            checkActivationConfig("user", metaData.getUser());
            checkActivationConfig("password", metaData.getPasswd());
            Element proxyConfig = invokerMetaData.getProxyFactoryConfig();
            checkActivationConfig("maxMessages", MetaData.getOptionalChildContent(proxyConfig, "MaxMessages"));
            checkActivationConfig("minSession", MetaData.getOptionalChildContent(proxyConfig, "MinimumSize"));
            checkActivationConfig("maxSession", MetaData.getOptionalChildContent(proxyConfig, "MaximumSize"));
            checkActivationConfig("keepAlive", MetaData.getOptionalChildContent(proxyConfig, "KeepAliveMillis"));
            Element mdbConfig = MetaData.getOptionalChild(proxyConfig, "MDBConfig");
            if (mdbConfig != null)
            {
               checkActivationConfig("reconnectInterval", MetaData.getOptionalChildContent(proxyConfig, "ReconnectIntervalSec"));
               checkActivationConfig("deliveryActive", MetaData.getOptionalChildContent(proxyConfig, "DeliveryActive"));
               checkActivationConfig("providerAdapterJNDI", MetaData.getOptionalChildContent(proxyConfig, "JMSProviderAdapterJNDI"));
               // TODO DLQ
            }
         }
      }
   }   

   /**
    * When the config doesn't exist for a given name adds the value when not null
    * 
    * @param name the name of the config property
    * @param value the value to add
    */
   void checkActivationConfig(String name, String value)
   {
      if (value != null && properties.containsKey(name) == false)
      {
         ActivationConfigPropertyMetaData md = new ActivationConfigPropertyMetaData(name, value);
         properties.put(name, md);
      }
   }
   
   // Package Private -----------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner Classes -------------------------------------------------
}
