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
 *	This is an interface for Containers that uses ContainerInvokers.
 *
 *	ContainerInvokers may communicate with the Container through this interface
 *
 *	@see ContainerInvoker
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:docodan@mvcsoft.com">Daniel OConnor</a>
 *	@version $Revision: 1.5 $
 */
public interface ContainerInvokerContainer
{
   // Public --------------------------------------------------------
   public Class getHomeClass();
   
   public Class getRemoteClass();
   
   public Class getLocalHomeClass();
   
   public Class getLocalClass();
	
   public ContainerInvoker getContainerInvoker();
   
   public LocalContainerInvoker getLocalContainerInvoker();
}

