/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import java.lang.reflect.Method;


/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.4 $
 */
public class StatelessHandleImpl
   implements Handle
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public StatelessHandleImpl(String name)
   {
      this.name = name;
   }
   
   // Public --------------------------------------------------------

   // Handle implementation -----------------------------------------
   public EJBObject getEJBObject()
      throws RemoteException
   {
      try
      {
         Object home = new InitialContext().lookup(name);
         
         Method create = home.getClass().getMethod("create", new Class[0]);
         return (EJBObject)create.invoke(home, new Object[0]);
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

