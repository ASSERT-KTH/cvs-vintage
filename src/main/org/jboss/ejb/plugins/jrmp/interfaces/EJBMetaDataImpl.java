/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.3 $
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
   boolean statefulSession;
   
   EJBHome homeHandle;
   
   // Constructors --------------------------------------------------
   public EJBMetaDataImpl(Class remote, Class home, Class pkClass, boolean session, boolean statefulSession, EJBHome homeHandle)
   {
      this.remote = remote;
      this.home = home;
      this.pkClass = pkClass;
      this.session = session;
      this.statefulSession = statefulSession;
      this.homeHandle = homeHandle;
   }
   
   // EJBMetaData ---------------------------------------------------
   public EJBHome getEJBHome() { return homeHandle; }
   public java.lang.Class getHomeInterfaceClass() { return home; }
   public java.lang.Class getRemoteInterfaceClass() { return remote; }
   public java.lang.Class getPrimaryKeyClass() { return pkClass; }
   public boolean isSession() { return session; }
   public boolean isStatelessSession() { return statefulSession; }
}

