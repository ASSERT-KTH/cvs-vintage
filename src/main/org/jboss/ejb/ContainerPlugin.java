/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.util.Service;

/**
 *   This is a superinterface for all Container plugins. All plugin interfaces
 *	  must extend this interface.
 *      
 *   @see Service
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.5 $
 */
public interface ContainerPlugin
   extends Service
{
   // Public --------------------------------------------------------
	/**
	 *	This callback is set by the container so that the plugin may access it
	 *
	 * @param   con  the container using this plugin
	 */
   public void setContainer(Container con);
}
