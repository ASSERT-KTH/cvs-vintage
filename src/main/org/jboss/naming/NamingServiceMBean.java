/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 *   @version $Revision: 1.6 $
 */
public interface NamingServiceMBean
   extends org.jboss.util.ServiceMBean, org.jnp.server.MainMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Naming";
    
   // Public --------------------------------------------------------
}

