/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EntityBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.transaction.UserTransaction;

import org.jboss.ejb.Container;
import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.EntityPersistenceManager;
import org.jboss.ejb.StatefulSessionEnterpriseContext;

/**
 * The SessionObjectInputStream is used to deserialize stateful session beans when they are activated
 *      
 *	@see org.jboss.ejb.plugins.SessionObjectOutputStream
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
 *	@author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 *	@version $Revision: 1.7 $
 */
public class SessionObjectInputStream
	extends ObjectInputStream
{
	StatefulSessionEnterpriseContext ctx;
   ClassLoader appCl;

	// Constructors -------------------------------------------------
	public SessionObjectInputStream(StatefulSessionEnterpriseContext ctx, InputStream in)
      throws IOException
   {
      super(in);
      enableResolveObject(true);
		
		this.ctx = ctx;
      
      // cache the application classloader
      appCl = Thread.currentThread().getContextClassLoader();
   }
      
   // ObjectInputStream overrides -----------------------------------
   protected Object resolveObject(Object obj)
      throws IOException
   {
      // section 6.4.1 of the ejb1.1 specification states what must be taken care of 
      
      // ejb reference (remote interface) : resolve handle to EJB
      if (obj instanceof Handle)
         return ((Handle)obj).getEJBObject();
      
      // ejb reference (home interface) : resolve handle to EJB Home
      else if (obj instanceof HomeHandle)
         return ((HomeHandle)obj).getEJBHome();
      
      // naming context: the jnp implementation of contexts is serializable, do nothing

      else if (obj instanceof StatefulSessionBeanField) {
         byte type = ((StatefulSessionBeanField)obj).type; 
       
         // session context: recreate it
         if (type == StatefulSessionBeanField.SESSION_CONTEXT)          
            return ctx.getSessionContext();

         // user transaction: restore it
         else if (type == StatefulSessionBeanField.USER_TRANSACTION) 
            return ctx.getSessionContext().getUserTransaction();      
      }
      return obj;
   }
   
   /** Override the ObjectInputStream implementation to use the application class loader
    */
   protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException
   {
      try
      {
         // use the application classloader to resolve the class
         return appCl.loadClass(v.getName());
         
      } catch (ClassNotFoundException e) {
         // we should probably never get here
         return super.resolveClass(v);
      }
   }

   /** Override the ObjectInputStream implementation to use the application class loader
    */
   protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException
   {
       Class clazz = null;
       Class[] ifaceClasses = new Class[interfaces.length];
       for(int i = 0; i < interfaces.length; i ++)
           ifaceClasses[i] = Class.forName(interfaces[i], false, appCl);
       try
       {
           clazz = Proxy.getProxyClass(appCl, ifaceClasses);
       }
       catch(IllegalArgumentException e)
       {
           throw new ClassNotFoundException("Failed to resolve proxy class", e);
       }
       return clazz;
   }
}
