/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * Defines a model for a service.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 * @version $Revision: 1.5 $
 */
public interface Service
{
   /**
    * Initalize the service.
    *
    * @throws Exception    Service failed to initalize.
    */
   void init() throws Exception;

   /**
    * Start the service.
    *
    * @throws Exception    Service failed to start.
    */
   void start() throws Exception;

   /**
    * Stop the service.
    */
   void stop();
   
   /**
    * Destroy the service.
    */
   void destroy();
}
