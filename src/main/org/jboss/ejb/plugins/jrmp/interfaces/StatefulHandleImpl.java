/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import java.lang.reflect.Method;

import org.jboss.logging.Logger;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 * 	@author <a href="mailto:marc.fleury@telkel.com>Marc Fleury</a>
 *	@version $Revision: 1.1 $
 */
public class StatefulHandleImpl
   implements Handle
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   Object id;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public StatefulHandleImpl(String name, Object id)
   {
      this.name = name;
      this.id = id;
   }
   
   // Public --------------------------------------------------------

   // Handle implementation -----------------------------------------
   public EJBObject getEJBObject()
      throws RemoteException
   {
      try
      {
         Object home = new InitialContext().lookup(name);
         
		 // We need to wire the server to retrieve the instance with the right id
		throw new Exception("StatefulHandleImpl.getEJBObject() NYI"); 
		
	   } catch (Exception e)
      {
         throw new ServerException("Could not get EJBObject", e);
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

