/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.lang.reflect.Method;

import javax.ejb.EJBObject;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 *      @version $Revision: 1.7 $
 */
public abstract class EntityProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected Object id;
   
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
		super(name, container, optimize);

      this.id = id;
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

   // Inner classes -------------------------------------------------
}

