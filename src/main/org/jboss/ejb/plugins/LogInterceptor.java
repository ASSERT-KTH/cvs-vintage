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
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.transaction.TransactionRolledbackException;

import org.apache.log4j.NDC;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.Interceptor;
import org.jboss.invocation.Invocation;
import org.jboss.metadata.BeanMetaData;

/** An interceptor used to log call invocations. It also handles any
 unexpected exceptions.
 *
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 *   @version $Revision: 1.21 $
 */
public class LogInterceptor
   extends AbstractInterceptor
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
   
   public Object invokeHome(Invocation mi)
      throws Exception
   {
      NDC.push(ejbName);
      String methodName = mi.getMethod().getName();
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Start method="+methodName);
      // Log call details
      if(callLogging)
      {
         StringBuffer str = new StringBuffer("InvokeHome: ");
         str.append(methodName);
         str.append("(");
         Object[] args = mi.getArguments();
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               str.append(i==0?"":",");
               str.append(args[i]);
            }
         }
         str.append(")");
         log.debug(str.toString());
      }

      try
      {
         Interceptor next = getNext();
         Object value = next.invokeHome(mi);
         return value;
      }
      catch(Throwable e)
      {
         throw handleException(e, trace);
      }
      finally
      {
         if( trace )
            log.trace("End method="+methodName);
         NDC.pop();
      }
   }

   /**
    *   This method does invocation interpositioning of tx and security,
    *   retrieves the instance from an object table, and invokes the method
    *   on the particular instance
    *
    * @param   id
    * @param   m
    * @param   args
    * @return
    * @exception   Exception
    */
   public Object invoke(Invocation mi)
      throws Exception
   {
      NDC.push(ejbName);
      String methodName = mi.getMethod().getName();
      boolean trace = log.isTraceEnabled();
      if( trace )
         log.trace("Start method="+methodName);
      // Log call details
      if(callLogging)
      {
         StringBuffer str = new StringBuffer("Invoke: ");
         str.append(mi.getId() == null ? "" : "["+mi.getId().toString()+"] ");
         str.append(methodName);
         str.append("(");
         Object[] args = mi.getArguments();
         if (args != null)
         {
            for (int i = 0; i < args.length; i++)
            {
               str.append(i==0?"":",");
               str.append(args[i]);
            }
         }
         str.append(")");
         log.debug(str.toString());
      }

      try
      {
         Interceptor next = getNext();
         Object value = next.invoke(mi);
         return value;
      }
      catch(Throwable e)
      {
         throw handleException(e, trace);
      }
      finally
      {
         if( trace )
            log.trace("End method="+methodName);
         NDC.pop();
      }
   }

   // Private -------------------------------------------------------
   private Exception handleException(Throwable e, boolean trace)
   {
      Exception toThrow = null;
      // Log system exceptions
      if (e instanceof EJBException)
      {
         EJBException ex = (EJBException) e;
         if( ex.getCausedByException() != null )
            log.error("EJBException, causedBy:", ex.getCausedByException());
         else
            log.error("EJBException:", ex);

         // Client sees RemoteException
         toThrow = new ServerException("Bean exception. Notify the application administrator", ex);
      }
      else if (e instanceof RuntimeException)
      {
         RuntimeException ex = (RuntimeException) e;
         log.error("CONTAINER EXCEPTION:", ex);
         
         // Client sees RemoteException
         toThrow = new ServerException("Container exception. Notify the container developers :-)", ex);
      }
      else if (e instanceof TransactionRolledbackException)
      {
         TransactionRolledbackException ex = (TransactionRolledbackException) e;
         // Log the rollback cause
         // Sometimes it wraps EJBException - let's unwrap it
         Throwable cause = ex.detail;
         if (cause != null)
         {
            if ((cause instanceof EJBException) &&
            (((EJBException) cause).getCausedByException() != null))
            {
               cause = ((EJBException) cause).getCausedByException();
            }
            log.error("TransactionRolledbackException, causedBy:", cause);
         }
         else
         {
            log.error("TransactionRolledbackException:", ex);
         }
         toThrow = ex;
      }
      else
      {
         Exception ex = null;
         if( e instanceof Exception )
            ex = (Exception) e;
         else if( e instanceof Error )
         {
            // The app should not be throwing this show issue a warning
            Error err = (Error) e;
            log.warn("Unexpected Error", err);
            ex = new ServerError("Unexpected Error", err);
         }
         else
         {
            // The app should not be throwing this show issue a warning
            String msg = formatException("Unexpected Throwable", e);
            log.warn("Unexpected Throwable", e);
            ex = new ServerException(msg);
         }
         // Application exception, or (in case of RemoteException) already handled system exc
         // Call debugging -> show exceptions
         if(callLogging)
            log.info("AppException", ex);
         toThrow = ex;
      }
      return toThrow;
   }

   private String formatException(String msg, Throwable t)
   {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      if( msg != null )
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
