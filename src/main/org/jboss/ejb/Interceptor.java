/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.security.Principal;

import com.dreambean.ejx.ejb.EnterpriseBean;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public interface Interceptor
   extends ContainerPlugin
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------
   public void setNext(Interceptor interceptor);
   public Interceptor getNext();
   
   public Object invokeHome(MethodInvocation mi)
      throws Exception;
      
   public Object invoke(MethodInvocation mi)
      throws Exception;
}

