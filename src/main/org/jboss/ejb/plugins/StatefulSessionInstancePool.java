/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

/**
*	An empty shadow class, used to keep symmetry in the code  
*      
*	@see <related>
*   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*   @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
*	@version $Revision: 1.5 $
*      
* <p><b>Revisions:</b>
* <p><b>20010718 andreas schaefer:</b>
* <ul>
* <li>- Added Statistics Gathering
* </ul>
*/
public class StatefulSessionInstancePool
extends AbstractInstancePool
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	
	// Static --------------------------------------------------------
	
	// Constructors --------------------------------------------------
	
	// Public --------------------------------------------------------
	
	public synchronized void free(EnterpriseContext ctx)
	{
		discard(ctx);
	}
	
	// Protected -----------------------------------------------------
	protected EnterpriseContext create(Object instance)
	throws Exception
	{
      mInstantiate.add();
		// The instance is created by the caller and is a newInstance();
		return new StatefulSessionEnterpriseContext(instance, getContainer());
	}
	
	// Private -------------------------------------------------------
	
	// Inner classes -------------------------------------------------

}

