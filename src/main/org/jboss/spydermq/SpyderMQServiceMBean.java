/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.spydermq;

/**
 *   <description> 
 * MBean interface for the SpyderMQ JMX service.
 *      
 *   @see <related>
 *   @author Vincent Sheffer (vincent.sheffer@telkel.com)
 *   @version $Revision: 1.3 $
 */
public interface SpyderMQServiceMBean
   extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=SpyderMQ";
    
   // Public --------------------------------------------------------
}
