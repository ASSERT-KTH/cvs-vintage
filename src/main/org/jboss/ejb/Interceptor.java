/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.util.Set;

import org.jboss.invocation.Invocation;
import org.jboss.monitor.StatisticsProvider;

import org.w3c.dom.Element;

/**
 * Provides the interface for all container interceptors.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.12 $
 *
 *<p><b>20011219 marc fleury:</b>
 * <ul>
 *  <li>Method Invocation is replaced by Invocation
 *</ul>
 */
public interface Interceptor
   extends ContainerPlugin, StatisticsProvider
{

   void setConfiguration(Element config);

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
   Object invokeHome(Invocation mi) throws Exception;

   /**
    * ???
    *
    * @param mi         ???
    * @return           ???
    *
    * @throws Exception ???
    */
   Object invoke(Invocation mi) throws Exception;
}

