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
 * Provides the interface for all container interceptors.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.6 $
 */
public interface Interceptor
   extends ContainerPlugin
{
   /**
    * Set the next interceptor in the chain.
    *
    * @param interceptor        The next interceptor in the chain.
    */
   void setNext(Interceptor interceptor);

   /**
    * Get the next interceptor in the chain.
    *
    * @return   The next interceptor in the chain.
    */
   Interceptor getNext();
   
   /**
    * ???
    *
    * @param mi         ???
    * @return           ???
    *
    * @throws Exception ???
    */
   Object invokeHome(MethodInvocation mi) throws Exception;

   /**
    * ???
    *
    * @param mi         ???
    * @return           ???
    *
    * @throws Exception ???
    */
   Object invoke(MethodInvocation mi) throws Exception;
}

