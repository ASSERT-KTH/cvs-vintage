/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.proxy.compiler;

import java.io.*;

import java.rmi.RemoteException;
import javax.ejb.RemoveException;
import javax.ejb.EJBHome;
import javax.ejb.Handle;
import javax.ejb.EJBObject;

import org.jboss.logging.Logger;

/**
 * ???
 *
 * @author Unknown
 * @version $Revision: 1.1 $
 */
public class ProxyProxy
   implements Serializable, EJBObject
{
   private static Logger log = Logger.getLogger(ProxyProxy.class);
   InvocationHandler handler;
   String[] targetNames;

   public ProxyProxy(InvocationHandler handler, Class[] targetTypes)
   {
      this.handler = handler;
      targetNames = new String[ targetTypes.length ];

      for (int iter=0; iter<targetTypes.length; iter++) {
         targetNames[iter] = targetTypes[iter].getName();
      }
   }

   private Class[] getClasses()
   {
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class[] classes = new Class[targetNames.length];

         for (int iter=0; iter<targetNames.length; iter++) {
            classes[iter] = cl.loadClass( targetNames[iter] );
         }

         return classes;
      }
      catch (Exception e)
      {
         log.error("unexpected", e);
         return null;
      }
   }

   public Object readResolve() throws ObjectStreamException
   {
      return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                                    getClasses(),
                                    handler);
   }

   public EJBHome getEJBHome() throws RemoteException

   {
      throw new UnsupportedOperationException();
   }

   public Handle getHandle() throws RemoteException
   {
      throw new UnsupportedOperationException();
   }

   public Object getPrimaryKey() throws RemoteException
   {
      throw new UnsupportedOperationException();
   }

   public boolean isIdentical(EJBObject parm1) throws RemoteException
   {
      throw new UnsupportedOperationException();
   }

   public void remove() throws RemoteException, RemoveException
   {
      throw new UnsupportedOperationException();
   }
}
