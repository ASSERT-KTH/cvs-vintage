/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.naming.Name;

/**
 *	This is an interface for Containers that uses InstancePools.
 *
 *	Plugins wanting to access pools from containers should use this interface
 *
 *	@see InstancePool
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.3 $
 */
public interface InstancePoolContainer
{
   // Public --------------------------------------------------------
	public InstancePool getInstancePool();
}

