/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jms;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.naming.InitialContext;
import javax.naming.Context;

import javax.jms.Session;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.QueueSender;
import javax.jms.Queue;
import javax.jms.Message;
import javax.jms.JMSException;

import org.w3c.dom.Element;

import org.jboss.logging.Logger;
import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;


/**
 * Places redeliveded messages on a Dead Letter Queue.
 *
 *<p>The Dead Letter Queue handler is used to not set JBoss in an endles loop
 * when a message is resent on and on due to transaction rollback for
 * message receipt.
 *
 *<p>It sends message to a dead letter queue (configurable, defaults to
 * queue/DLQ) when the message has been resent a configurable amount of times,
 * defaults to 10.
 *
 * <p>The handler is configured through the element MDBConfig in
 * container-invoker-conf.
 *
 * <p>The JMS property JBOSS_ORIG_DESTINATION in the resent message is set
 * to the name of the original destination (Destionation.toString()).
 *
 * <p>The JMS property JBOSS_ORIG_MESSAGEID in the resent message is set
 * to the id of the original message.
 *
 *
 * Created: Thu Aug 23 21:17:26 2001
 *
 * @author
 * @version $Revision: 1.4 $ $Date: 2001/12/14 02:21:45 $
 */
public class DLQHandler
{
   /** Class logger. */
   private static Logger log = Logger.getLogger(DLQHandler.class);
   
   /** JMS property name holding original destination. */
   public static final String JBOSS_ORIG_DESTINATION ="JBOSS_ORIG_DESTINATION";
   
   /** JMS property name holding original JMS message id. */
   public static final String JBOSS_ORIG_MESSAGEID="JBOSS_ORIG_MESSAGEID";
   
   /** Connection factory JNDI, java:/ConnectionFactory, should we make it configurable? */
   private static final String FACTORY_JNDI="java:/ConnectionFactory";
   
   // Configuratable stuff
   /**
    *  Destination to send dead letters to.
    *<p>Defaults to queue/DLQ,
    * configurable through DestinationQueue element.
    */
   private String destinationJNDI = "queue/DLQ";
   
   /**
    * Maximum times a message is alowed to be resent.
    *
    * <p>Defaults to 10, configurable through MaxTimesRedelivered element.
    */
   private int maxResent = 10;
   
   /**
    * Time to live for the message.
    *
    *<p>Defaults to Message.DEFAULT_TIME_TO_LIVE, configurable through
    * the TimeToLive element.
    */
   private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;
   
   // May become configurable
   /** Delivery mode for message, Message.DEFAULT_DELIVERY_MODE. */
   private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;
   /** Priority for the message, Message.DEFAULT_PRIORITY */
   private int priority = Message.DEFAULT_PRIORITY;
   
   // Private stuff
   private QueueConnection connection;
   private Queue dlq;
   private Hashtable resentBuffer = new Hashtable();
   
   public DLQHandler()
   {
      
   }
   
   
   //--- Service
   /**
    * Initalize the service.
    *
    * @throws Exception    Service failed to initalize.
    */
   void init() throws Exception
   {
      Context ctx = new InitialContext();
      QueueConnectionFactory factory = (QueueConnectionFactory)
      ctx.lookup(FACTORY_JNDI);
      
      connection = factory.createQueueConnection();
      dlq = (Queue)ctx.lookup(destinationJNDI);
      log.debug("Created Dead Letter Queue connection " + dlq);
   }
   
   /**
    * Start the service.
    *
    * @throws Exception    Service failed to start.
    */
   void start() throws Exception
   {
      
   }
   
   /**
    * Stop the service.
    */
   void stop()
   {
      
   }
   
   /**
    * Destroy the service.
    */
   void destroy()
   {
      try
      {
         connection.stop();
      }catch(Exception ex)
      {}
   }
   
   //--- Logic
   /**
    * Check if a message has been redelivered to many times.
    *
    * If message has been redelivered to many times, send it to the
    * dead letter queue (default to queue/DLQ).
    *
    * @return true if message is handled, i.e resent, false if not.
    */
   public boolean handleRedeliveredMessage(Message msg)
   {
      try
      {
         String id = msg.getJMSMessageID();
         if (id == null)
         {
            // Warning function
            log.debug("Message id is null, can't handle message");
         }
         // if we can't get the id we are basically fucked
         if(id != null && incrementResentCount(id) > maxResent)
         {
            log.info("Message resent to many time, sending it to DLQ. Id: "
            + id);
            sendMessage(msg);
            deleteFromBuffer(id);
            return true;
         }
      }
      catch(JMSException ex)
      {
         // If we can't send it ahead, we do not dare to just drop it...or?
         log.error("Could not send message to Dead Letter Queue " + ex,ex);
         return false;
      }
      return false;
      
   }
   
   //--- Private helper stuff
   /**
    * Increment the counter for the specific JMS message id.
    *
    * @return the new counter value.
    */
   protected int incrementResentCount(String id)
   {
      BufferEntry entry = null;
      if(!resentBuffer.containsKey(id))
      {
         log.debug("Making new entry for id " + id);
         entry = new BufferEntry();
         entry.id = id;
         entry.count = 1;
         resentBuffer.put(id,entry);
      }
      else
      {
         entry = (BufferEntry)resentBuffer.get(id);
         entry.count++;
         log.debug("Incremented old entry for id " + id + " count " + entry.count);
      }
      return entry.count;
   }
   
   /**
    * Delete the entry in the message counter buffer for specifyed JMS id.
    */
   protected void deleteFromBuffer(String id)
   {
      resentBuffer.remove(id);
   }
   
   /**
    * Send message to the configured dead letter queue, defaults to queue/DLQ.
    */
   protected void sendMessage(Message msg) throws JMSException
   {
      // Set the properties
      QueueSession ses = null;
      QueueSender sender = null;
      try
      {
         msg = makeWritable(msg);//Don't know yet if we are gona clone or not
         msg.setStringProperty(JBOSS_ORIG_MESSAGEID,
         msg.getJMSMessageID());
         msg.setStringProperty(JBOSS_ORIG_DESTINATION,
         msg.getJMSDestination().toString());
         
         ses = connection.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
         sender = ses.createSender(dlq);
         log.debug("Resending DLQ message to destination" + dlq);
         sender.send(msg,deliveryMode,priority,timeToLive);
      }
      finally
      {
         try
         {
            sender.close();
            ses.close();
         }
         catch(Exception ex)
         {
         }
      }
   }
   
   /**
    * Make the Message properties writable.
    *
    * @return the writable message.
    */
   protected Message makeWritable(Message msg) throws JMSException
   {
      
      Hashtable tmp = new Hashtable();
      // Save properties
      for(Enumeration en = msg.getPropertyNames();en.hasMoreElements();)
      {
         String key = (String) en.nextElement();
         tmp.put(key,msg.getStringProperty(key));
      }
      // Make them writable
      msg.clearProperties();
      
      Enumeration keys = tmp.keys();
      
      while(keys.hasMoreElements())
      {
         String key = (String) keys.nextElement();
         msg.setStringProperty(key,(String)tmp.get(key));
      }
      return msg;
   }
   
   /**
    * Takes an MDBConfig Element
    */
   public void importXml(Element element) throws DeploymentException
   {
      destinationJNDI  = MetaData.getElementContent
      (MetaData.getUniqueChild(element, "DestinationQueue"));
      
      try
      {
         String mr = MetaData.getElementContent
         (MetaData.getUniqueChild(element, "MaxTimesRedelivered"));
         maxResent = Integer.parseInt(mr);
         
         String ttl = MetaData.getElementContent
         (MetaData.getUniqueChild(element, "TimeToLive"));
         timeToLive = Long.parseLong(ttl);
         // A timeToLive < 0 means the msg is unusable so use default instead
         if( timeToLive < 0 )
            timeToLive = Message.DEFAULT_TIME_TO_LIVE;
      }
      catch (NumberFormatException e)
      {
         //Noop will take default value
      }
      catch (DeploymentException e)
      {
         //Noop will take default value
      }
   }

   public String toString()
   {
      StringBuffer buff = new StringBuffer();
      buff.append("DLQHandler: {");
      buff.append("destinationJNDI=").append(destinationJNDI);
      buff.append(";maxResent=").append(maxResent);
      buff.append(";timeToLive=").append(timeToLive);
      buff.append("}");
      return buff.toString();
   }
   
   private class BufferEntry
   {
      int count;
      String id;
   }
} // DLQHandler
