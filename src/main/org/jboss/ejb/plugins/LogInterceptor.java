/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.util.Map;

import javax.ejb.EJBObject;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.TransactionRolledbackLocalException;
import javax.transaction.TransactionRolledbackException;

import org.apache.log4j.NDC;

import org.jboss.ejb.Container;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationType;
import org.jboss.metadata.BeanMetaData;

/** 
 * An interceptor used to log all invocations. It also handles any
 * unexpected exceptions.
 *
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.24 $
 */
public class LogInterceptor extends AbstractInterceptor
{
   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------
   protected String ejbName;
   protected boolean callLogging;
   protected Container container;
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container)
   {
      this.container = container;
   }
   
   public Container getContainer()
   {
      return container;
   }
   
   // Container implementation --------------------------------------
   public void create()
      throws Exception
   {
      super.start();
      
      BeanMetaData md = getContainer().getBeanMetaData();
      ejbName = md.getEjbName();

      // Should we log call details
      callLogging = md.getContainerConfiguration().getCallLogging();
   }
   
   /**
    * This method logs the method, calls the next invoker, and handles 
    * any exception.
    *
    * @param invocation contain all infomation necessary to carry out the 
    * invocation
    * @return the return value of the invocation
    * @exception Exception if an exception during the invocation
    */
   public Object invokeHome(Invocation invocation)
      throws Exception
   {
      NDC.push(ejbName);

      String methodName;
      if (invocation.getMethod() != null) 
      {
         methodName = invocation.getMethod().getName();
      }
      else
      {
         methodName = "<no method>";
      }
      
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Start method=" + methodName);
      }

      // Log call details
      if (callLogging)
      {
         StringBuffer str = new StringBuffer("InvokeHome: ");
         str.append(methodName);
         str.append("(");
         Object[] args = invocation.getArguments();
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               if (i > 0) 
               {
                  str.append(",");
               }
               str.append(args[i]);
            }
         }
         str.append(")");
         log.debug(str.toString());
      }

      try
      {
         return getNext().invokeHome(invocation);
      }
      catch(Throwable e)
      {
         throw handleException(e, invocation);
      }
      finally
      {
         if (trace)
         {
            log.trace("End method=" + methodName);
         }
         NDC.pop();
      }
   }

   /**
    * This method logs the method, calls the next invoker, and handles 
    * any exception.
    *
    * @param invocation contain all infomation necessary to carry out the 
    * invocation
    * @return the return value of the invocation
    * @exception Exception if an exception during the invocation
    */
   public Object invoke(Invocation invocation)
      throws Exception
   {
      NDC.push(ejbName);

      String methodName;
      if (invocation.getMethod() != null) 
      {
         methodName = invocation.getMethod().getName();
      }
      else 
      {
         methodName = "<no method>";
      }

      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Start method=" + methodName);
      }

      // Log call details
      if (callLogging)
      {
         StringBuffer str = new StringBuffer("Invoke: ");
         if (invocation.getId() != null)
         {
            str.append("[" + invocation.getId().toString() + "] ");
         }
         str.append(methodName);
         str.append("(");
         Object[] args = invocation.getArguments();
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               if (i > 0)
               {
                  str.append(",");
               }
               str.append(args[i]);
            }
         }
         str.append(")");
         log.debug(str.toString());
      }

      try
      {
         return getNext().invoke(invocation);
      }
      catch(Throwable e)
      {
         throw handleException(e, invocation);
      }
      finally
      {
         if (trace)
         {
            log.trace("End method=" + methodName);
         }
         NDC.pop();
      }
   }

   // Private -------------------------------------------------------
   private Exception handleException(Throwable e, Invocation invocation)
   {
      
      InvocationType type = invocation.getType();
      boolean isLocal = 
            type == InvocationType.LOCAL ||
            type == InvocationType.LOCALHOME;
      
      if (e instanceof TransactionRolledbackLocalException ||
            e instanceof TransactionRolledbackException)
      {
         // If we got a remote TransactionRolledbackException for a local
         // invocation convert it into a TransactionRolledbackLocalException
         if (isLocal && e instanceof TransactionRolledbackException) 
         {
            TransactionRolledbackException remoteTxRollback = 
                  (TransactionRolledbackException)e;

            Exception cause;
            if (remoteTxRollback.detail instanceof Exception) 
            {
               cause = (Exception)remoteTxRollback.detail;
            }
            else if (remoteTxRollback.detail instanceof Error) 
            {
               String msg = formatException(
                     "Unexpected Error", 
                     remoteTxRollback.detail);
               cause = new EJBException(msg);
            }
            else 
            {
               String msg = formatException(
                     "Unexpected Throwable", 
                     remoteTxRollback.detail);
               cause = new EJBException(msg);
            }

            e = new TransactionRolledbackLocalException(
                  remoteTxRollback.getMessage(),
                  cause);
         }

         // If we got a local TransactionRolledbackLocalException for a remote
         // invocation convert it into a TransactionRolledbackException
         if (!isLocal && e instanceof TransactionRolledbackLocalException) 
         {
            TransactionRolledbackLocalException localTxRollback = 
                  (TransactionRolledbackLocalException)e;
            e = new TransactionRolledbackException(
                  localTxRollback.getMessage());
            ((TransactionRolledbackException)e).detail = 
                  localTxRollback.getCausedByException();
         }

         // get the data we need for logging
         Throwable cause = null;
         String exceptionType = null;
         if (e instanceof TransactionRolledbackException)
         {
            cause = ((TransactionRolledbackException)e).detail;
            exceptionType = "TransactionRolledbackException";
         } 
         else 
         {
            cause = 
               ((TransactionRolledbackLocalException)e).getCausedByException();
            exceptionType = "TransactionRolledbackLocalException";
         }

         // log the exception
         if (cause != null)
         {
            // if the cause is an EJBException unwrap it for logging
            if ((cause instanceof EJBException) &&
                  (((EJBException) cause).getCausedByException() != null))
            {
               cause = ((EJBException) cause).getCausedByException();
            }
            log.error(exceptionType + ", causedBy:", cause);
         }
         else
         {
            log.error(exceptionType + ":", e);
         }
         return (Exception)e;
      }
      else if (e instanceof NoSuchEntityException)
      {
         NoSuchEntityException noSuchEntityException = 
               (NoSuchEntityException) e;
         if (noSuchEntityException.getCausedByException() != null)
         {
            log.error("NoSuchEntityException, causedBy:", 
                  noSuchEntityException.getCausedByException());
         }
         else
         {
            log.error("NoSuchEntityException:", noSuchEntityException);
         }

         if (isLocal) 
         {
            return new NoSuchObjectLocalException(
                  noSuchEntityException.getMessage(),
                  noSuchEntityException.getCausedByException());
         }
         else 
         {
            NoSuchObjectException noSuchObjectException = 
                  new NoSuchObjectException(noSuchEntityException.getMessage());
            noSuchObjectException.detail = noSuchEntityException;
            return noSuchObjectException;
         }
      }
      else if (e instanceof EJBException)
      {
         EJBException ejbException = (EJBException) e;
         if (ejbException.getCausedByException() != null)
         {
            log.error("EJBException, causedBy:", 
                  ejbException.getCausedByException());
         }
         else
         {
            log.error("EJBException:", ejbException);
         }

         if (isLocal) 
         {
            return ejbException;
         }
         else 
         {
            // Remote invocation need a remote exception
            return new ServerException("EJBException:", ejbException);
         }
      }
      else if (e instanceof RuntimeException)
      {
         RuntimeException runtimeException = (RuntimeException)e;
         log.error("RuntimeException:", runtimeException);

         if (isLocal) 
         {
            return new EJBException("RuntimeException", runtimeException);
         } 
         else 
         {
            return new ServerException("RuntimeException", runtimeException);
         }
      }
      else if (e instanceof Error)
      {
         log.error("Unexpected Error:", e);
         if (isLocal) 
         {
            String msg = formatException("Unexpected Error", e);
            return new EJBException(msg);
         }
         else 
         {
            return new ServerError("Unexpected Error", (Error)e);
         }
      }
      else if (e instanceof Exception)
      {
         if (callLogging)
         {
            log.info("Application Exception", e);
         }
         return (Exception)e;
      }
      else
      {
         // The should not happen
         String msg = formatException("Unexpected Throwable", e);
         log.warn("Unexpected Throwable", e);
         if (isLocal)
         {
            return new EJBException(msg);
         }
         else
         {
            return new ServerException(msg);
         }
      }
   }

   private String formatException(String msg, Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      if (msg != null)
         pw.println(msg);
      t.printStackTrace(pw);
      return sw.toString();
   }
   
   // Monitorable implementation ------------------------------------
   public void sample(Object s)
   {
      // Just here to because Monitorable request it but will be removed soon
   }
   public Map retrieveStatistic()
   {
      return null;
   }
   public void resetStatistic()
   {
   }
}
