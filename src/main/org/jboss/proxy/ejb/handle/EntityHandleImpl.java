/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb.handle;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.ServerException;

import javax.naming.InitialContext;

import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

/**
* An EJB entity bean handle implementation.
*      
* @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>.
* @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
* @version $Revision: 1.1 $
*/
public class EntityHandleImpl
implements Handle
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //private static final long serialVersionUID = 1636103643167246469L;
   
   // Attributes ----------------------------------------------------
   
   /** The primary key of the entity bean. */
   protected final Object id;
   
   // The container 
   public String name; 
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * Construct a <tt>EntityHandleImpl</tt>.
   *
   * @param state     The initial context state that will be used
   *                  to restore the naming context or null to use
   *                  a fresh InitialContext object.
   * @param name      JNDI name.
   * @param id        Primary key of the entity.
   */
   public EntityHandleImpl( String name, Object id)
   {
      this.name=name;
      this.id = id;
   }
   
   // Public --------------------------------------------------------
   
   /**
   * Handle implementation.
   *
   * @return  <tt>EJBObject</tt> reference.
   *
   * @throws ServerException    Could not get EJBObject.
   * @throws RemoteException
   */
   public EJBObject getEJBObject() throws RemoteException {
      
      try {
                  
         EJBHome home = (EJBHome)new InitialContext().lookup(name);
         Class type = home.getClass();
         Method method = type.getMethod("findByPrimaryKey", new Class[] {id.getClass()});
         
         // call findByPrimary on the target 
         return (EJBObject)method.invoke(home, new Object[] {id});
      }
      catch (Exception e) {
         throw new ServerException("Could not get EJBObject", e);
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

