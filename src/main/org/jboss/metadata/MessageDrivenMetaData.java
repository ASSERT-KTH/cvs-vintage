/*
 * jBoss, the OpenSource EJB server
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
 *   <description> 
 *   Based on SessionMetaData   
 *
 * Have to add changes ApplicationMetaData and ConfigurationMetaData
 *   @see <related>
' *   @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *   @author Peter Antman (peter.antman@tim.se)

 *   @version $Revision: 1.6 $
 */
public class MessageDrivenMetaData extends BeanMetaData {
    // Constants -----------------------------------------------------
    public static final int AUTO_ACKNOWLEDGE_MODE = Session.AUTO_ACKNOWLEDGE;
    public static final int DUPS_OK_ACKNOWLEDGE_MODE = Session.DUPS_OK_ACKNOWLEDGE;
    public static final int CLIENT_ACKNOWLEDGE_MODE = Session.CLIENT_ACKNOWLEDGE;
    public static final byte DURABLE_SUBSCRIPTION = 0;
    public static final byte NON_DURABLE_SUBSCRIPTION = 1;
    
    // Attributes ----------------------------------------------------
    private boolean containerManagedTx;
    private int acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
    private String destinationType;
    private byte subscriptionDurability = NON_DURABLE_SUBSCRIPTION;
    private String messageSelector = null;
    private String destinationJndiName;
    private String user = null;
    private String passwd = null;
    private String clientId = null;

    // Static --------------------------------------------------------
    
    // Constructors --------------------------------------------------
    public MessageDrivenMetaData(ApplicationMetaData app) {
	super(app);
	messageDriven = true;
	session = false;
	}
	
    // Public --------------------------------------------------------
    public boolean isContainerManagedTx() { return containerManagedTx; }
    public boolean isBeanManagedTx() { return !containerManagedTx; }
    /**
     * returns MessageDrivenMetaData.AUTO_ACKNOWLADGE_MODE or
     * MessageDrivenMetaData.DUPS_OK_AKNOWLEDGE_MODE
     */
    public int getAcknowledgeMode() {return acknowledgeMode;}
    public String getDestinationType() { return destinationType;}
    public String getMessageSelector() { return messageSelector;}
    public String getDestinationJndiName(){return destinationJndiName;}
    public String getUser() { return user;}
    public String getPasswd() {return passwd;}
    public String getClientId() {return clientId;}
    /**
     * returns MessageDrivenMetaData.DURABLE_SUBSCRIPTION or 
     * MessageDrivenMetaData.NON_DURABLE_SUBSCRIPTION
     */
    public byte getSubscriptionDurability() {return subscriptionDurability;}
    
    public String getDefaultConfigurationName() {
	return jdk13Enabled() ? ConfigurationMetaData.MESSAGE_DRIVEN_13 : ConfigurationMetaData.MESSAGE_DRIVEN_12;

    }
	
	public void importEjbJarXml(Element element) throws DeploymentException {
		super.importEjbJarXml(element);
		
		messageSelector = getElementContent(getOptionalChild(element, "message-selector"));

		// set 
		Element destination = getUniqueChild(element, "message-driven-destination");
		destinationType = getElementContent(getUniqueChild(destination
, "destination-type"));
		 
		if (destinationType.equals("javax.jms.Topic")) {
			String subscr = getElementContent(getUniqueChild(destination, "subscription-durability"));
			// Should we do sanity check??
			if (subscr.equals("Durable"))
			    subscriptionDurability = DURABLE_SUBSCRIPTION;
			else
			    subscriptionDurability = NON_DURABLE_SUBSCRIPTION;//Default
		}
		/* Skipp check of dest type, for flexibility
		} else if (destinationType.equals("javax.jms.Queue")) {
			//Noop
		} else {
			throw new DeploymentException("session type should be 'Stateful' or 'Stateless'");
		}
		*/
		// set the transaction type
		String transactionType = getElementContent(getUniqueChild(element, "transaction-type"));
		if (transactionType.equals("Bean")) {
			containerManagedTx = false;
			String ack = getElementContent(getUniqueChild(element, "acknowledge-mode"));
			if ( ack.equals("Auto-acknowledge") || ack.equals("AUTO_ACKNOWLEDGE"))
			    acknowledgeMode = AUTO_ACKNOWLEDGE_MODE;
			else
			    acknowledgeMode = DUPS_OK_ACKNOWLEDGE_MODE;
			// else defaults to AUTO
		} else if (transactionType.equals("Container")) {
			containerManagedTx = true;
			/* My interpretation of the EJB and JMS spec leads
			   me to that CLIENT_ACK is the only possible
			   solution. A transaction is per session in JMS, and
			   it is not possible to get access to the transaction.
			   According to the JMS spec it is possible to 
			   multithread handling of messages (but not session),
			   but there is NO transaction support for this.
			   I,e, we can not use the JMS transaction for
			   message ack: hence we must use manual ack.

			   But for NOT_SUPPORTED this is not true here we 
			   should have AUTO_ACKNOWLEDGE_MODE

			   This is not true for now. For JBossMQ we relly 
			   completely on transaction handling. For JBossMQ, the
			   ackmode is actually not relevant. We keep it here
			   anyway, if we find that this is needed for other
			   JMS provider, or is not good.
			   
			*/

			/*
			 * Here we should have a way of looking up wich message class
			 * the MessageDriven bean implements, by doing this we might
			 * be able to use other MOM systems, aka XmlBlaser. TODO!
			 * The MessageDrivenContainer needs this too!!
			 */
			if(getMethodTransactionType("onMessage", new Class[] {}, true) == MetaData.TX_REQUIRED)
			    acknowledgeMode = CLIENT_ACKNOWLEDGE_MODE;
		} else {
		    throw new DeploymentException("transaction type should be 'Bean' or 'Container'");
		}
	}
    public void importJbossXml(Element element) throws DeploymentException {
	
	super.importJbossXml(element);
	// set the jndi name, (optional)		
	destinationJndiName = getElementContent(getUniqueChild(element, "destination-jndi-name"));
	user = getElementContent(getOptionalChild(element,"mdb-user"));
	passwd = getElementContent(getOptionalChild(element,"mdb-passwd"));
	clientId = getElementContent(getOptionalChild(element,"mdb-client-id"));
    }	
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}
