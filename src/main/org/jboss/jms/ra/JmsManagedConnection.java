/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

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

import org.jboss.jms.ConnectionFactoryHelper;
import org.jboss.jms.jndi.JMSProviderAdapter;

import org.jboss.logging.Logger;

/**
 * Managed Connection, manages one or more JMS sessions.
 * 
 * <p>Every ManagedConnection will have a physical JMSConnection under the
 *    hood. This may leave out several session, as specifyed in 5.5.4 Multiple
 *    Connection Handles. Thread safe semantics is provided. 
 * <p>Hm. If we are to follow the example in 6.11 this will not work. We would
 *    have to use the SAME session. This means we will have to guard against
 *    concurrent access. We use a stack, and only allowes the handle at the
 *    top of the stack to do things.
 *
 * <p>As to transactions we some fairly hairy alternatives to handle:
 *    XA - we get an XA. We may now only do transaction through the
 *    XAResource, since a XASession MUST throw exceptions in commit etc. But
 *    since XA support implies LocatTransaction support, we will have to use
 *    the XAResource in the LocalTransaction class.
 *    LocalTx - we get a normal session. The LocalTransaction will then work
 *    against the normal session api.
 *
 * <p>An invokation of JMS MAY BE DONE in none transacted context. What do we 
 *    do then? How much should we leave to the user???
 *
 * <p>One possible solution is to use transactions any way, but under the hood.
 *    If not LocalTransaction or XA has been aquired by the container, we have
 *    to do the commit in send and publish. (CHECK is the container required 
 *    to get a XA every time it uses a managed connection? No its is not, only 
 *    at creation!)
 *
 * <p>Does this mean that a session one time may be used in a transacted env, 
 *    and another time in a not transacted.
 *
 * <p>Maybe we could have this simple rule:
 *
 * <p>If a user is going to use non trans:
 * <ul>
 * <li>mark that i ra deployment descr
 * <li>Use a JmsProviderAdapter with non XA factorys
 * <li>Mark session as non transacted (this defeats the purpose of specifying
 * <li>trans attrinbutes in deploy descr NOT GOOD
 * </ul>
 *
 * <p>From the JMS tutorial:
 *    "When you create a session in an enterprise bean, the container ignores
 *    the arguments you specify, because it manages all transactional 
 *    properties for enterprise beans."
 *
 * <p>And further:
 *    "You do not specify a message acknowledgment mode when you create a
 *    message-driven bean that uses container-managed transactions. The
 *    container handles acknowledgment automatically."
 *
 * <p>On Session or Connection:
 * <p>From Tutorial:
 *    "A JMS API resource is a JMS API connection or a JMS API session." But in
 *    the J2EE spec only connection is considered a resource.
 * 
 * <p>Not resolved: connectionErrorOccurred: it is verry hard to know from the
 *    exceptions thrown if it is a connection error. Should we register an
 *    ExceptionListener and mark al handles as errounous? And then let them 
 *    send the event and throw an exception?
 *
 * <p>Created: Tue Apr 10 13:09:45 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.8 $
 */
public class JmsManagedConnection
   implements ManagedConnection
{
   private static final Logger log = Logger.getLogger(JmsManagedConnection.class);
   
   private JmsManagedConnectionFactory mcf;
   private JmsConnectionRequestInfo info;
   private String user; // = null;
   private String pwd; // = null;
   private boolean isDestroyed; // = false;

   // Physical JMS connection stuff
   private Connection con;
   private TopicSession topicSession;
   private XATopicSession xaTopicSession;
   private QueueSession queueSession;
   private XAQueueSession xaQueueSession;
   private XAResource xaResource;
   private boolean xaTransacted; // = false;

   /** Holds all current JmsSession handles. */
   private Set handles = new HashSet();

   /** The event listeners */
   private Vector listeners = new Vector();

   /**
    * Create a <tt>JmsManagedConnection</tt>.
    *
    * @param mcf
    * @param info
    * @param user
    * @param pwd
    *
    * @throws ResourceException
    */
   public JmsManagedConnection(final JmsManagedConnectionFactory mcf,
                               final ConnectionRequestInfo info,
                               final String user, 
                               final String pwd)
      throws ResourceException 
   {
      this.mcf = mcf;

      // seem like its asking for trouble here
      this.info = (JmsConnectionRequestInfo)info;
      this.user = user;
      this.pwd = pwd;

      setup();
   }

   //---- ManagedConnection API ----

   /**
    * Get the physical connection handler.
    * 
    * <p>This bummer will be called in two situations:
    * <ol>
    * <li>When a new mc has bean created and a connection is needed
    * <li>When an mc has been fetched from the pool (returned in match*)
    * </ol>
    *
    * <p>It may also be called multiple time without a cleanup, to support
    *    connection sharing.
    *
    * @param subject
    * @param info
    * @return           A new connection object.
    *
    * @throws ResourceException
    */
   public Object getConnection(final Subject subject, 
                               final ConnectionRequestInfo info) 
      throws ResourceException
   {
      // Check user first
      JmsCred cred = JmsCred.getJmsCred(mcf,subject,info);

      // Null users are allowed!
      if (user != null && !user.equals(cred.name)) {
         throw new SecurityException
            ("Password credentials not the same, reauthentication not allowed");
      }
      if (cred.name != null && user == null) {
         throw new SecurityException
            ("Password credentials not the same, reauthentication not allowed");
      }
	
      user = cred.name; // Basically meaningless
	
      if (isDestroyed) {
         throw new IllegalStateException("ManagedConnection already destroyd");
      }
      
      // Create a handle
      JmsSession handle = new JmsSession(this);
      handles.add(handle);
      return handle;
   }

   /**
    * Destroy all handles.
    *
    * @throws ResourceException    Failed to close one or more handles.
    */
   private void destroyHandles() throws ResourceException {
      Iterator iter = handles.iterator();
      
      while (iter.hasNext()) {
         ((JmsSession)iter.next()).destroy();
      }

      // clear the handles map
      handles.clear();
   }
   
   /**
    * Destroy the physical connection.
    *
    * @throws ResourceException    Could not property close the session and
    *                              connection.
    */
   public void destroy() throws ResourceException {
      if (isDestroyed) return;
      isDestroyed = true;

      // destory handles
      destroyHandles();
      
      try {
         // Close session and connection
         if (info.isTopic()) {
            topicSession.close();
            if (xaTransacted) {
               xaTopicSession.close();
            }
         }
         else {
            queueSession.close();
            if (xaTransacted) {
               xaQueueSession.close();
            }
         }
         con.close();
      }
      catch (JMSException ex) {
         ResourceException e = new ResourceException
            ("Could not properly close the session and connection: " + ex);
         e.setLinkedException(ex);
         throw e;
      }
   }

   /**
    * Cleans up the, from the spec
    *  - The cleanup of ManagedConnection instance resets its client specific
    *    state.
    *
    * Does that mean that autentication should be redone. FIXME
    */
   public void cleanup() throws ResourceException {
      if (isDestroyed) {
         throw new IllegalStateException("ManagedConnection already destroyd");
      }

      // destory handles      
      destroyHandles();
   }

   /**
    * Move a handler from one mc to this one.
    *
    * @param obj   An object of type JmsSession.
    *
    * @throws ResourceException        Failed to associate connection.
    * @throws IllegalStateException    ManagedConnection in an illegal state.
    */ 
   public void associateConnection(final Object obj)
      throws ResourceException
   {
      //
      // Should we check auth, ie user and pwd? FIXME
      //
      
      if (!isDestroyed && obj instanceof JmsSession) {
         JmsSession h = (JmsSession)obj;
         h.setManagedConnection(this);
         handles.add(h);
      }
      else {
         throw new IllegalStateException
            ("ManagedConnection in an illegal state");
      }
   }

   /**
    * Add a connection event listener.
    *
    * @param l   The connection event listener to be added.
    */
   public void addConnectionEventListener(final ConnectionEventListener l) {
      listeners.addElement(l);
      
      if (log.isDebugEnabled()) {
         log.debug("ConnectionEvent listener added: " + l);
      }
   }

   /**
    * Remove a connection event listener.
    *
    * @param l    The connection event listener to be removed.
    */
   public void removeConnectionEventListener(final ConnectionEventListener l) {
      listeners.removeElement(l);
   }

   /**
    * Get the XAResource for the connection.
    *
    * @return   The XAResource for the connection.
    *
    * @throws ResourceException    XA transaction not supported
    */
   public XAResource getXAResource() throws ResourceException {
      //
      // Spec says a mc must allways return the same XA resource, 
      // so we cache it.
      //
      if (!xaTransacted) {
         throw new NotSupportedException("XA transaction not supported");
      }
	
      if (xaResource == null) {
         if (info.isTopic()) {
            xaResource = xaTopicSession.getXAResource();
         }
         else {
            xaResource = xaQueueSession.getXAResource();
         }
      }
      log.debug("Leaving out XAResource");

      return xaResource;
   }

   /**
    * Get the location transaction for the connection.
    *
    * @return    The local transaction for the connection.
    *
    * @throws ResourceException
    */
   public LocalTransaction getLocalTransaction() throws ResourceException {
      log.debug("Leaving out LocalTransaction");
      return new JmsLocalTransaction(this);
   }

   /**
    * Get the meta data for the connection.
    *
    * @return    The meta data for the connection.
    *
    * @throws ResourceException
    * @throws IllegalStateException    ManagedConnection already destroyed.
    */
   public ManagedConnectionMetaData getMetaData() throws ResourceException {
      if (isDestroyed) {
         throw new IllegalStateException("ManagedConnection already destroyd");
      }
      
      return new JmsMetaData(this);
   }

   /**
    * Set the log writer for this connection.
    *
    * @param out   The log writer for this connection.
    *
    * @throws ResourceException
    */
   public void setLogWriter(final PrintWriter out) throws ResourceException {
      log.debug("Ignoring call to setLogWriter()");
   }
    
   /**
    * Get the log writer for this connection.
    *
    * @return   Always null
    */
   public PrintWriter getLogWriter() throws ResourceException {
      return null;
   }

   // --- Api to JmsSession

   /**
    * Get the session for this connection.
    *
    * @return   Either a topic or queue connection.
    */
   protected Session getSession() {
      if (info.isTopic()) {
         return topicSession;
      }
      else {
         return queueSession;
      }
   }

   /**
    * Send an event.
    *
    * @param event    The event to send.
    */
   protected void sendEvent(final ConnectionEvent event) {
      log.debug("Sending connection event: " + event.getId());

      Vector list = (Vector) listeners.clone();
      int size = list.size();
      
      for (int i=0; i<size; i++) {
         ConnectionEventListener listener = 
            (ConnectionEventListener)list.elementAt(i);
         int type = event.getId();
         
         switch (type) {
          case ConnectionEvent.CONNECTION_CLOSED:
             listener.connectionClosed(event);
             break;
             
          case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
             listener.localTransactionStarted(event);
             break;
             
          case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
             listener.localTransactionCommitted(event);
             break;
             
          case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
             listener.localTransactionRolledback(event);
             break;
             
          case ConnectionEvent.CONNECTION_ERROR_OCCURRED:
             listener.connectionErrorOccurred(event);
             break;
             
          default:
             throw new IllegalArgumentException("Illegal eventType: " + type);
         }
      }
   }

   /**
    * Remove a handle from the handle map.
    *
    * @param handle     The handle to remove.
    */
   protected void removeHandle(final JmsSession handle) {
      handles.remove(handle);
   }

   // --- Used by MCF

   /**
    * Get the request info for this connection.
    *
    * @return    The request info for this connection.
    */
   protected ConnectionRequestInfo getInfo() {
      return info;
   }

   /**
    * Get the connection factory for this connection.
    *
    * @return    The connection factory for this connection.
    */
   protected JmsManagedConnectionFactory getManagedConnectionFactory() {
      return mcf;
   }

   // --- Used by MetaData

   /**
    * Get the user name for this connection.
    *
    * @return    The user name for this connection.
    */
   protected String getUserName() {
      return user;
   }
   
   // --- Private helper methods

   /**
    * Get the JMS provider adapter that will be used to create JMS
    * resources.
    *
    * @return    A JMS provider adapter.
    *
    * @throws NamingException    Failed to lookup provider adapter.
    */
   private JMSProviderAdapter getProviderAdapter() throws NamingException {
      JMSProviderAdapter adapter;
      
      if (mcf.getJmsProviderAdapterJNDI() != null) {
         // lookup the adapter from JNDI
         Context ctx = new InitialContext();
         try {
            adapter = (JMSProviderAdapter)
               ctx.lookup(mcf.getJmsProviderAdapterJNDI());
         }
         finally {
            ctx.close();
         }
      }
      else {
         adapter = mcf.getJmsProviderAdapter();
      }

      return adapter;
   }
   
   /**
    * Setup the connection.
    *
    * @throws ResourceException
    */
   private void setup() throws ResourceException
   {
      try {
         JMSProviderAdapter adapter = getProviderAdapter();
         Context context = adapter.getInitialContext();
         Object factory;
         boolean transacted = true;
         int ack = Session.AUTO_ACKNOWLEDGE;

         if (info.isTopic()) {
            factory = context.lookup(adapter.getTopicFactoryRef());
            con = ConnectionFactoryHelper.createTopicConnection(factory,
                                                                user,
                                                                pwd);
            log.debug("created connection: " + con);

            if (con instanceof XATopicConnection) {
               xaTopicSession = ((XATopicConnection)con).createXATopicSession();
               topicSession = xaTopicSession.getTopicSession();
               xaTransacted = true;
            }
            else if (con instanceof TopicConnection) {
               topicSession =
                  ((TopicConnection)con).createTopicSession(transacted, ack);
               log.debug("Using a non-XA TopicConnection.  " +
                         "It will not be able to participate in a Global UOW");
            }
            else {
               log.error("Error in getting session for con: " + con);
               throw new ResourceException
                  ("Connection was not reconizable: " + con);
            }

            log.debug("xaTopicSession: " + xaTopicSession);
            log.debug("topicSession: " + topicSession);
         }
         else { // isQueue
            factory = context.lookup(adapter.getQueueFactoryRef());
            con = ConnectionFactoryHelper.createQueueConnection(factory,
                                                                user,
                                                                pwd);
            log.debug("created connection: " + con);

            if (con instanceof XAQueueConnection) {
               xaQueueSession =
                  ((XAQueueConnection)con).createXAQueueSession();
               queueSession = xaQueueSession.getQueueSession();
               xaTransacted = true;
            }
            else if (con instanceof QueueConnection) {
               queueSession =
                  ((QueueConnection)con).createQueueSession(transacted, ack);
               log.debug("Using a non-XA QueueConnection.  " +
                         "It will not be able to participate in a Global UOW");
            }
            else {
               log.error("Error in getting session for con: " + con);
               throw new ResourceException
                  ("Connection was not reconizable: " + con);
            }

            log.debug("xaQueueSession: " + xaQueueSession);
            log.debug("queueSession: " + queueSession);
         }
	 con.start();
         log.debug("transacted: " + transacted);
         log.debug("ack mode: " + ack);
      }
      catch (NamingException e) {
         CommException ce = new CommException(e.toString());
         ce.setLinkedException(e);
         throw ce;
      }
      catch (JMSException e) {
         CommException ce = new CommException(e.toString());
         ce.setLinkedException(e);
         throw ce;
      }
   }
}
