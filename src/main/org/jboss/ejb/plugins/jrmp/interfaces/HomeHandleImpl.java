/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.ejb.HomeHandle;
import javax.ejb.EJBHome;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;


/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public class HomeHandleImpl
   implements HomeHandle
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public HomeHandleImpl(String name)
   {
      this.name = name;
   }
   
   // Public --------------------------------------------------------

   // Handle implementation -----------------------------------------
   public EJBHome getEJBHome()
      throws RemoteException
   {
		try {
        
			return (EJBHome) new InitialContext().lookup(name);
         
      	} 
		catch (NamingException e) {
			
			e.printStackTrace();
         	throw new RemoteException("Could not get EJBHome");
      	} 
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

