/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * The management interface for the Shutdown bean.
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.3 $
 */
public interface ShutdownMBean
{
   /**
    * Shutdown the virtual machine and run shutdown hooks.
    */
   void shutdown();
   
   /**
    * Forcibly terminates the currently running Java virtual machine.
    */
   void halt();
}





