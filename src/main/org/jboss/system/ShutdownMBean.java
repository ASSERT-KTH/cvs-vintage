/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.system;

/**
 * The management interface for the Shutdown bean.
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @version $Revision: 1.2 $
 */
public interface ShutdownMBean
{
   /** The default object name to use. */
   String OBJECT_NAME = ":type=Shutdown";
   
   /**
    * Shutdown the virtual machine and run shutdown hooks.
    */
   void shutdown();
   
   /**
    * Forcibly terminates the currently running Java virtual machine.
    */
   void halt();
}
