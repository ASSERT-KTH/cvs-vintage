/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb.handle;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.lang.reflect.Method;

import javax.naming.InitialContext;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

/**
* An EJB stateless session bean handle.
*
* @author  <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
*/
public class StatelessHandleImpl
implements Handle
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //private static final long serialVersionUID = 4651553991845772180L;
   
   // Attributes ----------------------------------------------------
   
   public String jndiName;
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * Construct a <tt>StatelessHandleImpl</tt>.
   *
   * @param handle    The initial context handle that will be used
   *                  to restore the naming context or null to use
   *                  a fresh InitialContext object.
   * @param name      JNDI name.
   */
   public StatelessHandleImpl(String jndiName)
   {
      this.jndiName = jndiName;
      
   }
   
   // Public --------------------------------------------------------
   
   public EJBObject getEJBObject()
   throws ServerException
   {
      try {
         
         EJBHome home = (EJBHome) new InitialContext().lookup(jndiName);
         Class type = home.getClass();
         Method method = type.getMethod("create", new Class[0]);
         
         return (EJBObject)method.invoke(home, new Object[0]);
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

