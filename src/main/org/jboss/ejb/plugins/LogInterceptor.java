/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class LogInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Log log;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Container implementation --------------------------------------
   public void init()
      throws Exception
   {
      super.start();
      
      String name = getContainer().getMetaData().getEjbName();
      log = new Log(name);
   }
   
   public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      Log.setLog(log);
      
      String str = method.getName()+"(";
      if (args != null)
         for (int i = 0; i < args.length; i++)
            str += (i==0?"":",")+args[i];
      log.log(str+")");
      
      try
      {
         return getNext().invokeHome(method, args, ctx);
      } catch (Exception e)
      {
         if (e instanceof FinderException)
            throw e;
         else if (e instanceof CreateException)
            throw e;
         else if (e instanceof RemoveException)
            throw e;
            
         e.printStackTrace();
         throw e;
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
   public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      Log.setLog(log);
      
      try
      {
         String str = (id == null ? "" : "["+id.toString()+"] ") + method.getName()+"(";
         if (args != null)
            for (int i = 0; i < args.length; i++)
               str += (i==0?"":",")+args[i];
         log.log(str+")");
         
         return getNext().invoke(id, method, args, ctx);
      } catch (Exception e)
      {
         log.exception(e);
         throw e;
      } finally
      {
         Log.unsetLog();
      }
   }
   
   // Private -------------------------------------------------------
}

