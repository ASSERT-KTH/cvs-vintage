/*
 * Copyright (c) 2000 Peter Antman Tim <peter.antman@tim.se>
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
package org.jboss.jms.asf;

import java.lang.Runnable;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;

import org.jboss.logging.Logger;
/**
 * StdServerSession.java
 *
 *
 * Created: Thu Dec  7 18:25:40 2000
 *
 * @author 
 * @version
 */

public class StdServerSession implements Runnable, ServerSession {
    private StdServerSessionPool serverSessionPool = null;
    private Session session = null;
    
    StdServerSession(StdServerSessionPool pool, Session session) throws JMSException{

	serverSessionPool = pool;
	this.session = session;
    }

    // --- Impl of JMS standard API
    
    /**
     * Implementation of ServerSession.getSession
     *
     * This simply returns what it has fetched from the connection. It is
     * up to the jms provider to typecast it and have a private API to stuff
     * messages into it.
     */
    public Session getSession() 
        throws JMSException
    {
        return session;
    }
    
    // implementation of ServerSession.start
    public void start() 
        throws JMSException
    {
	Logger.debug("Start invokes on server session");
        if ( session != null) {
	    serverSessionPool.getThreadPool().run(this);
        }
        else {
	    throw new JMSException("No listener has been specified");
	}
    }
    
    //--- Protected parts, used by other in the package
    
    /**
     * Runs in an own thread, basically calls the session.run(), it is up
     * to the session to have been filled with messages and it will run
     * against the listener set in StdServerSessionPool. When it has send
     * all its messages it returns.
     */
    public void run() {
	try {
	    Logger.debug("Invoking run on session");
	    session.run();
	}catch (Exception ex) {
	    // Log error
	}finally {
	    StdServerSession.this.recycle();
	}
    }
    
    /**
     * This method is called by the ServerSessionPool when it is ready to
     * be recycled intot the pool
     */
    void recycle()
    {
	serverSessionPool.recycle(this);
    }
} // StdServerSession
