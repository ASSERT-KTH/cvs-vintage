/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb.handle;

import javax.ejb.HomeHandle;
import javax.ejb.EJBHome;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.ServerException;
import java.rmi.RemoteException;

/*
import java.lang.reflect.Method;
*/

/**
* An EJB home handle implementation.
*      
* @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>.
* @version $Revision: 1.1 $
*/
public class HomeHandleImpl
implements HomeHandle
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
  // private static final long serialVersionUID = -6105191783910395296L;
   
   // Attributes ----------------------------------------------------
   
   public String jndiName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * Construct a <tt>HomeHandleImpl</tt>.
   *
   * @param handle    The initial context handle that will be used
   *                  to restore the naming context or null to use
   *                  a fresh InitialContext object.
   * @param name      JNDI name.
   */
   public HomeHandleImpl(String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   // Public --------------------------------------------------------
   
   // Handle implementation -----------------------------------------
   
   /**
   * HomeHandle implementation.
   *
   * @return  <tt>EJBHome</tt> reference.
   *
   * @throws ServerException    Could not get EJBObject.
   * @throws RemoteException
   */
   public EJBHome getEJBHome() throws RemoteException 
   {
      try 
      {
         System.out.println(" GETTING THE HOME");
         return (EJBHome) new InitialContext().lookup(jndiName);
      }   
      catch (NamingException e) {
         throw new ServerException("Could not get EJBHome", e);
      } 
   }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

