/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mgt;

import org.jboss.ejb.Container;

/**
 *   This is the interface of the ContainerMgt that is exposed for administration
 *
 *   @see ContainerMgt
 *   @author Andreas Schaefer (andreas.schaefer@madplanet.com)
 *
 *   @version $Revision: 1.1 $
 */
public interface ContainerMgtMBean
	extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Sets the container this is the management proxy for
    *
    * @param pContainer Container which this refers to. Must not be null.
    **/
   public void setContainer( Container pContainer );
   
   /**
    * @return Container this is a proxy for
    **/
   public Container getContainer();

}

