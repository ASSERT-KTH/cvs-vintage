/*
* jBoss, the OpenSource EJB server
*
* Distributable under GPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.jrmp.interfaces;

import javax.ejb.HomeHandle;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBHome;
import java.rmi.RemoteException;

/**
*	<description> 
*      
*	@see <related>
*	@author Rickard Öberg (rickard.oberg@telkel.com)
*	@author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>  
*	@version $Revision: 1.6 $
*/
public class EJBMetaDataImpl
implements EJBMetaData, java.io.Serializable
{
	// Constants -----------------------------------------------------
	
	// Attributes ----------------------------------------------------
	Class remote;
	Class home;
	Class pkClass;
	
	boolean session;
	boolean statelessSession;
	
	HomeHandle homeHandle;
	
	// Constructors --------------------------------------------------
	public EJBMetaDataImpl(Class remote, Class home, Class pkClass, boolean session, boolean statelessSession, HomeHandle homeHandle)
	{
		
		this.remote = remote;
		this.home = home;
		this.pkClass = pkClass;
		this.session = session;
		this.statelessSession = statelessSession;
		this.homeHandle = homeHandle;
	}
	
	// EJBMetaData ---------------------------------------------------
	public EJBHome getEJBHome() 
	{ 
		/* 
		* MF BUG?????? 
		* The java.ejb.HomeHandle says throws RemoteException but if I let it be propagated it doesn't compile
		* ???????????
		*/
		try {
			return homeHandle.getEJBHome();
		}
		catch (RemoteException re) {
			re.printStackTrace();
			return null;
		}
	
	}
	
	public java.lang.Class getHomeInterfaceClass() { return home; }
	public java.lang.Class getRemoteInterfaceClass() { return remote; }
	public java.lang.Class getPrimaryKeyClass() { return pkClass; }
	public boolean isSession() { return session; }
	public boolean isStatelessSession() { return statelessSession; }
}

