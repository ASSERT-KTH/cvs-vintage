/*
 * JBoss, the OpenSource J2EE webOS
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
 *   @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 *   @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 *
 *   @version $Revision: 1.3 $
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

