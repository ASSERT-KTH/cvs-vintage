/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.asf;
import EDU.oswego.cs.dl.util.concurrent.Executor;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.ServerSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueSession;
import javax.jms.XASession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicSession;

import org.jboss.logging.Logger;
import org.jboss.tm.XidFactoryMBean;

/**
 * Implementation of ServerSessionPool. <p>
 *
 * Created: Thu Dec 7 17:02:03 2000
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a> .
 * @version   $Revision: 1.26 $
 */
public class StdServerSessionPool
       implements ServerSessionPool
{
   /**
    * The thread group which session workers will run.
    */
   private static ThreadGroup threadGroup =
         new ThreadGroup("ASF Session Pool Threads");

   /**
    * Instance logger.
    */
   private final Logger log = Logger.getLogger(this.getClass());

   /** The minimum size of the pool */
   private int minSize;

   /**
    * The size of the pool.
    */
   private int poolSize;

   /** How long to keep sessions alive */
   private long keepAlive;

   /**
    * The message acknowledgment mode.
    */
   private int ack;

   /**
    * Is the bean container managed?
    */
   private boolean useLocalTX;

   /**
    * True if this is a transacted session.
    */
   private boolean transacted;

   /**
    * The destination.
    */
   private Destination destination;

   /**
    * The session connection.
    */
   private Connection con;

   /**
    * The message listener for the session.
    */
   private MessageListener listener;

   /**
    * The list of ServerSessions.
    */
   private List sessionPool;

   /**
    * The executor for processing messages?
    */
   private PooledExecutor executor;

   /**
    * Used to signal when the Pool is being closed down
    */
   private boolean closing = false;

   /**
    * Used during close down to wait for all server sessions to be returned and
    * closed.
    */
   private int numServerSessions = 0;

   private XidFactoryMBean xidFactory;

   /**
    * Construct a <tt>StdServerSessionPool</tt> using the default pool size.
    *
    * @param destination the destination
    * @param con connection to get sessions from
    * @param transacted transaction mode when not XA (
    * @param ack ackmode when not XA
    * @param listener the listener the sessions will call
    * @param minSession minumum number of sessions in the pool
    * @param maxSession maximum number of sessions in the pool
    * @param keepAlive the time to keep sessions alive
    * @param xidFactory  Description of Parameter
    * @exception JMSException    Description of Exception
    */
   public StdServerSessionPool(final Destination destination,
                               final Connection con,
                               final boolean transacted,
                               final int ack,
                               final boolean useLocalTX,
                               final MessageListener listener,
                               final int minSession,
                               final int maxSession,
                               final long keepAlive,
                               final XidFactoryMBean xidFactory)
      throws JMSException
   {
      this.destination = destination;
      this.con = con;
      this.ack = ack;
      this.listener = listener;
      this.transacted = transacted;
      this.minSize = minSession;
      this.poolSize = maxSession;
      this.keepAlive = keepAlive;
      this.sessionPool = new ArrayList(maxSession);
      this.useLocalTX = useLocalTX;
      this.xidFactory = xidFactory;
      // setup the worker pool
      executor = new MyPooledExecutor(poolSize);
      executor.setMinimumPoolSize(minSize);
      executor.setKeepAliveTime(keepAlive);
      executor.waitWhenBlocked();
      executor.setThreadFactory(new DefaultThreadFactory());

      // finish initializing the session
      create();
      log.debug("Server Session pool set up");
   }

   // --- JMS API for ServerSessionPool

   /**
    * Get a server session.
    *
    * @return               A server session.
    * @throws JMSException  Failed to get a server session.
    */
   public ServerSession getServerSession() throws JMSException
   {
      if( log.isTraceEnabled() )
         log.trace("getting a server session");
      ServerSession session = null;

      try
      {
         while (true)
         {
            synchronized (sessionPool)
            {
               if (closing)
               {
                  throw new JMSException("Cannot get session after pool has been closed down.");
               }
               else if (sessionPool.size() > 0)
               {
                  session = (ServerSession)sessionPool.remove(0);
                  break;
               }
               else
               {
                  try
                  {
                     sessionPool.wait();
                  }
                  catch (InterruptedException ignore)
                  {
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         throw new JMSException("Failed to get a server session: " + e);
      }

      // assert session != null
      if( log.isTraceEnabled() )
         log.trace("using server session: " + session);
      return session;
   }

   /**
    * Clear the pool, clear out both threads and ServerSessions,
    * connection.stop() should be run before this method.
    */
   public void clear()
   {
      synchronized (sessionPool)
      {
         // FIXME - is there a runaway condition here. What if a
         // ServerSession are taken by a ConnecionConsumer? Should we set
         // a flag somehow so that no ServerSessions are recycled and the
         // ThreadPool won't leave any more threads out.
         closing = true;

         if (log.isDebugEnabled())
         {
            log.debug("Clearing " + sessionPool.size() +
                  " from ServerSessionPool");
         }

         Iterator iter = sessionPool.iterator();
         while (iter.hasNext())
         {
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
      synchronized (sessionPool)
      {
         while (numServerSessions > 0)
         {
            try
            {
               sessionPool.wait();
            }
            catch (InterruptedException ignore)
            {
            }
         }
      }
   }

   /**
    * Get the executor we are using.
    *
    * @return   The Executor value
    */
   Executor getExecutor()
   {
      return executor;
   }

   // --- Protected messages for StdServerSession to use

   /**
    * Returns true if this server session is transacted.
    *
    * @return   The Transacted value
    */
   boolean isTransacted()
   {
      return transacted;
   }

   /**
    * Recycle a server session.
    *
    * @param session  Description of Parameter
    */
   void recycle(StdServerSession session)
   {
      synchronized (sessionPool)
      {
         if (closing)
         {
            session.close();
            numServerSessions--;
            if (numServerSessions == 0)
            {
               //notify clear thread.
               sessionPool.notifyAll();
            }
         }
         else
         {
            sessionPool.add(session);
            sessionPool.notifyAll();
            if( log.isTraceEnabled() )
               log.trace("recycled server session: " + session);
         }
      }
   }

   // --- Private methods used internally

   private void create() throws JMSException
   {
      boolean debug = log.isDebugEnabled();

      for (int index = 0; index < poolSize; index++)
      {
         // Here is the meat, that MUST follow the spec
         Session ses = null;
         XASession xaSes = null;

         if (debug)
            log.debug("initializing with connection: " + con);

         if (destination instanceof Topic && con instanceof XATopicConnection)
         {
            xaSes = ((XATopicConnection)con).createXATopicSession();
            ses = ((XATopicSession)xaSes).getTopicSession();
         }
         else if (destination instanceof Queue && con instanceof XAQueueConnection)
         {
            xaSes = ((XAQueueConnection)con).createXAQueueSession();
            ses = ((XAQueueSession)xaSes).getQueueSession();
         }
         else if (destination instanceof Topic && con instanceof TopicConnection)
         {
            ses = ((TopicConnection)con).createTopicSession(transacted, ack);
            log.warn("Using a non-XA TopicConnection.  " +
                  "It will not be able to participate in a Global UOW");
         }
         else if (destination instanceof Queue && con instanceof QueueConnection)
         {
            ses = ((QueueConnection)con).createQueueSession(transacted, ack);
            log.warn("Using a non-XA QueueConnection.  " +
                  "It will not be able to participate in a Global UOW");
         }
         else
         {
            throw new JMSException("Connection was not reconizable: " + con + " for destination " + destination);
         }

         // create the server session and add it to the pool - it is up to the
         // server session to set the listener
         StdServerSession serverSession = new StdServerSession(this, ses, xaSes,
            listener, useLocalTX, xidFactory);

         sessionPool.add(serverSession);
         numServerSessions++;
         if (debug)
            log.debug("added server session to the pool: " + serverSession);
      }
   }

   /**
    * A pooled executor where the minimum pool size
    * threads are kept alive
    */
   private static class MyPooledExecutor extends PooledExecutor
   {
      public MyPooledExecutor(int poolSize)
      {
         super(poolSize);
      }
      
      protected Runnable getTask() throws InterruptedException
      {
         Runnable task = null;
         while ((task = super.getTask()) == null && keepRunning());
         return task;
      }
      
      /**
       * We keep running unless we are told to shutdown
       * or there are more than minimumPoolSize_ threads in the pool
       * 
       * @return whether to keep running
       */
      protected synchronized boolean keepRunning()
      {
         if (shutdown_)
            return false;
         
         return poolSize_ <= minimumPoolSize_;
      }
   }

   private static class DefaultThreadFactory implements ThreadFactory
   {
      private static int count = 0;
      private static synchronized int nextCount()
      {
         return count ++;
      }

      /**
       * Create a new Thread for the given Runnable
       *
       * @param command The Runnable to pass to Thread
       * @return The newly created Thread
       */
      public Thread newThread(final Runnable command)
      {
         String name = "JMS SessionPool Worker-" + nextCount();
         return new Thread(threadGroup, command, name);
      }
   }
}
