/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import javax.jms.Session;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.jboss.ejb.DeploymentException;

/**
 * Provides a container and parser for the metadata of a message driven bean.
 *
 * <p>Have to add changes ApplicationMetaData and ConfigurationMetaData.
 * 
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.13 $
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

   // Attributes ----------------------------------------------------
   
   private int acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
   private String destinationType = null;
   private byte subscriptionDurability = NON_DURABLE_SUBSCRIPTION;
   private String messageSelector; // = null;
   private String destinationJndiName;
   private String user; // = null;
   private String passwd; // = null;
   private String clientId; // = null;
   private byte methodTransactionType= TX_UNSET;
   // Static --------------------------------------------------------
    
   // Constructors --------------------------------------------------
   
   public MessageDrivenMetaData(ApplicationMetaData app)
   {
      super(app, BeanMetaData.MDB_TYPE);
   }
	
   // Public --------------------------------------------------------

   /**
    * returns MessageDrivenMetaData.AUTO_ACKNOWLADGE_MODE or
    * MessageDrivenMetaData.DUPS_OK_AKNOWLEDGE_MODE, or MessageDrivenMetaData.CLIENT_ACKNOWLEDGE_MODE
    *
    */
   public int getAcknowledgeMode() {
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
      if ( getMethodTransactionType() == TX_REQUIRED)
	 return  CLIENT_ACKNOWLEDGE_MODE;
      else 
	 return acknowledgeMode;
   }
   
   public String getDestinationType() {
      return destinationType;
   }
   
   public String getMessageSelector() {
      return messageSelector;
   }
   
   public String getDestinationJndiName() {
      return destinationJndiName;
   }
   
   public String getUser() {
      return user;
   }
   
   public String getPasswd() {
      return passwd;
   }
   
   public String getClientId() {
      return clientId;
   }
   
   /**
    * Check MDB methods TX type, is cached here
    */
   public byte getMethodTransactionType() {
      if (methodTransactionType == TX_UNSET) {
	 if (isContainerManagedTx()) {
	    //
	    // Here we should have a way of looking up wich message class
	    // the MessageDriven bean implements, by doing this we might
	    // be able to use other MOM systems, aka XmlBlaser. TODO!
	    // The MessageDrivenContainer needs this too!!
	    //
	    if(super.getMethodTransactionType("onMessage", new Class[] {}, true) == MetaData.TX_REQUIRED) {
	       methodTransactionType = TX_REQUIRED;
	    } else {
	       methodTransactionType = TX_NOT_SUPPORTED;
	    }
	 } else {
	    methodTransactionType = TX_UNKNOWN;
	 }
      }
      return methodTransactionType;
   }
   
   /**
    * Overide here, since a message driven bean only ever have one method, wich
    * we might cache.
    */
   public byte getMethodTransactionType(String methodName, Class[] params, boolean remote) {
      // An MDB may only ever have on method
      return getMethodTransactionType();
   }
   
   /**
    * returns MessageDrivenMetaData.DURABLE_SUBSCRIPTION or 
    * MessageDrivenMetaData.NON_DURABLE_SUBSCRIPTION
    */
   public byte getSubscriptionDurability() {
      return subscriptionDurability;
   }
    
   public String getDefaultConfigurationName() {
      return
         jdk13Enabled() ?
         ConfigurationMetaData.MESSAGE_DRIVEN_13 :
         ConfigurationMetaData.MESSAGE_DRIVEN_12;
   }
	
   public void importEjbJarXml(Element element) throws DeploymentException
   {
      super.importEjbJarXml(element);

      messageSelector = getOptionalChildContent(element, "message-selector");

      // destination is optional
      Element destination =
         getOptionalChild(element, "message-driven-destination");
      if (destination != null) {
	 destinationType = getUniqueChildContent(destination, 
						 "destination-type");
	 
	 if (destinationType.equals("javax.jms.Topic")) {
	    String subscr =
	       getUniqueChildContent(destination, "subscription-durability");
	    
	    // Should we do sanity check??
	    if (subscr.equals("Durable")) {
	       subscriptionDurability = DURABLE_SUBSCRIPTION;
	    }
	    else {
	       subscriptionDurability = NON_DURABLE_SUBSCRIPTION;//Default
	    }
	 }
      }
      // set the transaction type
      String transactionType =
         getUniqueChildContent(element, "transaction-type");
      
      if (transactionType.equals("Bean")) {
         containerManagedTx = false;
         String ack = getUniqueChildContent(element, "acknowledge-mode");
         
         if (ack.equals("Auto-acknowledge") || ack.equals("AUTO_ACKNOWLEDGE")) {
            acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
         }
         else {
            acknowledgeMode = DUPS_OK_ACKNOWLEDGE_MODE;
         }
         // else defaults to AUTO
      } else if (transactionType.equals("Container")) {
         containerManagedTx = true;
      } else {
         throw new DeploymentException
            ("transaction type should be 'Bean' or 'Container'");
      }
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      super.importJbossXml(element);
      // set the jndi name, (optional)		
      destinationJndiName =
         getUniqueChildContent(element, "destination-jndi-name");
      user = getOptionalChildContent(element, "mdb-user");
      passwd = getOptionalChildContent(element,"mdb-passwd");
      clientId = getOptionalChildContent(element,"mdb-client-id");
   }	

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
    
   // Inner classes -------------------------------------------------
}
