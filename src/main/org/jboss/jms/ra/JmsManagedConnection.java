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

import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.io.PrintWriter;

import javax.security.auth.Subject;

import javax.transaction.xa.XAResource;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;

import javax.jms.*;

import javax.resource.ResourceException;
import javax.resource.NotSupportedException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.CommException;
import javax.resource.spi.SecurityException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.ConnectionEvent;

import org.jboss.jms.jndi.JMSProviderAdapter;

/**
<p>Managed Connection, manages one or more JMS sessions.
</p>


 <p>Every ManagedConnection will have a physical JMSConnection under the hood. This may leave out several session, as specifyed in 5.5.4 Multiple Connection Handles. Thread safe semantics is provided. 
 </p>
 <p>Hm. If we are to follow the example in 6.11 this will not work. We would have to use the SAME session. This means we will have to guard against concurrent
access. We use a stack, and only allowes the handle at the top of the stack to do things.
 </p>
 <p>As to transactions we some fairly hairy alternatives to handle:
  XA - we get an XA. We may now only do transaction through the XAResource, since a XASession MUST throw exceptions in commit etc. But since XA support implies LocatTransaction support, we will have to use the XAResource in the LocalTransaction class.
  LocalTx - we get a normal session. The LocalTransaction will then work against the normal session api.

  An invokation of JMS MAY BE DONE in none transacted context. What do we do
  then? How much should we leave to the user???

  One possible solution is to use transactions any way, but under the hood. If not LocalTransaction or XA has been aquired by the container, we have to do
 the commit in send and publish. (CHECK is the container required to get a XA
 every time it uses a managed connection? No its is not, only at creation!)

 Does this mean that a session one time may be used in a transacted env, and
 another time in a not transacted.

 Maybe we could have this simple rule:

 If a user is going to use non trans:

 - mark that i ra deployment descr
 - Use a JmsProviderAdapter with non XA factorys
 - Mark session as non transacted (this defeats the purpose of specifying trans attrinbutes in deploy descr NOT GOOD

 From the JMS tutorial:
 "When you create a session in an enterprise bean, the container ignores the arguments you specify, because it manages all transactional properties for enterprise beans."

 And further:
 "You do not specify a message acknowledgment mode when you create a message-driven bean that uses container-managed transactions. The container handles
acknowledgment automatically."

On Session or Connection:

From Tutorial:
"A JMS API resource is a JMS API connection or a JMS API session." But in the
J2EE spec only connection is considered a resource.


Not resolved: connectionErrorOccurred: it is verry hard to know from the
exceptions thrown if it is a connection error. Should we register an
ExceptionListener and mark al handles as errounous? And then let them send the event and throw an exception?
 *
 *
 * Created: Tue Apr 10 13:09:45 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class JmsManagedConnection  implements ManagedConnection{
    private JmsManagedConnectionFactory mcf;
    private JmsConnectionRequestInfo info;
    private String user = null;
    private String pwd = null;

    private boolean isDestroyed = false;

    // Physical JMS connection stuff
    private Connection con;
    //    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private XATopicSession xaTopicSession;
    //    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private XAQueueSession xaQueueSession;
    private XAResource xaResource;
    // private boolean isTopic = true;
    private boolean xaTransacted = true; 

    // Should we have one for each connection
    private PrintWriter logWriter = null;
    private JmsLogger logger = new JmsLogger();
    
    // Holds all current JmsSession handles
    private Set handles =  new HashSet();

    // The event listeners
    Vector listeners = new Vector();
    
    public JmsManagedConnection(JmsManagedConnectionFactory mcf,
				ConnectionRequestInfo info,
				String user, 
				String pwd) throws ResourceException 
    {
	this.mcf = mcf;
	this.info = (JmsConnectionRequestInfo)info;
	this.user = user;
	this.pwd = pwd;

	setUp();
	
    }

    //---- ManagedConnection API ----

    /**
     Get the physical connection handler.
 
     This bummer will be called in two situations. 
     1. When a new mc has bean created and a connection is needed
     2. When an mc has been fetched from the pool (returned in match*)

     It may also be called multiple time without a cleanup, to support
     connection sharing
     */
    public Object getConnection(Subject subject, 
                                ConnectionRequestInfo info) 
        throws ResourceException {

	// Check user first
	JmsCred cred = JmsCred.getJmsCred(mcf,subject,info);

	// Null users are allowed!
	if (user != null && !user.equals(cred.name) )
	    throw new SecurityException("Password credentials not the same, reauthentication not allowed");
	if (cred.name != null && user == null) 
	    throw new SecurityException("Password credentials not the same, reauthentication not allowed");
	
	user = cred.name; //Basically meaningless
	
	if(isDestroyed)
	    throw new IllegalStateException("ManagedConnection already destroyd");
	// Create a handle
	JmsSession handle = new JmsSession(this);
	handles.add(handle);
	return handle;
	
    }
    
    /**
     * Destroy the physical connection
     */
    public void destroy() throws ResourceException {
	if (isDestroyed) return;
	isDestroyed = true;
	
	// Destroy all handles
	Iterator h = handles.iterator();
	while(h.hasNext()) {
	    ((JmsSession)h.next()).destroy();
	}
	handles.clear();
	try {
	    // Close session and connection
	    if(info.isTopic()) {
		topicSession.close();
		if(xaTransacted) 
		    xaTopicSession.close();
	    } else {
		queueSession.close();
		if(xaTransacted) 
		    xaQueueSession.close();
	    }
	    con.close();
	}catch(JMSException ex) {
	    ResourceException e = new ResourceException("Could not properly close the session and connection: " + ex);
	    e.setLinkedException(ex);
	    throw e;
	}
    }
    

    /**
     * Cleans up the, from the spec
     - The cleanup of ManagedConnection instance resets its client specific state.

     Does that mean that autentication should be redone. FIXME
     */
    public void cleanup() throws ResourceException {
	
	if(isDestroyed)
	    throw new IllegalStateException("ManagedConnection already destroyd");
	// 
	Iterator h = handles.iterator();
	while(h.hasNext()) {
	    ((JmsSession)h.next()).destroy();
	}
	handles.clear();
	
    }


    /**
     * Move a handler from one mc to this one
     */ 
    
    public void associateConnection(Object connection)
        throws ResourceException {

	// Should we check auth, ie user and pwd? FIXME
        if(!isDestroyed &&
	   connection instanceof JmsSession) {
	    JmsSession h = (JmsSession) connection;
            h.setManagedConnection(this);
	    handles.add(h);
	}else {
	    throw new IllegalStateException("ManagedConnection in an illegal state");
	}

    }


    public void addConnectionEventListener(ConnectionEventListener listener) {
	logger.log(Level.FINE,"ConnectionEvent listener added");
        listeners.addElement(listener);
    }


    public void removeConnectionEventListener(ConnectionEventListener listener) {
	
        listeners.removeElement(listener);
    }
    
    
    public XAResource getXAResource() throws ResourceException {
	/* Spec says a mc must allways return the same XA resource, so we
	   chaches it
	*/
	
	if (! xaTransacted)
	    	throw new NotSupportedException("XA transaction not supported");
	
	if (xaResource == null) {
	    if (info.isTopic())
		xaResource = xaTopicSession.getXAResource();
	    else
		xaResource = xaQueueSession.getXAResource();
	}
	logger.log(Level.FINE, "Leaving out XAResource");
	return xaResource;
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
	logger.log(Level.FINE, "Leaving out LocalTransaction");
	return new JmsLocalTransaction(this);
    }
    
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        if(isDestroyed)
	    throw new IllegalStateException("ManagedConnection already destroyd");
        return new JmsMetaData(this);
    }
    
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
	if (out != null)
	    logger.setLogWriter(out);
    }
    
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }


    // --- Api to JmsSession
    protected Session getSession() {
	if (info.isTopic()) {
	    return topicSession;
	} else {
	    return queueSession;
	}
    }

    protected JmsLogger getLogger() {
	return logger;
    }

    protected void sendEvent(ConnectionEvent ev) {
	logger.log(Level.FINE,"Sending connection event: " + ev.getId());
	Vector list = (Vector) listeners.clone();
	int size = list.size();
        for (int i=0; i<size; i++) {
	    ConnectionEventListener listener = 
                (ConnectionEventListener) list.elementAt(i);
	    int type = ev.getId();
	    switch (type) {
            case ConnectionEvent.CONNECTION_CLOSED:
                listener.connectionClosed(ev);
                break;
            case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                listener.localTransactionStarted(ev);
                break;
            case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                listener.localTransactionCommitted(ev);
                break;
            case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                listener.localTransactionRolledback(ev);
                break;
            case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
                listener.connectionErrorOccurred(ev);
                break;
            default:
                throw new IllegalArgumentException("Illegal eventType: " +
                                                   type);
            }
	}
    }
   

    protected void removeHandle(JmsSession handle) {
	handles.remove(handle);
    }

    //--- Used by MCF
    protected ConnectionRequestInfo getInfo() {
	return info;
    }

    protected JmsManagedConnectionFactory getManagedConnectionFactory() {
	return mcf;
    }
   

    // --- Used bu MetaData
    protected String getUserName() {
	return user;
    }
    //---- Private helper methods
    private void setUp() throws ResourceException
    {
	try {
	    JMSProviderAdapter adapter;
	    if (mcf.getJmsProviderAdapterJNDI() != null) {
	    
		// Get initial context
		Context serverCtx = new InitialContext();
		//Lokup adapter
		adapter = (JMSProviderAdapter)serverCtx.
		    lookup(mcf.getJmsProviderAdapterJNDI());
	    } else {
		adapter = mcf.getJmsProviderAdapter();
	    }

	    Context context = adapter.getInitialContext();
	    
	    // Get connections
	    if (info.isTopic()) {
		// Get connectionFactory
		TopicConnectionFactory topicFactory = 
		    (TopicConnectionFactory)context.
		    lookup(adapter.getTopicFactoryName());
		
	    // Set up connection
		if(user != null) 
		    {
			logger.log(Level.FINE, "Creating topic connection with user: " + user + " passwd: " + pwd);
			con = topicFactory.
			    createTopicConnection(user, pwd);
		    }
		else 
		    {
			logger.log(Level.FINE, "Creating topic connection");
			con = topicFactory.createTopicConnection();
		    }
		
	    } else {
		// Get connectionFactory
		QueueConnectionFactory queueFactory = 
		    (QueueConnectionFactory)context.
		    lookup(adapter.getQueueFactoryName());
		
		// Queue connection
		if (user != null) 
		    {
			logger.log(Level.FINE, "Creating queue connection with user: " + user + " passwd: " + pwd); 
			con = queueFactory.
			    createQueueConnection(user,pwd);
		    } 
		else 
		    {
			logger.log(Level.FINE, "Creating queue connection");
			con = queueFactory.createQueueConnection();
		    }
	    }
	    
	    // Get sessions
	    boolean transacted = true;
	    int ack = Session.AUTO_ACKNOWLEDGE;
	    
	    if (con instanceof XATopicConnection) {
		xaTopicSession = ((XATopicConnection)con).createXATopicSession();
		topicSession = xaTopicSession.getTopicSession();
		xaTransacted = true;
	    } else if(con instanceof XAQueueConnection) {
		xaQueueSession = ((XAQueueConnection)con).createXAQueueSession();
		queueSession = xaQueueSession.getQueueSession();
		xaTransacted = true;
	    } else if (con instanceof TopicConnection) {
		topicSession = ((TopicConnection)con).createTopicSession(transacted, ack);
		logger.log(Level.WARNING,"WARNING: Using a non-XA TopicConnection.  It will not be able to participate in a Global UOW");
	    } else if(con instanceof QueueConnection) {
		queueSession = ((QueueConnection)con).createQueueSession(transacted, ack);
		logger.log(Level.WARNING,"WARNING: Using a non-XA QueueConnection.  It will not be able to participate in a Global UOW");
	    } else {
		logger.log(Level.SEVERE,"Error in getting session for con: " + con);
		throw new ResourceException("Connection was not reconizable: " + con);
	    }
	    
	}catch(NamingException ne) {
	    CommException ce = new CommException(ne.toString());
	    ce.setLinkedException(ne);
	    throw ce;
	}catch(JMSException je) {
	    CommException ce = new CommException(je.toString());
	    ce.setLinkedException(je);
	    throw ce;
	}
    }
    
} // JmsManagedConnection
    
    
    



