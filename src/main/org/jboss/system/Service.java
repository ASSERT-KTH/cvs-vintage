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
 * @version $Revision: 1.3 $
 *
 * <p><b>20010830 marc fleury:</b>
 * <ul>
 *   <li>Initial import
 * </ul>
 * <p><b>20011111 david jencks:</b>
 * <ul>
 *   <li>removed init and destroy methods
 * </ul>

 */
public interface Service
{
   void start() throws Exception;
   
   void stop();
   //well I tried... maybe later.
   void init() throws Exception;
   void destroy();
}
