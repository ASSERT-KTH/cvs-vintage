/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
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

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;

import org.jboss.logging.Log;
import org.jboss.logging.Logger;


/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.15 $
 */
public class LogInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected Log log;
   
    protected boolean callLogging;
    
    protected Container container;
    
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container) 
   { 
    this.container = container; 
   }
    
   public  Container getContainer()
   {
    return container;
   }

   // Container implementation --------------------------------------
   public void init()
      throws Exception
   {
      super.start();
      
      String name = getContainer().getBeanMetaData().getEjbName();
        
      // Should we log all calls?
      callLogging = getContainer().getBeanMetaData().getContainerConfiguration().getCallLogging();
        
      log = Log.createLog(name);
   }
   
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      Log.setLog(log);
      
        // Log calls?
        if (callLogging)
        {
            StringBuffer str = new StringBuffer();
            str.append(mi.getMethod().getName());
            str.append("(");
            Object[] args = mi.getArguments();
            if (args != null)
               for (int i = 0; i < args.length; i++)
                {
                  str.append(i==0?"":",");
                    str.append(args[i]);
                }
            str.append(")");
            log.log(str.toString());
        }
      
      try
      {
         return getNext().invokeHome(mi);
      } catch (Exception e)
      {
        // Log system exceptions
        if (e instanceof EJBException)
        {
            Logger.error("BEAN EXCEPTION:"+e.getMessage());
            if (((EJBException)e).getCausedByException() != null)
                Logger.exception(((EJBException)e).getCausedByException());
            
            // Client sees RemoteException
            throw new ServerException("Bean exception. Notify the application administrator", e);
        } else if (e instanceof RuntimeException)
        {
            Logger.error("CONTAINER EXCEPTION:"+e.getMessage());
            Logger.exception(e);
            
            // Client sees RemoteException
            throw new ServerException("Container exception. Notify the container developers :-)", e);
        } else if (e instanceof TransactionRolledbackException)
        {
            Logger.error("TRANSACTION ROLLBACK EXCEPTION:"+e.getMessage());
            // Log the rollback cause
            // Sometimes it wraps EJBException - let's unwrap it
            Throwable cause = ((RemoteException) e).detail;
            if (cause != null) {
                if ((cause instanceof EJBException) &&
                        (((EJBException) cause).getCausedByException() != null)) {
                    cause = ((EJBException) cause).getCausedByException();
                }
                Logger.exception(cause);
            }
            throw e;
        } else
        {
            // Application exception, or (in case of RemoteException) already handled system exc
            // Call debugging -> show exceptions
            if (callLogging)
            {
                Logger.warning(e.getMessage());
                // The full stack trace is much more useful for debugging
                // On the other hand, it may be turned off by the logger filter
                Logger.debug(e);
            }
            
            throw e;
        }
      } finally
      {
         Log.unsetLog();
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
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      Log.setLog(log);
      
      // Log calls?
      if (callLogging)
      {
        StringBuffer str = new StringBuffer();
         str.append(mi.getId() == null ? "" : "["+mi.getId().toString()+"] ");
        str.append(mi.getMethod().getName());
        str.append("(");
         Object[] args = mi.getArguments();
         if (args != null)
            for (int i = 0; i < args.length; i++)
            {
               str.append(i==0?"":",");
                str.append(args[i]);
            }
        str.append(")");
         log.log(str.toString());
      }
        
      try
      {
         return getNext().invoke(mi);
      } catch (Exception e)
      {
        // Log system exceptions
        if (e instanceof EJBException)
        {
            Logger.error("BEAN EXCEPTION:"+e.getMessage());
            if (((EJBException)e).getCausedByException() != null)
                Logger.exception(((EJBException)e).getCausedByException());
            
            // Client sees RemoteException
            throw new ServerException("Bean exception. Notify the application administrator", e);
        } else if (e instanceof RuntimeException)
        {
            Logger.error("CONTAINER EXCEPTION:"+e.getMessage());
            Logger.exception(e);
            
            // Client sees RemoteException
            throw new ServerException("Container exception. Notify the container developers :-)", e);
        } else if (e instanceof TransactionRolledbackException)
        {
            Logger.error("TRANSACTION ROLLBACK EXCEPTION:"+e.getMessage());
            // Log the rollback cause
            // Sometimes it wraps EJBException - let's unwrap it
            Throwable cause = ((RemoteException) e).detail;
            if (cause != null) {
                if ((cause instanceof EJBException) && 
                        (((EJBException) cause).getCausedByException() != null)) {
                    cause = ((EJBException) cause).getCausedByException();
                }
                Logger.exception(cause);
            }
            throw e;
        } else
        {
            // Application exception, or (in case of RemoteException) already handled system exc
            // Call debugging -> show exceptions
            if (callLogging)
            {
                Logger.warning(e.getMessage());
                // The full stack trace is much more useful for debugging
                // On the other hand, it may be turned off by the logger filter
                Logger.debug(e);
            }
            
            throw e;
        }
      } finally
      {
         Log.unsetLog();
      }
   }
   
   // Private -------------------------------------------------------
}


