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
 * JmsSession.java
 *
 *
 * Created: Tue Apr 17 22:39:45 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsSession implements QueueSession, TopicSession {
    private JmsManagedConnection mc = null;
    public JmsSession(JmsManagedConnection mc) {
	this.mc = mc;
	
    }

    // ---- Session API
    public BytesMessage createBytesMessage() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createBytesMessage();
    }
    
    public MapMessage createMapMessage() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createMapMessage();
    }
    
    public Message createMessage() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createMessage();
    }

    public ObjectMessage createObjectMessage() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createObjectMessage();
    }
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createObjectMessage(object);
    }

    public StreamMessage createStreamMessage() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createStreamMessage();
    }

    public TextMessage createTextMessage() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createTextMessage();
    }

    public TextMessage createTextMessage(String string) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().createTextMessage(string);
    }
    public boolean getTransacted() throws JMSException
    { 
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return mc.getSession().getTransacted();
    }

    public MessageListener getMessageListener() throws JMSException
    {		
	
	throw new javax.jms.IllegalStateException("Methid not allowed in J2EE");	
	
    }
    
    public void setMessageListener(MessageListener listener) throws JMSException
    {
	throw new javax.jms.IllegalStateException("Methid not allowed in J2EE");	
    }

    public void run()
    {
	throw new Error("Methid not allowed in J2EE");	
    }

    public void close() throws JMSException
    { 
	mc.getLogger().log(Level.FINE, "Closing session");
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	// Special stuff FIXME
	mc.removeHandle(this);
	ConnectionEvent ev = new ConnectionEvent(mc, ConnectionEvent.CONNECTION_CLOSED);
	ev.setConnectionHandle(this);
	mc.sendEvent(ev);
	mc = null;	
    }

    // FIXME - is this really OK, probably not
    public void commit() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	mc.getSession().commit();
    }

    public void rollback() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	mc.getSession().rollback();
    }

    public synchronized void recover() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	mc.getSession().recover();
    }

    // --- TopicSession API
    public Topic createTopic(String topicName) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createTopic(topicName);
    }

    public TopicSubscriber createSubscriber(Topic topic) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createSubscriber(topic);
    }

    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createSubscriber(topic,messageSelector, noLocal);
    }

    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createDurableSubscriber(topic, name);
    }

    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createDurableSubscriber(topic,
								       name,
								       messageSelector,
								       noLocal);					       
    }

    public TopicPublisher createPublisher(Topic topic) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createPublisher(topic);
    }

    public TemporaryTopic createTemporaryTopic() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((TopicSession)mc.getSession()).createTemporaryTopic();
    }

    public void unsubscribe(String name) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	((TopicSession)mc.getSession()).unsubscribe(name);
    }

    //--- QueueSession API
    public QueueBrowser createBrowser(Queue queue) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createBrowser(queue);
    }

    public QueueBrowser createBrowser(Queue queue,String messageSelector) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createBrowser(queue,messageSelector);

    }

    public Queue createQueue(String queueName) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createQueue(queueName);
    }


    public QueueReceiver createReceiver(Queue queue) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createReceiver(queue);
    }

    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException
    { 
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createReceiver(queue, messageSelector);

    }

    public QueueSender createSender(Queue queue) throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createSender(queue);
    }

    public TemporaryQueue createTemporaryQueue() throws JMSException
    {
	if (mc == null) throw new javax.jms.IllegalStateException("The session is closed");
	return ((QueueSession)mc.getSession()).createTemporaryQueue();
    }

    // --- JmsManagedConnection api
    void setManagedConnection(JmsManagedConnection mc) {
	if (this.mc !=null) {
	    this.mc.removeHandle(this);
	}
	this.mc = mc;	
    }

    void destroy() {
	mc = null;
    }
} // JmsSession







