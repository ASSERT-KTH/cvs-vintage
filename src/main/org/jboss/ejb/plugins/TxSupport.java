
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.ejb.plugins;

import java.io.ObjectStreamException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.logging.Logger;
import org.jboss.tm.JBossRollbackException;
import org.jboss.tm.JBossTransactionRolledbackException;
import org.jboss.tm.JBossTransactionRolledbackLocalException;
import org.jboss.util.UnreachableStatementException;
import java.util.ArrayList;



/**
 * TxSupport.java encapsulates the transaction handling possibilities
 * from the ejb spec.  The Tx interceptors call the clientInvoke and
 * serverInvoke methods on the subclass determined by the method's
 * transaction support.
 *
 *
 * Created: Sun Feb  2 23:25:09 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public abstract class TxSupport
   implements Serializable
{

   /**
    * The variable <code>values</code> has the acceptable TxSupport
    * instances to support the readResolve method.
    *
    */
   private static final List values = new ArrayList();

   protected final static Logger log = Logger.getLogger(TxSupport.class);

   public final static TxSupport NEVER = new Never("Never");
   public final static TxSupport NOT_SUPPORTED = new NotSupported("NotSupported");
   public final static TxSupport SUPPORTS = new Supports("Supports");
   public final static TxSupport REQUIRED = new Required("Required");
   public final static TxSupport REQUIRES_NEW = new RequiresNew("RequiresNew");
   public final static TxSupport MANDATORY = new Mandatory("Mandatory");

   public final static TxSupport STATELESS_BMT = new StatelessBMT("StatelessBMT");
   public final static TxSupport STATEFUL_BMT = new StatefulBMT("StatefulBMT");

   public final static TxSupport DEFAULT = REQUIRED;//??

   private static int nextOrdinal = 0;
   private final int ordinal = nextOrdinal++;
   private final transient String name;

   private TxSupport(String name) {
      this.name = name;
      values.add(this);
   }

   public static TxSupport byName(String name)
   {
      if (NEVER.name.equals(name))
      {
         return NEVER;
      } // end of if ()
      if (NOT_SUPPORTED.name.equals(name))
      {
         return NOT_SUPPORTED;
      } // end of if ()
      if (SUPPORTS.name.equals(name))
      {
         return SUPPORTS;
      } // end of if ()
      if (REQUIRED.name.equals(name))
      {
         return REQUIRED;
      } // end of if ()
      if (REQUIRES_NEW.name.equals(name))
      {
         return REQUIRES_NEW;
      } // end of if ()
      if (MANDATORY.name.equals(name))
      {
         return MANDATORY;
      } // end of if ()
      if (STATELESS_BMT.name.equals(name))
      {
         return STATELESS_BMT;
      } // end of if ()
      if (STATEFUL_BMT.name.equals(name))
      {
         return STATEFUL_BMT;
      } // end of if ()
      throw new IllegalArgumentException("Unknown TxType: " + name);
   }

   public String toString()
   {
      return name;
   }

   protected Object internalReadResolve() throws ObjectStreamException {
      return values.get(ordinal);
   }

   public abstract InvocationResponse clientInvoke(Invocation invocation, TransactionManager tm, org.jboss.proxy.Interceptor next) throws Throwable;

   public abstract InvocationResponse serverInvoke(Invocation invocation, TransactionManager tm, org.jboss.ejb.Interceptor next) throws Exception;


   /**
    * The <code>invokeInNoTx</code> method implements the behavior in
    * the ejb 2.1 spec section 18.3 table 15 lines 5 and 6, in the
    * case where there is no transaction.
    *
    * @param invocation an <code>Invocation</code> value
    * @param next an <code>org.jboss.ejb.Interceptor</code> value
    * @return an <code>InvocationResponse</code> value
    * @exception Exception if an error occurs
    */
   protected InvocationResponse invokeInNoTx(Invocation invocation, org.jboss.ejb.Interceptor next) throws Exception
   {
      invocation.setTransaction(null);
      try
      {
         return next.invoke(invocation);
      }
      catch (Throwable t)
      {
         // if this is an ApplicationException, just rethrow it
         rethrowApplicationException(t);
         //otherwise reformat if necessary and rethrow.
         rethrowAsException(invocation.getType(), t);
         throw new UnreachableStatementException();
      } // end of try-catch
   }

   /**
    * The <code>invokeInOurTx</code> method implements the behavior
    * described in the ejb 2.1 spec, section 18.3, table 15, lines 3
    * and 4.
    *
    * @param invocation an <code>Invocation</code> value
    * @param next an <code>org.jboss.ejb.Interceptor</code> value
    * @param tm a <code>TransactionManager</code> value
    * @return an <code>InvocationResponse</code> value
    * @exception Exception if an error occurs
    */
   protected InvocationResponse invokeInOurTx(Invocation invocation, org.jboss.ejb.Interceptor next, TransactionManager tm) throws Exception
   {
      tm.begin();
      Transaction tx = tm.getTransaction();
      invocation.setTransaction(tx);
      try
      {
         try
         {
            return next.invoke(invocation);
         }
         catch (Throwable t)
         {
            // if this is an ApplicationException, just rethrow it
            rethrowApplicationException(t);
            setRollbackOnly(tx);
            //otherwise reformat if necessary and rethrow.
            rethrowAsException(invocation.getType(), t);
            throw new UnreachableStatementException();
         } // end of try-catch
      }
      finally
      {
         endTransaction(invocation, tm, tx);
      } // end of try-finally
   }

   /**
    * The <code>invokeInCallerTx</code> method implements the behavior
    * described in the ejb 2.1 spec, section 18.3, table 15, lines 1
    * and 2.
    *
    * @param invocation an <code>Invocation</code> value
    * @param next an <code>org.jboss.ejb.Interceptor</code> value
    * @param tx a <code>Transaction</code> value
    * @return an <code>InvocationResponse</code> value
    * @exception Exception if an error occurs
    */
   protected InvocationResponse invokeInCallerTx(Invocation invocation, org.jboss.ejb.Interceptor next, Transaction tx) throws Exception
   {
      invocation.setTransaction(tx);
      try
      {
         return next.invoke(invocation);
      }
      catch (Throwable t)
      {
         // if this is an ApplicationException, just rethrow it
         rethrowApplicationException(t);
         setRollbackOnly(tx);
         //otherwise reformat if necessary and rethrow.
         rethrowAsTxRolledbackException(invocation.getType(), t);
         throw new UnreachableStatementException();
      } // end of try-catch
   }

   /**
    * The <code>endTransaction</code> method ends a transaction and
    * translates any exceptions into
    * TransactionRolledBack[Local]Exception or SystemException.
    *
    * @param invocation an <code>Invocation</code> value
    * @param tm a <code>TransactionManager</code> value
    * @param tx a <code>Transaction</code> value
    * @exception TransactionRolledbackException if an error occurs
    * @exception SystemException if an error occurs
    */
   protected void endTransaction(Invocation invocation, TransactionManager tm, Transaction tx)
      throws TransactionRolledbackException, SystemException
   {
      if (tx != tm.getTransaction())
      {
         throw new IllegalStateException("Wrong tx on thread: expected " + tx + ", actual " + tm.getTransaction());
      } // end of if ()

      try
      {
         if(tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
         {
            tx.rollback();
         }
         else
         {
            // Commit tx
            // This will happen if
            // a) everything goes well
            // b) app. exception was thrown
            tx.commit();
         }
      }
      catch (Exception re)
      {
         // Unwrap a JBossRollbackExcepion if possible.  There is no
         // point in the extra wrapping, and the EJB spec should have
         // just used javax.transaction.RollbackException
         if (re instanceof JBossRollbackException)
         {
            JBossRollbackException rollback = (JBossRollbackException)re;
            if(rollback.getCause() instanceof Exception)
            {
               re = (Exception) rollback.getCause();
            }
         }
         if (invocation.getType() == InvocationType.LOCAL
             || invocation.getType() == InvocationType.LOCALHOME)
         {
            throw new JBossTransactionRolledbackLocalException(re);
         } // end of if ()
         else
         {
            throw new JBossTransactionRolledbackException(re);
         } // end of else
      }
   }

   /**
    * The <code>rethrowAsTxRolledbackException</code> method
    * translates the supplied exception into an appropriate
    * TransactionRolledback[Local]Exception, unwrapping as
    * appropriate.
    *
    * @param type an <code>InvocationType</code> value
    * @param e a <code>Throwable</code> value
    * @exception TransactionRolledbackException if an error occurs
    */
   protected void rethrowAsTxRolledbackException(InvocationType type, Throwable e)
      throws TransactionRolledbackException
   {
      Throwable cause;
      if (e instanceof NoSuchEntityException)
      {
         NoSuchEntityException nsee = (NoSuchEntityException)e;
         if (type.isLocal())
         {
            cause = new NoSuchObjectLocalException(
               nsee.getMessage(),
               nsee.getCausedByException());
         } else {
            cause = new NoSuchObjectException(nsee.getMessage());

            // set the detil of the exception
            ((NoSuchObjectException)cause).detail =
               nsee.getCausedByException();
         }
      }
      else
      {
         if (type.isLocal())
         {
            // local transaction rolled back exception can only wrap
            // an exception so we create an EJBException for the cause
            if(e instanceof Exception)
            {
               cause = e;
            }
            else if(e instanceof Error)
            {
               String msg = formatException("Unexpected Error", e);
               cause = new EJBException(msg);
            }
            else
            {
               String msg = formatException("Unexpected Throwable", e);
               cause = new EJBException(msg);
            }
         }
         else
         {
            // remote transaction rolled back exception can wrap
            // any throwable so we are ok
            cause = e;
         }
      }

      // We inherited tx: Tell caller we marked for rollback only.
      if (type.isLocal())
      {
         if(cause instanceof TransactionRolledbackLocalException) {
            throw (TransactionRolledbackLocalException)cause;
         } else {
            throw new JBossTransactionRolledbackLocalException(
               cause.getMessage(),
               (Exception)cause);
         }
      }
      else
      {
         if(cause instanceof TransactionRolledbackException) {
            throw (TransactionRolledbackException)cause;
         } else {
            TransactionRolledbackException ex =
               new TransactionRolledbackException(cause.getMessage());
            ex.detail = cause;
            throw ex;
         }
      }
   }

   /**
    * The <code>rethrowAsException</code> method converts the
    * throwable into an exception if necessary and throws the result.
    *
    * @param type an <code>InvocationType</code> value
    * @param e a <code>Throwable</code> value
    * @exception Exception if an error occurs
    * @exception Error if an error occurs
    * @exception ServerException if an error occurs
    */
   protected void rethrowAsException(InvocationType type, Throwable e)
      throws Exception, Error, ServerException
   {
      if(e instanceof Exception) {
         throw (Exception) e;
      }
      if(e instanceof Error) {
         throw (Error) e;
      }

      // we have some funky throwable, wrap it
      if(type.isLocal())
      {
         String msg = formatException("Unexpected Throwable", e);
         throw new EJBException(msg);
      }
      else
      {
         ServerException ex = new ServerException("Unexpected Throwable");
         ex.detail = e;
         throw ex;
      }
   }

   /**
    * The <code>setRollbackOnly</code> method calls setRollbackOnly()
    * on the invocation's transaction and logs any exceptions than may
    * occur.
    *
    * @param invocation an <code>Invocation</code> value
    */
   protected void setRollbackOnly(Transaction tx)
   {
      try
      {
         tx.setRollbackOnly();
      }
      catch (SystemException ex)
      {
         log.error("SystemException while setting transaction " +
                   "for rollback only", ex);
      }
      catch (IllegalStateException ex)
      {
         log.error("IllegalStateException while setting transaction " +
                   "for rollback only", ex);
      }
   }

   /**
    * The <code>rethrowApplicationException</code> method determines
    * if the supplied Throwable is an application exception and
    * rethrows it if it is.
    *
    * @param e a <code>Throwable</code> value
    * @exception Exception if an error occurs
    */
   protected void rethrowApplicationException(Throwable e) throws Exception
   {
      if (e instanceof Exception &&
          !(e instanceof RuntimeException || e instanceof RemoteException))
      {
         throw (Exception)e;
      }
   }

   private final String formatException(String msg, Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      if( msg != null )
      {
         pw.println(msg);
      }
      t.printStackTrace(pw);
      return sw.toString();
   }

   public final static class Never extends TxSupport
   {
      private Never(String name)
      {
         super(name);
      }

      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         if (tm.getTransaction() != null)
         {
            throw new EJBException("Transaction not allowed");
         } // end of if ()
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         if (tm.getTransaction() != null)
         {
            throw new IllegalStateException("Transaction present on server in Never call");
         } // end of if ()
         return invokeInNoTx(invocation, next);
      }


   }

   public final static class NotSupported extends TxSupport
   {
      private NotSupported(String name)
      {
         super(name);
      }


      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            tm.suspend();
            try
            {
               return invokeInNoTx(invocation, next);
            }
            finally
            {
               tm.resume(tx);
            } // end of try-finally

         } // end of if ()
         else
         {
            return invokeInNoTx(invocation, next);
         } // end of else
      }

   }

   public final static class Supports extends TxSupport
   {
      private Supports(String name)
      {
         super(name);
      }


      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         invocation.setTransaction(tm.getTransaction());
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         if (tm.getTransaction() == null)
         {
            return invokeInNoTx(invocation, next);
         } // end of if ()
         else
         {
            return invokeInCallerTx(invocation, next, tm.getTransaction());
         } // end of else
      }

   }

   public final static class Required extends TxSupport
   {
      private Required(String name)
      {
         super(name);
      }

      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         invocation.setTransaction(tm.getTransaction());
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         Transaction tx = tm.getTransaction();
         if (tx == null)
         {
            return invokeInOurTx(invocation, next, tm);
         } // end of if ()

         else
         {
            return invokeInCallerTx(invocation, next, tx);
         } // end of else
      }

   }

   public final static class RequiresNew extends TxSupport
   {
      private RequiresNew(String name)
      {
         super(name);
      }

      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            tm.suspend();
            try
            {
               return invokeInOurTx(invocation, next, tm);
            }
            finally
            {
               tm.resume(tx);
            } // end of try-finally
         } // end of if ()
         else
         {
            return invokeInOurTx(invocation, next, tm);
         } // end of else
      }

   }

   public final static class Mandatory extends TxSupport
   {
      private Mandatory(String name)
      {
         super(name);
      }

      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         Transaction tx = tm.getTransaction();
         if (tx == null)
         {
            throw new EJBException("Transaction required");
         } // end of if ()
         invocation.setTransaction(tx);
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         Transaction tx = tm.getTransaction();
         if (tx == null)
         {
            throw new EJBException("Transaction required");
         } // end of if ()
         return invokeInCallerTx(invocation, next, tx);
      }

   }


   public final static class StatefulBMT extends TxSupport
   {
      private StatefulBMT(String name)
      {
         super(name);
      }


      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            tm.suspend();
            try
            {
               return invokeInBMTTx(invocation, next, tm);
            }
            finally
            {
               tm.resume(tx);
            } // end of try-finally

         } // end of if ()
         else
         {
            return invokeInBMTTx(invocation, next, tm);
         } // end of else
      }

      private InvocationResponse invokeInBMTTx(Invocation invocation, org.jboss.ejb.Interceptor next, TransactionManager tm) throws Exception
      {
         EnterpriseContext ctx =
            ((EnterpriseContext) invocation.getEnterpriseContext());
         //if there is a transaction in the context from a previous invocation, resume it.
         Transaction initialTx = ctx.getTransaction();
         if (initialTx != null)
         {
            tm.resume(initialTx);
         } // end of if ()
         invocation.setTransaction(initialTx);
         try
         {
            return invokeInNoTx(invocation, next);
         }
         finally
         {
            //if there is a current tx, suspend it and remember it in the context.
            Transaction finalTx = tm.suspend();
            ctx.setTransaction(finalTx);
         } // end of try-catch

      }
   }


   public final static class StatelessBMT extends TxSupport
   {
      private StatelessBMT(String name)
      {
         super(name);
      }


      private Object readResolve() throws ObjectStreamException
      {
         return internalReadResolve();
      }

      public InvocationResponse clientInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.proxy.Interceptor next)
         throws Throwable
      {
         return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
                                             TransactionManager tm,
                                             org.jboss.ejb.Interceptor next)
         throws Exception
      {
         if(invocation.getType().isHome())
         {
            // stateless: no context, no transaction, no call to the instance
            return next.invoke(invocation);
         }
         Transaction tx = tm.getTransaction();
         if (tx != null)
         {
            tm.suspend();
            try
            {
               return invokeInNoTxBMT(invocation, next, tm);
            }
            finally
            {
               tm.resume(tx);
            } // end of try-finally

         } // end of if ()
         else
         {
            return invokeInNoTxBMT(invocation, next, tm);
         } // end of else
      }

      private InvocationResponse invokeInNoTxBMT(Invocation invocation, org.jboss.ejb.Interceptor next, TransactionManager tm) throws Exception
      {
         try
         {
            return invokeInNoTx(invocation, next);
         }
         finally
         {
            Transaction tx = tm.getTransaction();
            if (tx != null)
            {
               //ejb2.1 section 17.6.1
               String msg = "Application error, UserTransaction not complete on return of call to stateless session bean " + invocation.getObjectName();
               log.error(msg);
               try
               {
                  tx.rollback();
               }
               catch (Exception e)
               {
                  log.info(msg, e);
               } // end of try-catch
               if (invocation.getType().isLocal())
               {
                  throw new EJBException(msg);
               } // end of if ()
               else
               {
                  throw new RemoteException(msg);
               } // end of else
            } // end of if ()

         } // end of try-catch

      }

   }


}// TxSupport
