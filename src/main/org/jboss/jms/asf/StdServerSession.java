/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jms.asf;
import java.lang.reflect.Method;

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;
import javax.jms.XASession;

import javax.naming.InitialContext;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Category;

import org.jboss.tm.TransactionManagerService;

/**
 * An implementation of ServerSession. <p>
 *
 * Created: Thu Dec 7 18:25:40 2000
 *
 * @author    <a href="mailto:peter.antman@tim.se">Peter Antman</a> .
 * @author    <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author    <a href="mailto:hiram.chirino@jboss.org">Hiram Chirino</a> .
 * @version   $Revision: 1.9 $
 */
public class StdServerSession
       implements Runnable, ServerSession
{
   /**
    * Instance logger.
    */
   private final Category log = Category.getInstance(this.getClass());

   /**
    * The server session pool which we belong to.
    */
   private StdServerSessionPool serverSessionPool;
   // = null;

   /**
    * Our session resource.
    */
   private Session session;
   // = null;

   /**
    * Our XA session resource.
    */
   private XASession xaSession;
   // = null;

   /**
    * The transaction manager that we will use for transactions.
    */
   private TransactionManager tm;

   /**
    * Use the session's XAResource directly if we have an JBossMQ XASession.
    * this allows us to get around the TX timeout problem when you have
    * extensive message processing.
    */
   private boolean useLocalTX;

   /**
    * Create a <tt>StdServerSession</tt> .
    *
    * @param pool              The server session pool which we belong to.
    * @param session           Our session resource.
    * @param xaSession         Our XA session resource.
    * @param useLocalTX       Will this session be used in a global TX (we can optimize with 1 phase commit)
    * @throws JMSException     Transation manager was not found.
    * @exception JMSException  Description of Exception
    */
   StdServerSession(final StdServerSessionPool pool,
         final Session session,
         final XASession xaSession,
         final boolean useLocalTX)
          throws JMSException
   {
      // assert pool != null
      // assert session != null

      this.serverSessionPool = pool;
      this.session = session;
      this.xaSession = xaSession;

      try
      {
         this.useLocalTX = useLocalTX && Class.forName("org.jboss.mq.SpySession").isAssignableFrom(session.getClass());
      }
      catch (ClassNotFoundException e)
      {
         this.useLocalTX = false;
      }

      log.debug("initializing (pool, session, xaSession, useLocalTX): " +
            pool + ", " + session + ", " + xaSession + ", " + useLocalTX);

      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext();
         tm = (TransactionManager)
               ctx.lookup(TransactionManagerService.JNDI_NAME);
      }
      catch (Exception e)
      {
         throw new JMSException("Transation manager was not found");
      }
      finally
      {
         if (ctx != null)
         {
            try
            {
               ctx.close();
            }
            catch (Exception ignore)
            {
            }
         }
      }
   }

   // --- Impl of JMS standard API

   /**
    * Returns the session. <p>
    *
    * This simply returns what it has fetched from the connection. It is up to
    * the jms provider to typecast it and have a private API to stuff messages
    * into it.
    *
    * @return                  The session.
    * @exception JMSException  Description of Exception
    */
   public Session getSession() throws JMSException
   {
      return session;
   }

   //--- Protected parts, used by other in the package

   /**
    * Runs in an own thread, basically calls the session.run(), it is up to the
    * session to have been filled with messages and it will run against the
    * listener set in StdServerSessionPool. When it has send all its messages it
    * returns. HC: run() also starts a transaction with the TransactionManager
    * and enlists the XAResource of the JMS XASession if a XASession was
    * available. A good JMS implementation should provide the XASession for use
    * in the ASF. So we optimize for the case where we have an XASession. So,
    * for the case where we do not have an XASession and the bean is not
    * transacted, we have the unneeded overhead of creating a Transaction. I'm
    * leaving it this way since it keeps the code simpler and that case should
    * not be too common (JBossMQ provides XASessions).
    */
   public void run()
   {
      log.debug("running...");

      log.info("running (pool, session, xaSession, useLocalTX): " +
            ", " + session + ", " + xaSession + ", " + useLocalTX);

      // Used if run with useLocalTX if true
      JBossMQTXInterface jbossMQTXInterface = null;

      // Used if run with useLocalTX if false
      Transaction trans = null;
      try
      {

         if (useLocalTX)
         {
            // Use JBossMQ One Phase Commit to commit the TX
            jbossMQTXInterface = new JBossMQTXInterface(session);
            jbossMQTXInterface.startTX();

         }
         else
         {

            // Use the TM to control the TX
            tm.begin();
            trans = tm.getTransaction();

            if (xaSession != null)
            {
               XAResource res = xaSession.getXAResource();
               trans.enlistResource(res);
               if (log.isDebugEnabled())
               {
                  log.debug("XAResource '" + res + "' enlisted.");
               }
            }
         }
         //currentTransactionId = connection.spyXAResourceManager.startTx();

         // run the session
         session.run();
      }
      catch (Exception e)
      {
         log.error("session failed to run; setting rollback only", e);

         if (useLocalTX)
         {
            // Use JBossMQ One Phase Commit to commit the TX
            jbossMQTXInterface.setRollbackOnly();
         }
         else
         {

            // Mark for tollback TX via TM
            try
            {
               // The transaction will be rolledback in the finally
               trans.setRollbackOnly();
            }
            catch (Exception x)
            {
               log.error("failed to set rollback only", x);
            }
         }

      }
      finally
      {
         try
         {
            if (useLocalTX)
            {
               // Use JBossMQ One Phase Commit to commit the TX
               jbossMQTXInterface.endTX();

            }
            else
            {
               // Use the TM to commit the Tx

               // Marked rollback
               if (trans.getStatus() == Status.STATUS_MARKED_ROLLBACK)
               {
                  log.info("Rolling back JMS transaction");
                  // actually roll it back
                  trans.rollback();

                  // NO XASession? then manually rollback.
                  // This is not so good but
                  // it's the best we can do if we have no XASession.
                  if (xaSession == null && serverSessionPool.isTransacted())
                  {
                     session.rollback();
                  }
               }
               else if (trans.getStatus() == Status.STATUS_ACTIVE)
               {
                  // Commit tx
                  // This will happen if
                  // a) everything goes well
                  // b) app. exception was thrown
                  trans.commit();

                  // NO XASession? then manually commit.  This is not so good but
                  // it's the best we can do if we have no XASession.
                  if (xaSession == null && serverSessionPool.isTransacted())
                  {
                     session.commit();
                  }
               }
            }

         }
         catch (Exception e)
         {
            log.error("failed to commit/rollback", e);
         }

         StdServerSession.this.recycle();
      }

      log.debug("done");
   }

   /**
    * Start the session and begin consuming messages.
    *
    * @throws JMSException  No listener has been specified.
    */
   public void start() throws JMSException
   {
      log.debug("starting invokes on server session");

      if (session != null)
      {
         try
         {
            serverSessionPool.getExecutor().execute(this);
         }
         catch (InterruptedException ignore)
         {
         }
      }
      else
      {
         throw new JMSException("No listener has been specified");
      }
   }

   /**
    * Called by the ServerSessionPool when the sessions should be closed.
    */
   void close()
   {
      if (session != null)
      {
         try
         {
            session.close();
         }
         catch (Exception ignore)
         {
         }

         session = null;
      }

      if (xaSession != null)
      {
         try
         {
            xaSession.close();
         }
         catch (Exception ignore)
         {
         }
         xaSession = null;
      }

      log.debug("closed");
   }

   /**
    * This method is called by the ServerSessionPool when it is ready to be
    * recycled intot the pool
    */
   void recycle()
   {
      serverSessionPool.recycle(this);
   }


   /**
    * #Description of the Class
    */
   private static class JBossMQTXInterface
   {

      static boolean initialzied = false;
      static Method getXAResourceManager;
      static Method startTx;
      static Method endTx;
      static Method commit;
      static Method rollback;
      boolean doRollback = false;
      Object xid = null;
      Object spyXAResourceManager = null;

      JBossMQTXInterface(Session sess) throws Exception
      {
         if (!initialzied)
         {
            getXAResourceManager = Class.forName("org.jboss.mq.SpySession").getMethod("getXAResourceManager", new Class[]{});
            startTx = Class.forName("org.jboss.mq.SpyXAResourceManager").getMethod("startTx", new Class[]{});
            endTx = Class.forName("org.jboss.mq.SpyXAResourceManager").getMethod("endTx", new Class[]{Object.class, boolean.class});
            commit = Class.forName("org.jboss.mq.SpyXAResourceManager").getMethod("commit", new Class[]{Object.class, boolean.class});
            rollback = Class.forName("org.jboss.mq.SpyXAResourceManager").getMethod("rollback", new Class[]{Object.class});
            initialzied = true;
         }
         spyXAResourceManager = getXAResourceManager.invoke(sess, new Object[]{});
      }

      void setRollbackOnly()
      {
         doRollback = true;
      }

      void startTX() throws Exception
      {
         xid = startTx.invoke(spyXAResourceManager, new Object[]{});
      }

      void endTX() throws Exception
      {
         if (doRollback)
         {
            endTx.invoke(spyXAResourceManager, new Object[]{xid, new Boolean(true)});
            rollback.invoke(spyXAResourceManager, new Object[]{xid});
         }
         else
         {
            endTx.invoke(spyXAResourceManager, new Object[]{xid, new Boolean(true)});
            commit.invoke(spyXAResourceManager, new Object[]{xid, new Boolean(true)});
         }
      }
   }
}
