/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.security.Principal;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.4 $
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

