/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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
import org.jboss.ejb.MethodInvocation;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public abstract class AbstractInterceptor
   implements Interceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected Interceptor nextInterceptor;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Interceptor implementation ------------------------------------
   public abstract void setContainer(Container container);
   public abstract Container getContainer();
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

   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      return getNext().invokeHome(mi);
   }

   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      return getNext().invoke(mi);
   }
   // Protected -----------------------------------------------------
}
