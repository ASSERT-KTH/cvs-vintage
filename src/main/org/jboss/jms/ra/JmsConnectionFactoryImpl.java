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

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.TopicConnection;

import org.jboss.jms.ra.client.JmsConnectionFactory;
/**
 * JmsConnectionFactoryImpl.java
 *
 *
 * Created: Thu Apr 26 17:02:50 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsConnectionFactoryImpl 
    implements JmsConnectionFactory,Serializable, Referenceable {
    private Reference reference;

    /**
     * JmsRa own factory
     */
    private ManagedConnectionFactory mcf;

    /**
     * Hook to the appserver
     */
    private ConnectionManager cm;
    
    public JmsConnectionFactoryImpl(ManagedConnectionFactory mcf,
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

    public void setReference(Reference reference) 
    {
	this.reference = reference;
    }
    
    public Reference getReference() 
    {
        return reference;
    }
    // --- QueueConnectionFactory
    public QueueConnection createQueueConnection() 
	throws JMSException 
    {
	JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf,cm);
	s.isTopic(Boolean.FALSE);
	return s;
    } 
    public QueueConnection createQueueConnection(String userName,
						 String password) 
	throws JMSException 
    {
	JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf,cm);
	s.isTopic(Boolean.FALSE);
	s.setUserName(userName);
	s.setPassword(password);
	return s;
    } 

    // --- TopicConnectionFactory
    public TopicConnection createTopicConnection() 
	throws JMSException 
    {
	JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf,cm);
	s.isTopic(Boolean.TRUE);
	return s;
    } 
    public TopicConnection createTopicConnection(String userName,
						 String password) 
	throws JMSException 
    {
	JmsSessionFactoryImpl s = new JmsSessionFactoryImpl(mcf,cm);
	s.isTopic(Boolean.TRUE);
	s.setUserName(userName);
	s.setPassword(password);
	return s;
    }
} // JmsConnectionFactoryImpl
