/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

import org.jboss.logging.Logger;

/** An ObjectInputStream subclass used by the MarshalledValue class to
 ensure the classes and proxies are loaded using the thread context
 class loader.

@author Scott.Stark@jboss.org
@version $Revision: 1.1 $
 */
public class MarshalledValueInputStream extends ObjectInputStream
{
   private static Logger log = Logger.getLogger(MarshalledValueInputStream.class);

   /** Creates a new instance of MarshalledValueOutputStream */
   public MarshalledValueInputStream(InputStream is) throws IOException
   {
      super(is);
   }

   /** Use the thread context class loader to resolve the class
    * @exception IOException Any exception thrown by the underlying OutputStream.
    */
   protected Class resolveClass(ObjectStreamClass v) throws IOException,
      ClassNotFoundException
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      String className = v.getName();
      return loader.loadClass(className);
   }

   protected Class resolveProxyClass(String[] interfaces) throws IOException,
      ClassNotFoundException
   {
      if( log.isDebugEnabled() )
      {
         StringBuffer tmp = new StringBuffer("[");
         for(int i = 0; i < interfaces.length; i ++)
         {
            if( i > 0 )
               tmp.append(',');
            tmp.append(interfaces[i]);
         }
         tmp.append(']');
         log.debug("resolveProxyClass called, ifaces="+tmp.toString());
      }
      // Load the interfaces from the thread context class loader
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      Class[] ifaceClasses = new Class[interfaces.length];
      for (int i = 0; i < interfaces.length; i++)
      {
          ifaceClasses[i] = loader.loadClass(interfaces[i]);
      }
      return java.lang.reflect.Proxy.getProxyClass(loader, ifaceClasses);
   }
}
