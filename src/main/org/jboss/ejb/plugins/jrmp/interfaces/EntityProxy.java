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
 *      @version $Revision: 1.10 $
 */
public class EntityProxy
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
   static Method hash;
   
   static
   {
      try
      {
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", new Class[0]);
         getHandle = EJBObject.class.getMethod("getHandle", new Class[0]);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] { EJBObject.class });
         toStr = Object.class.getMethod("toString", new Class[0]);
         eq = Object.class.getMethod("equals", new Class[] { Object.class });
	      hash = Object.class.getMethod("hashCode", new Class[0]);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   // Constructors --------------------------------------------------
   public EntityProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
		super(name, container, optimize);

		if (id == null)
			throw new NullPointerException("Id may not be null");
			
      this.id = id;
   }
   
   // Public --------------------------------------------------------

   // InvocationHandler implementation ------------------------------
   public final Object invoke(Object proxy, Method m, Object[] args)
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
      else if (m.equals(isIdentical))
      {
			return new Boolean(((EJBObject)args[0]).getPrimaryKey().equals(id));
      }
      else if (m.equals(hash))
      {
      	return new Integer(id.hashCode());
      }
      else
      {
	      // Delegate to container
	      // Optimize if calling another bean in same EJB-application
	      if (optimize && isLocal())
	      {
	         return container.invoke(id, m, args, 
												tm != null ? tm.getTransaction() : null,
												null);
	      } else
	      {
	         RemoteMethodInvocation rmi = new RemoteMethodInvocation(id, m, args);
	         if (tm != null)
	            rmi.setTransaction(tm.getTransaction());
	         return container.invoke(new MarshalledObject(rmi));
	      }
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   protected void writeObject(java.io.ObjectOutputStream out)
      throws IOException
   {
		super.writeObject(out);
   	out.writeObject(id);
   }
   
   protected void readObject(java.io.ObjectInputStream in)
      throws IOException, ClassNotFoundException
   {
   	super.readObject(in);
   	id = in.readObject();
   }
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

