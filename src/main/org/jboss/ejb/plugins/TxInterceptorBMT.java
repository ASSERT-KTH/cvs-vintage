/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;






import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;

/**
 * This interceptor handles transactions for session BMT beans.
 *
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @author <a href="mailto:akkerman@cs.nyu.edu">Anatoly Akkerman</a>
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.25 $
 */
public final class TxInterceptorBMT extends AbstractTxInterceptor
{
   /**
    * This associates the thread to the UserTransaction.
    *
    * It is used to redirect lookups on java:comp/UserTransaction to
    * the <code>getUserTransaction()</code> method of the context.
    */
   private final ThreadLocal userTransaction = new ThreadLocal();

   /**
    * If <code>false</code>, transactions may live across bean instance
    * invocations, otherwise the bean instance should terminate any
    * transaction before returning from the invocation.
    * This attribute defaults to <code>true</code>.
    */
   private boolean stateless;

   //public void create() throws Exception
   public void start() throws Exception
   {
      // Do initialization in superclass.
      super.start();


      // Set the atateless attribute
      BeanMetaData beanMetaData = getContainer().getBeanMetaData();
      if(beanMetaData instanceof SessionMetaData) {
         stateless = ((SessionMetaData)beanMetaData).isStateless();
      }
      else
      {
         stateless = true;
      }
      //COPY/PASTE FROM CMT INTERCEPTOR
      Map methodToTxSupportMap = new HashMap();
      if (getContainer().getHomeClass() != null)
      {
	 mapMethods(getContainer().getHomeClass(), beanMetaData, InvocationType.HOME,  methodToTxSupportMap);
      } // end of if ()
      
      if (getContainer().getRemoteClass() != null)
      {
	 mapMethods(getContainer().getRemoteClass(), beanMetaData, InvocationType.REMOTE, methodToTxSupportMap);
      } // end of if ()
      
      if (getContainer().getLocalHomeClass() != null)
      {
	 mapMethods(getContainer().getLocalHomeClass(), beanMetaData, InvocationType.LOCALHOME, methodToTxSupportMap);
      } // end of if ()
      
      if (getContainer().getLocalClass() != null)
      {
	 mapMethods(getContainer().getLocalClass(), beanMetaData, InvocationType.LOCAL, methodToTxSupportMap);
      } // end of if ()
      
      getContainer().setMethodToTxSupportMap(methodToTxSupportMap);
      //END COPY/PASTE
      // bind java:comp/UserTransaction
      RefAddr refAddr = new RefAddr("userTransaction") {
         public Object getContent() {
            return userTransaction;
         }
      };

      Reference ref = new Reference(
            "javax.transaction.UserTransaction",
            refAddr,
            new UserTxFactory().getClass().getName(),
            null);
      Context context = (Context) new InitialContext().lookup("java:comp/");
      context.bind("UserTransaction", ref);
   }

   private void mapMethods(Class clazz, BeanMetaData beanMetaData, InvocationType type, Map methodToTxSupportMap)
      throws Exception
   {
      Method[] methods = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
	 Method m = methods[i];
	 methodToTxSupportMap.put(methods[i], TxSupport.NOT_SUPPORTED);
      } // end of for ()
      
   }

   //public void destroy()
   public void stop()
   {
      // unbind java:comp/UserTransaction
      try 
      {
         Context context = (Context) new InitialContext().lookup("java:comp/");
         context.unbind("UserTransaction");
      }
      catch(Exception e) {}
      super.stop();
      getContainer().setMethodToTxSupportMap(null);
   }

   /**
    * This method calls the next interceptor in the chain.
    *
    * It handles the suspension of any client transaction, and the
    * association of the calling thread with the instance transaction.
    * And it takes care that any lookup of
    * <code>java:comp/UserTransaction</code> will return the right
    * UserTransaction for the bean instance.
    *
    * @todo Move this logic to a TxSupport subclass and coalesce this
    * class with TxInterceptorCMT.
    *
    * @param invocation the invocation context
    * @return the results of this invocation
    * @throws Exception if a problem occures during the invocation
    */
   public InvocationResponse invoke(Invocation invocation) throws Exception
   {
      if(invocation.getType().isHome() && stateless)
      {
         // stateless: no context, no transaction, no call to the instance
         return getNext().invoke(invocation);
      } 

      // Save the transaction that comes with the MI
      Transaction oldTransaction = invocation.getTransaction();

      // Get old threadlocal: It may be non-null if one BMT bean does a local
      // call to another.
      Object oldUserTx = userTransaction.get();

      // Suspend any transaction associated with the thread: It may be
      // non-null on optimized local calls.
      TransactionManager tm = getContainer().getTransactionManager();
      Transaction threadTx = tm.suspend();

      try 
      {
         EnterpriseContext ctx = 
               ((EnterpriseContext) invocation.getEnterpriseContext());

         // Set the threadlocal to the userTransaction of the instance
         userTransaction.set(ctx.getEJBContext().getUserTransaction());

         // Get the bean instance transaction
         Transaction beanTx = ctx.getTransaction();

         // Resume the bean instance transaction
         // only if it not null, some TMs can't resume(null), e.g. Tyrex
         if(beanTx != null) 
         {
            tm.resume(beanTx);
         }

         // Let the MI know about our new transaction
         invocation.setTransaction(beanTx);

         try 
         {
            // Let the superclass call next interceptor and do the exception
            // handling
            return super.invokeNext(invocation, false);
         } 
         finally 
         {
            try 
            {
               if(stateless)
               {
                  checkStatelessDone();
               }
            } 
            finally 
            {
               tm.suspend();
            }
         }
      } 
      finally 
      {
         // Reset threadlocal to its old value
         userTransaction.set(oldUserTx);

         // Restore old MI transaction
         // OSH: Why ???
         invocation.setTransaction(oldTransaction);

         // If we had a Tx associated with the thread reassociate
         if(threadTx != null)
         {
            tm.resume(threadTx);
         }
      }
   }

   private void checkStatelessDone() throws RemoteException
   {
      int status = Status.STATUS_NO_TRANSACTION;

      try 
      {
         status = getContainer().getTransactionManager().getStatus();
      }
      catch (SystemException ex) 
      {
         log.error("Failed to get status", ex);
      }

      switch (status) 
      {
         case Status.STATUS_ACTIVE:
         case Status.STATUS_COMMITTING:
         case Status.STATUS_MARKED_ROLLBACK:
         case Status.STATUS_PREPARING:
         case Status.STATUS_ROLLING_BACK:
            try 
            {
               getContainer().getTransactionManager().rollback();
            } 
            catch (Exception ex) 
            {
               log.error("Failed to rollback", ex);
            }
            // fall through...
         case Status.STATUS_PREPARED:
            String msg = "Application error: BMT stateless bean " +
               getContainer().getBeanMetaData().getEjbName() +
               " should complete transactions before" +
               " returning (ejb1.1 spec, 11.6.1)";
            log.error(msg);

            // the instance interceptor will discard the instance
            throw new RemoteException(msg);
      }
   }

   public static class UserTxFactory implements ObjectFactory
   {
      public Object getObjectInstance(
            Object ref, 
            Name name,
            Context nameCtx, 
            Hashtable environment) throws Exception
      {
         // The ref is a list with only one RefAddr ...
         RefAddr refAddr = ((Reference)ref).get(0);
         // ... whose content is the threadlocal
         ThreadLocal threadLocal = (ThreadLocal)refAddr.getContent();

         // The threadlocal holds the right UserTransaction
         return threadLocal.get();
      }
   }
}
