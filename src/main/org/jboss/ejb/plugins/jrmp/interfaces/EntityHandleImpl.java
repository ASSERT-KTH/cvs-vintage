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
 *	@version $Revision: 1.6 $
 */
public class EntityHandleImpl
   implements Handle
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   Object id;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public EntityHandleImpl(String name, Object id)
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
         
         Method finder = home.getClass().getMethod("findByPrimaryKey", new Class[] { id.getClass() });
         return (EJBObject)finder.invoke(home, new Object[] { id });
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

