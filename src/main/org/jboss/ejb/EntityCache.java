package org.jboss.ejb;

/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

import java.rmi.RemoteException;
import javax.ejb.NoSuchEntityException;

/**
 *	EntityCaches can work from several keys
 *
 *  A cache can use the natural primaryKey from the EJBObject, or DB dependent
 *  keys or a proprietary key
 *
 *	@see EntityInstanceCache
 *	@author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *	@version $Revision: 1.2 $
 */
public interface EntityCache
   extends InstanceCache
{
   /**
    * Returns the key used to cache the context
	*
	* @param id				Object id / primary key
	*
	* @return				Cache key
	*/
   public Object createCacheKey( Object id );

}
