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

import javax.jms.JMSException;

import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.EISSystemException;
/**
 * JmsLocalTransaction.java
 *
 *
 * Created: Tue Apr 17 23:44:05 2001
 *
 * @author Peter Antman (peter.antman@tim.se)
 * @version $Revision: 1.1 $
 */

public class JmsLocalTransaction implements LocalTransaction {
    JmsManagedConnection mc;
    public JmsLocalTransaction(JmsManagedConnection mc) {
	this.mc = mc;
    }

    public void begin() throws ResourceException {
	// NOOP - begin is automatic in JMS
	// Should probably send event
	ConnectionEvent ev = new ConnectionEvent(mc, ConnectionEvent.LOCAL_TRANSACTION_STARTED);
	mc.sendEvent(ev);
    }
    
    public void commit() throws ResourceException {
	try {
	    mc.getSession().commit();
	    ConnectionEvent ev = new ConnectionEvent(mc, ConnectionEvent.LOCAL_TRANSACTION_COMMITTED);
	    mc.sendEvent(ev);
	}catch(JMSException ex) {
	    ResourceException re = new EISSystemException("Could not commit LocalTransaction : " + ex.getMessage());
            re.setLinkedException(ex);
            throw re;
	}

    }
    public void rollback() throws ResourceException {
	try {
	    mc.getSession().rollback();
	    ConnectionEvent ev = new ConnectionEvent(mc, ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK);
	    mc.sendEvent(ev);
	}catch(JMSException ex) {
	    ResourceException re = new EISSystemException("Could not rollback LocalTransaction : " + ex.getMessage());
            re.setLinkedException(ex);
            throw re;
	}
    }
} // JmsLocalTransaction



