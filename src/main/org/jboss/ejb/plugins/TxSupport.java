
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.ejb.plugins;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJBException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRolledbackException;
import org.jboss.logging.Logger;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;



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

   protected final static Logger log = Logger.getLogger(TxSupport.class);

   public final static TxSupport NEVER = new Never("Never");
   public final static TxSupport NOT_SUPPORTED = new NotSupported("NotSupported");
   public final static TxSupport SUPPORTS = new Supports("Supports");
   public final static TxSupport REQUIRED = new Required("Required");
   public final static TxSupport REQUIRES_NEW = new RequiresNew("RequiresNew");
   public final static TxSupport MANDATORY = new Mandatory("Mandatory");

   public final static TxSupport DEFAULT = REQUIRED;//??

   /**
    * The variable <code>values</code> has the acceptable TxSupport
    * instances to support the readResolve method.  for reasons I
    * don't understand the obvious implementation using an ArrayList
    * did not work properly on my mac osx 10.2.3 w/ jdk 1.4.1dp9.
    *
    */
   private final TxSupport[] values = {NEVER,
				       NOT_SUPPORTED,
				       SUPPORTS,
				       REQUIRED,
				       REQUIRES_NEW,
				       MANDATORY};
   private static int nextOrdinal = 0;
   private final int ordinal = nextOrdinal++;
   private final transient String name;

   private TxSupport(String name) {
      this.name = name;
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
      throw new IllegalArgumentException("Unknown TxType: " + name);      
   }

   public String toString()
   {
      return name;
   }

   Object readResolve() throws ObjectStreamException {
      return values[ordinal];
   }

   public abstract InvocationResponse clientInvoke(Invocation invocation, TransactionManager tm, org.jboss.proxy.Interceptor next) throws Throwable;

   public abstract InvocationResponse serverInvoke(Invocation invocation, TransactionManager tm, org.jboss.ejb.Interceptor next) throws Exception;


   protected void endTransaction(TransactionManager tm, Transaction tx) 
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
      catch (RollbackException e)
      {
         throw new TransactionRolledbackException(e.getMessage());
      } 
      catch (HeuristicMixedException e)
      {
         throw new TransactionRolledbackException(e.getMessage());
      }
      catch (HeuristicRollbackException e)
      {
         throw new TransactionRolledbackException(e.getMessage());
      }
      catch (SystemException e)
      {
         throw new TransactionRolledbackException(e.getMessage());
      } // end of try-catch
   }

   public final static class Never extends TxSupport
   {
      private Never(String name)
      {
	 super(name);
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
	 return next.invoke(invocation);
      }


   }

   public final static class NotSupported extends TxSupport
   {
      private NotSupported(String name)
      {
	 super(name);
      }


      public InvocationResponse clientInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.proxy.Interceptor next)
	 throws Throwable
      {
	 Transaction tx = tm.getTransaction();
	 if (tx != null)
	 {
	    tm.suspend();
	    try
	    {
	       return next.invoke(invocation);
	    }
	    finally 
	    {
	       tm.resume(tx);
	    } // end of try-finally
	    
	 } // end of if ()
	 else
	 {
	    return next.invoke(invocation);
	 } // end of else
      }

      public InvocationResponse serverInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.ejb.Interceptor next)
	 throws Exception
      {
	 if (tm.getTransaction() != null)
	 {
	    throw new IllegalStateException("Transaction present on server in NotSupported call");   
	 } // end of if ()
	 return next.invoke(invocation);
      }

   }

   public final static class Supports extends TxSupport
   {
      private Supports(String name)
      {
	 super(name);
      }


      public InvocationResponse clientInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.proxy.Interceptor next)
	 throws Throwable
      {
	 Transaction tx = tm.getTransaction();
	 if (tx != null)
	 {
	    invocation.setTransaction(tx);   
	 } // end of if ()
	 
	 return next.invoke(invocation);
      }

      public InvocationResponse serverInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.ejb.Interceptor next)
	 throws Exception
      {
	 return next.invoke(invocation);
      }

   }

   public final static class Required extends TxSupport
   {
      private Required(String name)
      {
	 super(name);
      }

      public InvocationResponse clientInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.proxy.Interceptor next)
	 throws Throwable
      {
	 Transaction tx = tm.getTransaction();
	 if (tx != null)
	 {
	    invocation.setTransaction(tx);   
	 } // end of if ()
	 
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
	    tm.begin(); 
	    tx = tm.getTransaction();
	    try
	    {
	       return next.invoke(invocation);
	    } 
	    finally
	    {
	       endTransaction(tm, tx);
	    } // end of try-finally
	    
	 } // end of if ()
	 
	 else
	 {
	    return next.invoke(invocation);
	 } // end of else
      }

   }

   public final static class RequiresNew extends TxSupport
   {
      private RequiresNew(String name)
      {
	 super(name);
      }

      public InvocationResponse clientInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.proxy.Interceptor next)
	 throws Throwable
      {
	 Transaction tx = tm.getTransaction();
	 if (tx != null)
	 {
	    tm.suspend();
	    try
	    {
	       return next.invoke(invocation);
	    }
	    finally 
	    {
	       tm.resume(tx);
	    } // end of try-finally
	 } // end of if ()
	 else
	 {
	    return next.invoke(invocation);
	 } // end of else
      }

      public InvocationResponse serverInvoke(Invocation invocation,
				 TransactionManager tm,
				 org.jboss.ejb.Interceptor next)
	 throws Exception
      {
	 tm.begin(); 
	 Transaction tx = tm.getTransaction();
	 try 
	 {
	    return next.invoke(invocation);
	 }  
	 finally 
	 {
	    endTransaction(tm, tx);
	 } // end of try-finally
      }

   }

   public final static class Mandatory extends TxSupport
   {
      private Mandatory(String name)
      {
	 super(name);
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
	 invocation.setTransaction(tx);
	 return next.invoke(invocation);
      }

   }
   
}// TxSupport
