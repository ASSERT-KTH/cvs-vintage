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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.MessageListener;
import javax.jms.TopicConnection;
import javax.jms.XATopicConnection;
import javax.jms.QueueConnection;
import javax.jms.XAQueueConnection;
import javax.jms.Session;
import javax.jms.XASession;
import javax.jms.XAQueueSession;
import javax.jms.XATopicSession;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;

import org.apache.log4j.Category;

/**
 * Implementation of ServerSessionPool.
 *
 * <p>Created: Thu Dec  7 17:02:03 2000
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.12 $
 */
public class StdServerSessionPool
   implements ServerSessionPool
{
   /** The default size of the pool. */
   private static final int DEFAULT_POOL_SIZE = 15;

   /** The thread group which session workers will run. */
   private static ThreadGroup threadGroup =
      new ThreadGroup("ASF Session Pool Threads");

   /** Instance logger. */
   private final Category log = Category.getInstance(this.getClass());

   /** The size of the pool. */
   private int poolSize;

   /** The message acknowledgment mode. */
   private int ack;

   /** True if this is a transacted session. */
   private boolean transacted;

   /** The session connection. */
   private Connection con;

   /** The message listener for the session. */
   private MessageListener listener;

   /** The list of ServerSessions. */
   private List sessionPool;

   /** The executor for processing messages? */
   private PooledExecutor executor;

   /** Used to signal when the Pool is being closed down */
   private boolean closing = false;

   /** Used during close down to wait for all server sessions to be returned and closed.*/
   private int numServerSessions = 0;

   /**
    * Construct a <tt>StdServerSessionPool</tt> using the default
    * pool size.
    *
    * @param con
    * @param transacted
    * @param ack
    * @param listener
    */
   public StdServerSessionPool(final Connection con,
                               final boolean transacted,
                               final int ack,
                               final MessageListener listener)
      throws JMSException
   {
      this(con, transacted, ack, listener, DEFAULT_POOL_SIZE);
   }

   /**
    * Construct a <tt>StdServerSessionPool</tt> using the default
    * pool size.
    *
    * @param con
    * @param transacted
    * @param ack
    * @param listener
    * @param maxSession
    */
   public StdServerSessionPool(final Connection con,
                               final boolean transacted,
                               final int ack,
                               final MessageListener listener,
                               final int maxSession)
      throws JMSException
   {
      this.con = con;
      this.ack = ack;
      this.listener = listener;
      this.transacted = transacted;
      this.poolSize = maxSession;
      this.sessionPool = new ArrayList(maxSession);

      // setup the worker pool
      executor = new PooledExecutor(poolSize);
      executor.setMinimumPoolSize(0);
      executor.setKeepAliveTime(1000*30);
      executor.waitWhenBlocked();
      executor.setThreadFactory(new ThreadFactory() {
            private volatile int count = 0;
            
            public Thread newThread(final Runnable command) {
               return new Thread(threadGroup,
                                 command,
                                 "Thread Pool Worker-" + count++);
            }
         });

      // finish initializing the session
      init();
      log.debug("Server Session pool set up");
   }

   // --- JMS API for ServerSessionPool

   /**
    * Get a server session.
    *
    * @return    A server session.
    *
    * @throws JMSException    Failed to get a server session.
    */
   public ServerSession getServerSession() throws JMSException
   {
      log.debug("getting a server session");
      ServerSession session = null;

      try {
         while (true) {
            synchronized (sessionPool) {
               if(closing){
                  throw new JMSException("Cannot get session after pool has been closed down.");
               }
               else if (sessionPool.size() > 0) {
                  session = (ServerSession)sessionPool.remove(0);
                  break;
               }
               else {
                  try {
                     sessionPool.wait();
                  }
                  catch (InterruptedException ignore) {}
               }
            }
         }
      }
      catch (Exception e) {
         throw new JMSException("Failed to get a server session: " + e);
      }

      // assert session != null

      log.debug("using server session: " + session);
      return session;
   }

   // --- Protected messages for StdServerSession to use

   /**
    * Returns true if this server session is transacted.
    */
   boolean isTransacted() {
      return transacted;
   }

   /**
    * Recycle a server session.
    */
   void recycle(StdServerSession session) {
      synchronized (sessionPool) {
         if(closing){
           session.close();
           numServerSessions--;
           if(numServerSessions == 0)  //notify clear thread.
              sessionPool.notifyAll();
         }else{
           sessionPool.add(session);
           sessionPool.notifyAll();
           log.debug("recycled server session: " + session);
         }
      }
   }

   /**
    * Get the executor we are using.
    */
   Executor getExecutor() {
      return executor;
   }

   /**
    * Clear the pool, clear out both threads and ServerSessions,
    * connection.stop() should be run before this method.
    */
   public void clear() {
      synchronized (sessionPool) {
         // FIXME - is there a runaway condition here. What if a
         // ServerSession are taken by a ConnecionConsumer? Should we set
         // a flag somehow so that no ServerSessions are recycled and the
         // ThreadPool won't leave any more threads out.
         closing = true;

         if (log.isDebugEnabled()) {
            log.debug("Clearing " + sessionPool.size() +
                      " from ServerSessionPool");
         }

         Iterator iter = sessionPool.iterator();
         while (iter.hasNext()) {
            StdServerSession ses = (StdServerSession)iter.next();
            // Should we do anything to the server session?
            ses.close();
            numServerSessions--;
         }

         sessionPool.clear();
         sessionPool.notifyAll();
      }

      //Must be outside synchronized block because of recycle method.
      executor.shutdownAfterProcessingCurrentlyQueuedTasks();

      //wait for all server sessions to be returned.
      synchronized(sessionPool){
         while(numServerSessions > 0)
            try{ sessionPool.wait(); }catch(InterruptedException ignore){}
      }
   }

   // --- Private methods used internally

   private void init() throws JMSException
   {
      for (int index = 0; index < poolSize; index++) {
         // Here is the meat, that MUST follow the spec
         Session ses = null;
         XASession xaSes = null;

         log.debug("initializing with connection: " + con);

         if (con instanceof XATopicConnection) {
            xaSes = ((XATopicConnection)con).createXATopicSession();
            ses = ((XATopicSession)xaSes).getTopicSession();
         }
         else if (con instanceof XAQueueConnection) {
            xaSes = ((XAQueueConnection)con).createXAQueueSession();
            ses = ((XAQueueSession)xaSes).getQueueSession();
         }
         else if (con instanceof TopicConnection) {
            ses = ((TopicConnection)con).createTopicSession(transacted, ack);
            log.warn("Using a non-XA TopicConnection.  " +
                     "It will not be able to participate in a Global UOW");
         }
         else if (con instanceof QueueConnection) {
            ses = ((QueueConnection)con).createQueueSession(transacted, ack);
            log.warn("Using a non-XA QueueConnection.  " +
                     "It will not be able to participate in a Global UOW");
         }
         else {
            // should never happen really
            log.error("Connection was not reconizable: " + con);
            throw new JMSException("Connection was not reconizable: " + con);
         }

         // This might not be totaly spec compliant since it says that app
         // server should create as many message listeners its needs.
         log.debug("setting session listener: " + listener);
         ses.setMessageListener(listener);

         // create the server session and add it to the pool
         ServerSession serverSession = new StdServerSession(this, ses, xaSes);
         sessionPool.add(serverSession);
         numServerSessions++;
         log.debug("added server session to the pool: " + serverSession);
      }
   }
}
