/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.Set;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;
import org.jboss.monitor.StatisticsProvider;

import org.w3c.dom.Element;

/**
 * Provides the interface for all container interceptors.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.14 $
 */
public interface Interceptor extends ContainerPlugin, StatisticsProvider
{

   void setConfiguration(Element config);

   /**
    * Set the next interceptor in the chain.
    *
    * @param interceptor The next interceptor in the chain.
    */
   void setNext(Interceptor interceptor);

   /**
    * Get the next interceptor in the chain.
    *
    * @return The next interceptor in the chain.
    */
   Interceptor getNext();
   
   /**
    * Invokes this interceptor.  This usually performs some small work and 
    * hands off futher processing to the next interceptor.
    *
    * @param invocation the invocation context
    * @return the results of this invocation
    * @throws Exception if a problem occures during the invocation
    */
   InvocationResponse invoke(Invocation invocation) throws Exception;
}

