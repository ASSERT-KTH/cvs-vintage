/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * This is an interface for Containers that uses InstancePools.
 *
 * <p>Plugins wanting to access pools from containers should use this
 *    interface.
 *
 * @see InstancePool
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.4 $
 */
public interface InstancePoolContainer
{
   /**
    * Get the instance pool for the container.
    *
    * @return   The instance pool for the container.
    */
   InstancePool getInstancePool();
}

