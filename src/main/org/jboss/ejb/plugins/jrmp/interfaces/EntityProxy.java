/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

/*
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.server.RemoteObject;
import javax.naming.Reference;
import javax.naming.BinaryRefAddr;
import javax.naming.Referenceable;
import javax.naming.Name;
import java.lang.reflect.Proxy;
*/
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.lang.reflect.Method;

import javax.ejb.EJBObject;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public abstract class EntityProxy
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected String name;
   protected ContainerRemote container;
   protected Object id;
   protected long containerStartup = ContainerRemote.startup;
   
   protected boolean optimize = false;
   
   // Static --------------------------------------------------------
   static Method getPrimaryKey;
   static Method getHandle;
   static Method isIdentical;
   static Method toStr;
   static Method eq;
   
   static
   {
      try
      {
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", new Class[0]);
         getHandle = EJBObject.class.getMethod("getHandle", new Class[0]);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] { EJBObject.class });
         toStr = Object.class.getMethod("toString", new Class[0]);
         eq = Object.class.getMethod("equals", new Class[] { Object.class });
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   // Constructors --------------------------------------------------
   public EntityProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
      this.name = name;
      this.container = container;
      this.id = id;
      this.optimize = optimize;
   }
   
   // Public --------------------------------------------------------

   // InvocationHandler implementation ------------------------------
   public Object invoke(Object proxy, Method m, Object[] args)
      throws Throwable
   {
      // Normalize args to always be an array
      // Isn't this a bug in the proxy call??
      if (args == null)
         args = new Object[0];
      
      // Implement local methods
      if (m.equals(getPrimaryKey))
      {
         return id;
      }
      else if (m.equals(getHandle))
      {
         return new EntityHandleImpl(name, id);
      }
      else if (m.equals(toStr))
      {
         return name+":"+id.toString();
      }
      else if (m.equals(eq))
      {
         return invoke(proxy, isIdentical, args);
      }
      else
      {
         // Delegate to container
         // Optimize if calling another bean in same EJB-application
         if (optimize && isLocal())
         {
            return container.invoke(id, m, args, null, null);
         }
                
         Object result = container.invoke(new MarshalledObject(new MethodInvocation(id, m, args)), null, null);
         if (result instanceof MarshalledObject)
            return ((MarshalledObject)result).get();
         else
            return result;
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
   private void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      
      if (isLocal())
      {
         // VM-local optimization; still follows RMI-semantics though
         container = MethodInvocation.getLocal(name);
      }
      
   }
   
   private boolean isLocal()
   {
      return containerStartup == ContainerRemote.startup;
   }
   // Inner classes -------------------------------------------------
}

