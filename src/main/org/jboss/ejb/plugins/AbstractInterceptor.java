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

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;

import com.dreambean.ejx.ejb.EnterpriseBean;

import org.jboss.ejb.Container;
import org.jboss.ejb.Interceptor;
import org.jboss.ejb.EnterpriseContext;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public abstract class AbstractInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   private Container container;
   protected Interceptor nextInterceptor;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation ------------------------------------
   public void setContainer(Container container) { this.container = container; }
   public Container getContainer() { return container; }
   public void setNext(Interceptor interceptor) { nextInterceptor = interceptor; }
   public Interceptor getNext() { return nextInterceptor; }
   
   public void init()
      throws Exception
   {
   }
   
   public void start()
      throws Exception
   {
   }
   
   public void stop()
   {
   }

   public void destroy()
   {
   }

   public Object invokeHome(Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      return getNext().invokeHome(method, args, ctx);
   }

   public Object invoke(Object id, Method method, Object[] args, EnterpriseContext ctx)
      throws Exception
   {
      return getNext().invoke(id, method, args, ctx);
   }
   // Protected -----------------------------------------------------
}
