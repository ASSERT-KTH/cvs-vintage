/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.4 $
 */
public interface NamingServiceMBean
   extends org.jboss.util.ServiceMBean, org.jnp.server.MainMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Naming";
    
   // Public --------------------------------------------------------
}

