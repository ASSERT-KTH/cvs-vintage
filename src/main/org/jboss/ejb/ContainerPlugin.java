/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.util.Service;

/**
 * This is a superinterface for all Container plugins.
 * 
 * <p>All plugin interfaces must extend this interface.
 *      
 * @see Service
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.6 $
 */
public interface ContainerPlugin
   extends Service
{
   /**
    * This callback is set by the container so that the plugin may access it
    *
    * @param con    The container using this plugin.
    */
   void setContainer(Container con);
}
