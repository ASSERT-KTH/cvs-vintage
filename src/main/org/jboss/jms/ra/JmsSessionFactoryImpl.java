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

import javax.naming.Reference;

import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;



import javax.jms.JMSException;
import javax.jms.ConnectionConsumer;
import javax.jms.ServerSessionPool;
import javax.jms.TopicSession;
import javax.jms.Topic;
import javax.jms.QueueSession;
import javax.jms.Queue;
import javax.jms.ExceptionListener;
import javax.jms.ConnectionMetaData;

import org.jboss.jms.ra.client.JmsSessionFactory;
/**
 * JmsSessionFactoryImpl.java
 *
 *
 * Created: Thu Mar 29 15:36:51 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsSessionFactoryImpl implements JmsSessionFactory,Serializable, Referenceable {
    private static final String ISE = "This method is not applicatable in JMS resource adapter";

    private Reference reference;

    // Used from JmsConnectionFactory
    private String userName = null;
    private String password = null;
    private Boolean isTopic = null;
    
    /**
     * JmsRa own factory
     */
    private ManagedConnectionFactory mcf;

    /**
     * Hook to the appserver
     */
    private ConnectionManager cm;
    

    public JmsSessionFactoryImpl(ManagedConnectionFactory mcf,
				 ConnectionManager cm)
    {
	this.mcf = mcf;
	this.cm = cm;
        if (cm == null) {
	    // This is standalone usage, no appserver
            this.cm = new JmsConnectionManager();
        } else {
            this.cm = cm;
        }
	
    }

    public void setReference(Reference reference) {
	this.reference = reference;
    }
    
    public Reference getReference() {
        return reference;
    }
    
    // --- API for JmsConnectionFactoryImpl
    public void setUserName(String name) 
    {
	userName = name;
    }
    
    public void setPassword(String password) 
    {
	this.password = password;
    }

    public void isTopic(Boolean isTopic) {
	this.isTopic = isTopic; 
    }
    //---- QueueConnection ---
    public QueueSession createQueueSession(boolean transacted, 
					   int acknowledgeMode) 
	throws JMSException
    { 
	try {
	    if(isTopic != null && isTopic == Boolean.TRUE)
		throw new IllegalStateException("Cant get a queue session from a topic connection");
	    
	    JmsConnectionRequestInfo info = 
		new JmsConnectionRequestInfo(transacted,acknowledgeMode,false);
	    info.setUserName(userName);
	    info.setPassword(password);
	    
	    return (QueueSession) cm.allocateConnection(
							mcf, 
							info
							);
	}catch(ResourceException ex) {
	    JMSException je = new JMSException("Could not create a session: " + ex);
	    je.setLinkedException(ex);
	    throw je;
	}
    }
    
    public ConnectionConsumer createConnectionConsumer(Queue queue,
						       String messageSelector,
						       ServerSessionPool sessionPool,
						       int maxMessages) 
	throws JMSException 
    {
	throw new IllegalStateException(ISE);
    }
    

    //--- TopicConnection ---
    
    public TopicSession createTopicSession(boolean transacted, 
					   int acknowledgeMode) 
	throws JMSException
    { 
	try {
	    if(isTopic != null && isTopic == Boolean.FALSE)
		throw new IllegalStateException("Cant get a topic session from a session connection");

	    JmsConnectionRequestInfo info = 
		new JmsConnectionRequestInfo(transacted,acknowledgeMode,true);
	    info.setUserName(userName);
	    info.setPassword(password);
	    
	    return (TopicSession) cm.allocateConnection(mcf, 
							info
							    );
	    
	}catch(ResourceException ex) {
	    
	    JMSException je = new JMSException("Could not create a session: " + ex);
	    je.setLinkedException(ex);
	    throw je;
	}				    
    }
    
    public ConnectionConsumer createConnectionConsumer(Topic topic,
						       String messageSelector,
						       ServerSessionPool sessionPool,
						       int maxMessages) 
	throws JMSException 
    {
	throw new IllegalStateException(ISE);
    }		       

    public ConnectionConsumer createDurableConnectionConsumer(Topic topic, 
							String subscriptionName,
							String messageSelector,
							ServerSessionPool sessionPool, 
							int maxMessages) 
	throws JMSException
    {
	throw new IllegalStateException(ISE);
    }
    //--- Al the Connection methods
    public String getClientID() throws JMSException {throw new IllegalStateException(ISE);}
    
    public void setClientID(String cID) throws JMSException {throw new IllegalStateException(ISE);}
    
    public ConnectionMetaData getMetaData() throws JMSException {throw new IllegalStateException(ISE);}
    
    public ExceptionListener getExceptionListener() throws JMSException {throw new IllegalStateException(ISE);}
    
    public void setExceptionListener(ExceptionListener listener) throws JMSException {throw new IllegalStateException(ISE);}
    
    public void start() throws JMSException {throw new IllegalStateException(ISE);}
    
    public void stop() throws JMSException {throw new IllegalStateException(ISE);}
    
    public synchronized void close() throws JMSException {throw new IllegalStateException(ISE);}
} // JmsSessionFactoryImpl






