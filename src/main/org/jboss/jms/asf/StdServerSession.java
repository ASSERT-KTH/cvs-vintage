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
import javax.jms.XASession;
import javax.naming.InitialContext;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

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
	private XASession xaSession = null;
	private TransactionManager tm;

    
	StdServerSession(StdServerSessionPool pool, Session session, XASession xaSession) throws JMSException{

	serverSessionPool = pool;
	this.session = session;
	this.xaSession = xaSession;

	try {
		tm = (TransactionManager)new InitialContext().lookup("java:/TransactionManager");
	} catch ( Exception e ) {
		throw new JMSException("Transation Manager was not found");
	}

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
	public void start() throws JMSException {
	    //Logger.debug("Start invokes on server session");
	    if (session != null) {
	        try {
	            serverSessionPool.getExecutor().execute(this);
	        } catch ( InterruptedException e ) {
	        }
	    } else {
	        throw new JMSException("No listener has been specified");
	    }
	}
    
	//--- Protected parts, used by other in the package
	
	/**
	 * Runs in an own thread, basically calls the session.run(), it is up
	 * to the session to have been filled with messages and it will run
	 * against the listener set in StdServerSessionPool. When it has send
	 * all its messages it returns.
	 *
	 * HC: run() also starts a transaction with the TransactionManager and
	 * enlists the XAResource of the JMS XASession if a XASession was abvailable.
	 * A good JMS implementation should provide the XASession for use in the ASF.
	 * So we optimize for the case where we have an XASession.  So, for the case
	 * where we do not have an XASession and the bean is not transacted, we 
	 * have the unneeded overhead of creating a Transaction.  I'm leaving it
	 * this way since it keeps the code simpler and that case should not be too 
	 * common (JBossMQ provides XASessions).
	 *
	 */
	public void run() {

	Transaction trans=null;

	try {

	    //Logger.debug("Invoking run on session");
	    
	    //Logger.debug("Starting the Message Driven Bean transaction");
	    tm.begin();
	    trans = tm.getTransaction();
	    
	    if( xaSession != null ) {
		
		   	XAResource res = xaSession.getXAResource();
		   	trans.enlistResource(res);
		   	//Logger.debug("XAResource '"+res+"' enlisted.");
		
	    } 
	    
	    session.run();
	    
	}catch (Exception ex) {
	    
	    Logger.exception( ex );
	    
	    try {
		// The transaction will be rolledback in the finally
		trans.setRollbackOnly();
	    } catch( Exception e ) {
		Logger.exception( e );
	    }
	    
	} finally {
	    
	    
	    try {
		
		//Logger.debug("Ending the Message Driven Bean transaction");
		
			// Marked rollback
			if ( trans.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
		    Logger.log("Rolling back JMS transaction");
		    // actually roll it back 
		    trans.rollback();
		    
		   // NO XASession? then manually rollback.  
		   // This is not so good but
		   // it's the best we can do if we have no XASession.
		    if( xaSession==null && serverSessionPool.isTransacted() ) 
			session.rollback();
		    
			} else if(trans.getStatus() == Status.STATUS_ACTIVE) {
		    
		    // Commit tx
		    // This will happen if
		    // a) everything goes well
		    // b) app. exception was thrown
		    
		    trans.commit();
		    
		    // NO XASession? then manually commit.  This is not so good but
		    // it's the best we can do if we have no XASession.
		    if( xaSession==null && serverSessionPool.isTransacted() ) 
			session.commit();
		    
			}
		
	    } catch(Exception e) {
		// There was a problem doing the commit/rollback.
		Logger.exception(e);
	    }
	    
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

	/**
	 * Called by the ServerSessionPool when the sessions should be closed.
	 */
	void close() {
	if (session != null) {
	    try {
		session.close();
	    }catch(Exception ex) {}
	    session = null;
	}
	if (xaSession != null) {
	    try {
		xaSession.close();
	    }catch(Exception ex) {}
	    xaSession = null;
	}
	}
}