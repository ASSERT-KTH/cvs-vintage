/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

/**
 * The Service interface for the JBOSS-SYSTEM.
 *      
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>.
 * @version $Revision: 1.2 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 */
public interface Service
{
   void init() throws Exception;
   
   void start() throws Exception;
   
   void stop();
   
   void destroy();
}
