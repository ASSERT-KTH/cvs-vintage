/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.jms.Message;
import javax.jms.Session;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.invocation.InvocationType;
import org.jboss.deployment.DeploymentException;

/**
 * Provides a container and parser for the metadata of a message driven bean.
 *
 * <p>Have to add changes ApplicationMetaData and ConfigurationMetaData.
 *
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.31 $
 */
public class MessageDrivenMetaData
   extends BeanMetaData
{
   // Constants -----------------------------------------------------
   
   public static final int AUTO_ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
   public static final int DUPS_OK_ACKNOWLEDGE_MODE = Session.DUPS_OK_ACKNOWLEDGE;
   public static final int CLIENT_ACKNOWLEDGE_MODE = Session.CLIENT_ACKNOWLEDGE;
   public static final byte DURABLE_SUBSCRIPTION = 0;
   public static final byte NON_DURABLE_SUBSCRIPTION = 1;
   public static final byte TX_UNSET = 9;
   public static final String DEFAULT_MESSAGE_DRIVEN_BEAN_INVOKER_PROXY_BINDING = "message-driven-bean";

   public static final String DEFAULT_MESSAGING_TYPE = "javax.jms.MessageListener";

   // Attributes ----------------------------------------------------
   
   private int acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
   private byte subscriptionDurability = NON_DURABLE_SUBSCRIPTION;
   private byte methodTransactionType = TX_UNSET;
   private String messagingType;
   private String destinationType;
   private String messageSelector;
   private String destinationJndiName;
   private String user;
   private String passwd;
   private String clientId;
   private String subscriptionId;
   /** The activation config properties */
   private HashMap activationConfigProperties = new HashMap();
   /** The resource adapter name */
   private String resourceAdapterName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   public MessageDrivenMetaData(ApplicationMetaData app)
   {
      super(app, BeanMetaData.MDB_TYPE);
   }
   
   // Public --------------------------------------------------------
   
   /**
    * Get the message acknowledgement mode.
    *
    * @return    MessageDrivenMetaData.AUTO_ACKNOWLADGE_MODE or
    *            MessageDrivenMetaData.DUPS_OK_AKNOWLEDGE_MODE or
    *            MessageDrivenMetaData.CLIENT_ACKNOWLEDGE_MODE
    */
   public int getAcknowledgeMode()
   {
      // My interpretation of the EJB and JMS spec leads
      // me to that CLIENT_ACK is the only possible
      // solution. A transaction is per session in JMS, and
      // it is not possible to get access to the transaction.
      // According to the JMS spec it is possible to
      // multithread handling of messages (but not session),
      // but there is NO transaction support for this.
      // I,e, we can not use the JMS transaction for
      // message ack: hence we must use manual ack.
      
      // But for NOT_SUPPORTED this is not true here we
      // should have AUTO_ACKNOWLEDGE_MODE
      
      // This is not true for now. For JBossMQ we relly
      // completely on transaction handling. For JBossMQ, the
      // ackmode is actually not relevant. We keep it here
      // anyway, if we find that this is needed for other
      // JMS provider, or is not good.
      
      if (getMethodTransactionType() == TX_REQUIRED)
      {
         return CLIENT_ACKNOWLEDGE_MODE;
      }
      else
      {
         return acknowledgeMode;
      }
   }

   public String getMessagingType()
   {
      return messagingType;
   }

   public boolean isJMSMessagingType()
   {
      return DEFAULT_MESSAGING_TYPE.equals(messagingType);
   }

   public String getDestinationType()
   {
      return destinationType;
   }
   
   public String getMessageSelector()
   {
      return messageSelector;
   }
   
   public String getDestinationJndiName()
   {
      return destinationJndiName;
   }
   
   public String getUser()
   {
      return user;
   }
   
   public String getPasswd()
   {
      return passwd;
   }
   
   public String getClientId()
   {
      return clientId;
   }
   
   public String getSubscriptionId()
   {
      return subscriptionId;
   }
   
   /**
    * Check MDB methods TX type, is cached here
    */
   public byte getMethodTransactionType()
   {
      if (methodTransactionType == TX_UNSET)
      {
         Class[] sig = { Message.class };
         methodTransactionType = getMethodTransactionType("onMessage", sig);
      }
      return methodTransactionType;
   }
   
   /**
    * Check MDB methods TX type, is cached here
    */
   public byte getMethodTransactionType(String methodName, Class[] signature)
   {
      if (isContainerManagedTx())
      {
         if (super.getMethodTransactionType(methodName, signature, null) == MetaData.TX_NOT_SUPPORTED)
            return TX_NOT_SUPPORTED;
         else
            return TX_REQUIRED;
      }
      else
         return TX_UNKNOWN;
   }

   /**
    * Overide here, since a message driven bean only ever have one method,
    * which we might cache.
    */
   public byte getMethodTransactionType(String methodName, Class[] params, InvocationType iface)
   {
      // A JMS MDB may only ever have one method
      if (isJMSMessagingType())
         return getMethodTransactionType();
      else
         return getMethodTransactionType(methodName, params);
   }

   /**
    * Get the subscription durability mode.
    *
    * @return    MessageDrivenMetaData.DURABLE_SUBSCRIPTION or
    *            MessageDrivenMetaData.NON_DURABLE_SUBSCRIPTION
    */
   public byte getSubscriptionDurability()
   {
      return subscriptionDurability;
   }
   
   public String getDefaultConfigurationName()
   {
      if (activationConfigProperties.size() != 0 || isJMSMessagingType() == false)
         return ConfigurationMetaData.MESSAGE_INFLOW_DRIVEN;
      else
         return ConfigurationMetaData.MESSAGE_DRIVEN_13;
   }

   /**
    * Get all the activation config properties
    * 
    * @return a collection of ActivationConfigPropertyMetaData elements
    */
   public Collection getActivationConfigProperties()
   {
      return activationConfigProperties.values();
   }
   
   /**
    * Get a particular activation config property
    * 
    * @param name the name of the property
    * @return the ActivationConfigPropertyMetaData or null if not found
    */
   public ActivationConfigPropertyMetaData getActivationConfigProperty(String name)
   {
      return (ActivationConfigPropertyMetaData) activationConfigProperties.get(name);
   }

   /**
    * Get the resource adapter name
    * 
    * @return the resource adapter name or null if none specified
    */
   public String getResourceAdapterName()
   {
      return resourceAdapterName;
   }
   
   public void importEjbJarXml(Element element) throws DeploymentException
   {
      super.importEjbJarXml(element);
      
      messageSelector = getOptionalChildContent(element, "message-selector");
      if( messageSelector != null )
      {
         //AS Check for Carriage Returns, remove them and trim the selector
         int i = -1;
         // Note this only works this way because the search and replace are distinct
         while( ( i = messageSelector.indexOf( "\r\n" ) ) >= 0 )
         {
            // Replace \r\n by a space
            messageSelector = ( i == 0 ? "" : messageSelector.substring( 0, i ) ) +
            " " +
            ( i >= messageSelector.length() - 2 ? "" : messageSelector.substring( i + 2 ) );
         }
         i = -1;
         while( ( i = messageSelector.indexOf( "\r" ) ) >= 0 )
         {
            // Replace \r by a space
            messageSelector = ( i == 0 ? "" : messageSelector.substring( 0, i ) ) +
            " " +
            ( i >= messageSelector.length() - 1 ? "" : messageSelector.substring( i + 1 ) );
         }
         i = -1;
         while( ( i = messageSelector.indexOf( "\n" ) ) >= 0 )
         {
            // Replace \n by a space
            messageSelector = ( i == 0 ? "" : messageSelector.substring( 0, i ) ) +
            " " +
            ( i >= messageSelector.length() - 1 ? "" : messageSelector.substring( i + 1 ) );
         }
         // Finally trim it. This is here because only carriage returns and linefeeds are transformed
         // to spaces
         messageSelector = messageSelector.trim();
         if( "".equals( messageSelector ) )
         {
            messageSelector = null;
         }
      }

      // messaging-type is new to EJB-2.1
      messagingType = getOptionalChildContent(element, "messaging-type", DEFAULT_MESSAGING_TYPE);

      // destination is optional
      Element destination = getOptionalChild(element, "message-driven-destination");
      if (destination != null)
      {
         destinationType = getUniqueChildContent(destination, "destination-type");
         if (destinationType.equals("javax.jms.Topic"))
         {
            String subscr = getOptionalChildContent(destination, "subscription-durability");
            // Should we do sanity check??
            if( subscr != null && subscr.equals("Durable") )
            {
               subscriptionDurability = DURABLE_SUBSCRIPTION;
            }
            else
            {
               subscriptionDurability = NON_DURABLE_SUBSCRIPTION;//Default
            }
         }
      }
      
      // set the transaction type
      String transactionType = getUniqueChildContent(element, "transaction-type");
      if (transactionType.equals("Bean"))
      {
         containerManagedTx = false;
         String ack = getOptionalChildContent(element, "acknowledge-mode");
         if( ack == null || ack.equalsIgnoreCase("Auto-acknowledge") ||
            ack.equalsIgnoreCase("AUTO_ACKNOWLEDGE"))
         {
            acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
         }
         else if (ack.equalsIgnoreCase("Dups-ok-acknowledge") ||
            ack.equalsIgnoreCase("DUPS_OK_ACKNOWLEDGE"))
         {
            acknowledgeMode = DUPS_OK_ACKNOWLEDGE_MODE;
         }
         else
         {
            throw new DeploymentException("invalid acknowledge-mode: " + ack);
         }
      }
      else if (transactionType.equals("Container"))
      {
         containerManagedTx = true;
      }
      else
      {
         throw new DeploymentException
         ("transaction type should be 'Bean' or 'Container'");
      }
      
      // Set the activation config properties
      Element activationConfig = getOptionalChild(element, "activation-config");
      if (activationConfig != null)
      {
         Iterator iterator = getChildrenByTagName(activationConfig, "activation-config-property");
         while (iterator.hasNext())
         {
            Element resourceRef = (Element) iterator.next();
            ActivationConfigPropertyMetaData metaData = new ActivationConfigPropertyMetaData();
            metaData.importEjbJarXml(resourceRef);
            if (activationConfigProperties.containsKey(metaData.getName()))
               throw new DeploymentException("Duplicate activation-config-property-name: " + metaData.getName());
            activationConfigProperties.put(metaData.getName(), metaData);
         }
      }
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      super.importJbossXml(element);
      
      // set the jndi name, (optional)
      destinationJndiName = getUniqueChildContent(element, "destination-jndi-name");
      user = getOptionalChildContent(element, "mdb-user");
      passwd = getOptionalChildContent(element,"mdb-passwd");
      clientId = getOptionalChildContent(element,"mdb-client-id");
      subscriptionId = getOptionalChildContent(element,"mdb-subscription-id");
      resourceAdapterName = getOptionalChildContent(element,"resource-adapter-name");
   }
   
   public void defaultInvokerBindings()   
   {   
     this.invokerBindings = new HashMap();   
     this.invokerBindings.put(DEFAULT_MESSAGE_DRIVEN_BEAN_INVOKER_PROXY_BINDING, getJndiName());
   } 

   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

