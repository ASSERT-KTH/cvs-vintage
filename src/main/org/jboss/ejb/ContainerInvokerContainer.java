/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *	This is an interface for Containers that uses ContainerInvokers.
 *
 *	ContainerInvokers may communicate with the Container through this interface
 *
 *	@see ContainerInvoker
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.1 $
 */
public interface ContainerInvokerContainer
{
   // Public --------------------------------------------------------
   public Class getHomeClass();
   
   public Class getRemoteClass();
	
	public ContainerInvoker getContainerInvoker();
}

