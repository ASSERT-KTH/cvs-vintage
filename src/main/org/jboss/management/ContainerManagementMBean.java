/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.management;

import org.jboss.ejb.Container;

/**
 *   This is the interface of the ContainerMgt that is exposed for administration
 *
 *   @see ContainerMgt
 *   @author Andreas Schaefer (andreas.schaefer@madplanet.com)
 *
 *   @version $Revision: 1.1 $
 */
public interface ContainerManagementMBean
	extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * @return Container this is a proxy for
    **/
   public Container getContainer();

}

