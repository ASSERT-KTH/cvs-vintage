/*
 * Copyright (c) 2001 Peter Antman Tim <peter.antman@tim.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.jboss.jms.ra;

import java.io.Serializable;
import javax.jms.*;

import javax.resource.spi.ConnectionEvent;

/**
 * Adapts the JMS QueueSession and TopicSession API to a JmsManagedConnection.
 *
 * <p>Created: Tue Apr 17 22:39:45 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>.
 * @version $Revision: 1.4 $
 */
public class JmsSession
   implements QueueSession, TopicSession
{
   /** The managed connection for this session. */
   private JmsManagedConnection mc; // = null;

   /**
    * Construct a <tt>JmsSession</tt>.
    *
    * @param mc    The managed connection for this session.
    */
   public JmsSession(final JmsManagedConnection mc) {
      this.mc = mc;
   }

   /**
    * Ensure that the session is opened.
    *
    * @return    The session
    *
    * @throws javax.jms.IllegalStateException    The session is closed
    */
   private Session getSession() throws JMSException {
      // ensure that the connection is opened
      if (mc == null)
         throw new javax.jms.IllegalStateException("The session is closed");

      return mc.getSession();
   }
   
   // ---- Session API

   public BytesMessage createBytesMessage() throws JMSException
   {
      return getSession().createBytesMessage();
   }
    
   public MapMessage createMapMessage() throws JMSException
   {
      return getSession().createMapMessage();
   }
    
   public Message createMessage() throws JMSException
   {
      return getSession().createMessage();
   }

   public ObjectMessage createObjectMessage() throws JMSException
   {
      return getSession().createObjectMessage();
   }

   public ObjectMessage createObjectMessage(Serializable object)
      throws JMSException
   {
      return getSession().createObjectMessage(object);
   }

   public StreamMessage createStreamMessage() throws JMSException
   {
      return getSession().createStreamMessage();
   }

   public TextMessage createTextMessage() throws JMSException
   {
      return getSession().createTextMessage();
   }

   public TextMessage createTextMessage(String string) throws JMSException
   {
      return getSession().createTextMessage(string);
   }
   
   public boolean getTransacted() throws JMSException
   {
      return getSession().getTransacted();
   }

   /**
    * Always throws an Exception.
    *
    * @throws javax.jms.IllegalStateException     Method not allowed.
    */
   public MessageListener getMessageListener() throws JMSException
   {
      throw new javax.jms.IllegalStateException("Method not allowed");      
   }
    
   /**
    * Always throws an Exception.
    *
    * @throws javax.jms.IllegalStateException     Method not allowed.
    */
   public void setMessageListener(MessageListener listener)
      throws JMSException
   {
      throw new javax.jms.IllegalStateException("Method not allowed");
   }

   /**
    * Always throws an Error.
    *
    * @throws Error    Method not allowed.
    */
   public void run() {
      // should this really throw an Error?
      throw new Error("Method not allowed");
   }

   /**
    * Closes the session.  Sends a ConnectionEvent.CONNECTION_CLOSED to the
    * managed connection.
    *
    * @throws JMSException    Failed to close session.
    */
   public void close() throws JMSException
   {
      if (mc != null) {
         mc.getLogger().log(Level.FINE, "Closing session");
         // Special stuff FIXME
         mc.removeHandle(this);
         ConnectionEvent ev =
            new ConnectionEvent(mc, ConnectionEvent.CONNECTION_CLOSED);
         ev.setConnectionHandle(this);
         mc.sendEvent(ev);
         mc = null;
      }
   }

   // FIXME - is this really OK, probably not
   public void commit() throws JMSException
   {
      getSession().commit();
   }

   public void rollback() throws JMSException
   {
      getSession().rollback();
   }

   public void recover() throws JMSException
   {
      getSession().recover();
   }

   // --- TopicSession API
   
   public Topic createTopic(String topicName) throws JMSException
   {
      return ((TopicSession)getSession()).createTopic(topicName);
   }

   public TopicSubscriber createSubscriber(Topic topic) throws JMSException
   {
      return ((TopicSession)getSession()).createSubscriber(topic);
   }

   public TopicSubscriber createSubscriber(Topic topic,
                                           String messageSelector,
                                           boolean noLocal)
      throws JMSException
   {
      return ((TopicSession)getSession()).
         createSubscriber(topic,messageSelector, noLocal);
   }

   public TopicSubscriber createDurableSubscriber(Topic topic,
                                                  String name)
      throws JMSException
   {
      return ((TopicSession)getSession()).
         createDurableSubscriber(topic, name);
   }

   public TopicSubscriber createDurableSubscriber(Topic topic,
                                                  String name,
                                                  String messageSelector,
                                                  boolean noLocal)
      throws JMSException
   {
      return ((TopicSession)getSession()).
         createDurableSubscriber(topic, name, messageSelector, noLocal);
   }

   public TopicPublisher createPublisher(Topic topic) throws JMSException
   {
      return ((TopicSession)getSession()).createPublisher(topic);
   }

   public TemporaryTopic createTemporaryTopic() throws JMSException
   {
      return ((TopicSession)getSession()).createTemporaryTopic();
   }

   public void unsubscribe(String name) throws JMSException
   {
      ((TopicSession)getSession()).unsubscribe(name);
   }

   //--- QueueSession API
   
   public QueueBrowser createBrowser(Queue queue) throws JMSException
   {
      return ((QueueSession)getSession()).createBrowser(queue);
   }

   public QueueBrowser createBrowser(Queue queue,
                                     String messageSelector)
      throws JMSException
   {
      return ((QueueSession)getSession()).
         createBrowser(queue,messageSelector);
   }

   public Queue createQueue(String queueName) throws JMSException
   {
      return ((QueueSession)getSession()).createQueue(queueName);
   }

   public QueueReceiver createReceiver(Queue queue) throws JMSException
   {
      return ((QueueSession)getSession()).createReceiver(queue);
   }

   public QueueReceiver createReceiver(Queue queue, String messageSelector)
      throws JMSException
   {
      return ((QueueSession)getSession()).
         createReceiver(queue, messageSelector);
   }

   public QueueSender createSender(Queue queue) throws JMSException
   {
      return ((QueueSession)getSession()).createSender(queue);
   }

   public TemporaryQueue createTemporaryQueue() throws JMSException
   {
      return ((QueueSession)getSession()).createTemporaryQueue();
   }

   // --- JmsManagedConnection api
   
   void setManagedConnection(final JmsManagedConnection mc) {
      if (this.mc != null) {
         this.mc.removeHandle(this);
      }
      this.mc = mc;	
   }

   void destroy() {
      mc = null;
   }
}
