/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.metadata;

import java.util.HashMap;
import javax.jms.Session;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.invocation.InvocationType;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.plugins.TxSupport;

/**
 * Provides a container and parser for the metadata of a message driven
 * bean.
 *
 * <p>Have to add changes ApplicationMetaData and ConfigurationMetaData.
 *
 * @version <tt>$Revision: 1.25 $</tt>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>
 * @author <a href="mailto:andreas@jboss.org">Andreas Schaefer</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class MessageDrivenMetaData
   extends BeanMetaData
{
   // Constants -----------------------------------------------------

   public static final int AUTO_ACKNOWLEDGE_MODE =
      Session.AUTO_ACKNOWLEDGE;
   public static final int DUPS_OK_ACKNOWLEDGE_MODE =
      Session.DUPS_OK_ACKNOWLEDGE;
   public static final int CLIENT_ACKNOWLEDGE_MODE =
      Session.CLIENT_ACKNOWLEDGE;
   public static final byte DURABLE_SUBSCRIPTION = 0;
   public static final byte NON_DURABLE_SUBSCRIPTION = 1;
   public static final String DEFAULT_MESSAGE_DRIVEN_BEAN_INVOKER_PROXY_BINDING = "message-driven-bean";

   // Attributes ----------------------------------------------------
   private int acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
   private byte subscriptionDurability = NON_DURABLE_SUBSCRIPTION;
   private TxSupport methodTransactionType = null;
   private String destinationType;
   private String messageSelector;
   private String destinationJndiName;
   private String username;
   private String password;
   private String clientId;
   private String subscriptionId;

   // True to enable the usage of XA connections.
   private boolean xaConnection = true;

   // True if server sessions are transacted.
   private boolean sessionPoolTransacted = true;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public MessageDrivenMetaData( ApplicationMetaData app )
   {
      super( app, BeanMetaData.MDB_TYPE );
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

      if (getMethodTransactionType() == TxSupport.REQUIRED)
      {
         return CLIENT_ACKNOWLEDGE_MODE;
      }
      else
      {
         return acknowledgeMode;
      }
   }

   public boolean getUseXAConnection()
   {
      return xaConnection;
   }

   public boolean getSessionPoolTransacted()
   {
      return sessionPoolTransacted;
   }

   /**
    * Get the Destination type ( Queue / Topic )
    */
   public String getDestinationType()
   {
      return destinationType;
   }

   /**
    * Get the message selector
    */
   public String getMessageSelector()
   {
      return messageSelector;
   }

   /**
    * Get the JNDI Name of the destination
    */
   public String getDestinationJndiName()
   {
      return destinationJndiName;
   }

   /**
    * Get declared User for this MDB
    */
   public String getUsername()
   {
      return username;
   }

   /**
    * Get declared password for this MDB
    */
   public String getPassword()
   {
      return password;
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
   public TxSupport getMethodTransactionType()
   {
      if( methodTransactionType == null )
      {
         if( isContainerManagedTx() )
         {
            //
            // Here we should have a way of looking up wich message class
            // the MessageDriven bean implements, by doing this we might
            // be able to use other MOM systems, aka XmlBlaser. TODO!
            // The MessageDrivenContainer needs this too!!
            //
            Class[] sig = {};
            if( super.getMethodTransactionType("onMessage", sig, null)
               == TxSupport.REQUIRED )
            {
               methodTransactionType = TxSupport.REQUIRED;
            }
            else
            {
               methodTransactionType = TxSupport.NOT_SUPPORTED;
            }
         }
         else
         {
	    //THIS IS A CHANGE FROM "UNKNOWN" which got mapped to DEFAULT later!
            methodTransactionType = TxSupport.DEFAULT;
         }
      }

      return methodTransactionType;
   }

   /**
    * Overide here, since a message driven bean only ever have one method,
    * which we might cache.
    */
   public TxSupport getMethodTransactionType(String methodName, Class[] params,
      InvocationType iface)
   {
      // An MDB may only ever have on method
      return getMethodTransactionType();
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
      return ConfigurationMetaData.MESSAGE_DRIVEN_13;
   }

   public void importEjbJarXml( Element element )
      throws DeploymentException
   {
      super.importEjbJarXml(element);

      messageSelector = getOptionalChildContent(element, "message-selector");
      if (messageSelector != null)
      {
         // Convert \n and \r to spaces
         messageSelector = messageSelector.replace('\n', ' ');
         messageSelector = messageSelector.replace('\r', ' ').trim();

         // If the selector is an empty string then null it for easier
         // detection
         if( "".equals(messageSelector) )
         {
            messageSelector = null;
         }
      }

      // destination is optional
      Element destination = getOptionalChild( element,
         "message-driven-destination" );
      if (destination != null)
      {
         destinationType = getUniqueChildContent(destination,
            "destination-type" );
         if( destinationType.equals("javax.jms.Topic") )
         {
            String subscr = getOptionalChildContent( destination,
               "subscription-durability" );
            // Should we do sanity check??
            if( subscr != null && subscr.equals("Durable") )
            {
               subscriptionDurability = DURABLE_SUBSCRIPTION;
            }
            else
            {
               subscriptionDurability = NON_DURABLE_SUBSCRIPTION;
            }
         }
      }

      // set the transaction type
      String transactionType = getUniqueChildContent( element,
         "transaction-type");
      if (transactionType.equals("Bean"))
      {
         containerManagedTx = false;
         String ack = getOptionalChildContent(element, "acknowledge-mode");

         if (ack == null ||
             ack.equalsIgnoreCase("Auto-acknowledge") ||
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
            throw new DeploymentException("Invalid acknowledge-mode: " + ack);
         }
      }
      else if (transactionType.equals("Container"))
      {
         containerManagedTx = true;
      }
      else
      {
         throw new DeploymentException
            ("Transaction type should be 'Bean' or 'Container'");
      }
   }

   public void importJbossXml( Element element )
      throws DeploymentException
   {
      super.importJbossXml(element);

      destinationJndiName = getUniqueChildContent( element,
         "destination-jndi-name");
      username = getOptionalChildContent(element, "mdb-user");
      password = getOptionalChildContent(element,"mdb-passwd");
      clientId = getOptionalChildContent(element,"mdb-client-id");
      subscriptionId = getOptionalChildContent(element,"mdb-subscription-id");

      String temp;

      temp = getOptionalChildContent(element, "session-pool-transacted");
      if( temp != null )
      {
         sessionPoolTransacted = new Boolean(temp).booleanValue();
      }

      temp = getOptionalChildContent(element, "xa-connection");
      if (temp != null)
      {
         xaConnection = new Boolean(temp).booleanValue();
      }
   }

   public void defaultInvokerBindings()
   {
      this.invokerBindings = new HashMap();
      this.invokerBindings.put(
         DEFAULT_MESSAGE_DRIVEN_BEAN_INVOKER_PROXY_BINDING, getJndiName() );
   }
}
/*
vim:ts=3:sw=3:et
*/
