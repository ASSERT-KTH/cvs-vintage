/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.MarshalledObject;
import javax.naming.InitialContext;

import javax.ejb.EJBObject;
import javax.ejb.EJBHome;
import javax.naming.Name;

import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Rickard Öberg (rickard.oberg@telkel.com)
 * 		@author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *      @version $Revision: 1.18 $
 */
public class StatefulSessionProxy
   extends GenericProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   // Stateful beans come with a jboss generated identifier
   protected Object id;
   
   // Static --------------------------------------------------------

   static Method getPrimaryKey;
   static Method getHandle;
   static Method getEJBHome;
   static Method isIdentical;
   static Method toStr;
   static Method eq;
   static Method hash;
   
   static
   {
      try
      {
        // EJBObject methods
         getPrimaryKey = EJBObject.class.getMethod("getPrimaryKey", new Class[0]);
         getHandle = EJBObject.class.getMethod("getHandle", new Class[0]);
         getEJBHome = EJBObject.class.getMethod("getEJBHome", new Class[0]);
         isIdentical = EJBObject.class.getMethod("isIdentical", new Class[] { EJBObject.class });
         
        // Object methods
        toStr = Object.class.getMethod("toString", new Class[0]);
         eq = Object.class.getMethod("equals", new Class[] { Object.class });
         hash = Object.class.getMethod("hashCode", new Class[0]);
      } catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   // Constructors --------------------------------------------------
   public StatefulSessionProxy()
   {
      // For externalization to work
   }
   
   public StatefulSessionProxy(String name, ContainerRemote container, Object id, boolean optimize)
   {
       super(name, container, optimize);
    
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
      if (m.equals(toStr))
      {
         return name+":"+id.toString();
      }
      else if (m.equals(eq))
      {
         return invoke(proxy, isIdentical, args);
      }
      
      else if (m.equals(hash))
      {
        return new Integer(id.hashCode());
      }
      
      // Implement local EJB calls
       else if (m.equals(getHandle))
      {
         return new StatefulHandleImpl(name, id);
      }
	  
	  
	  else if (m.equals(getEJBHome))
      { 
         return (EJBHome) new InitialContext().lookup(name);
      }
	   
     
      else if (m.equals(getPrimaryKey))
      {
         // MF FIXME 
         // The spec says that SSB PrimaryKeys should not be returned and the call should throw an exception
         // However we need to expose the field *somehow* so we can check for "isIdentical"
         // For now we use a non-spec compliant implementation and just return the key as is
         // See jboss1.0 for the PKHolder and the hack to be spec-compliant and yet solve the problem
         
         // This should be the following call 
         //throw new RemoteException("Session Beans do not expose their keys, RTFS");
      
         // This is how it was solved in jboss1.0
         // throw new PKHolder("RTFS", id);
         
         // This is non-spec compliant but will do for now
         return id;
      }
      else if (m.equals(isIdentical))
      {
           // MF FIXME
         // See above, this is not correct but works for now (do jboss1.0 PKHolder hack in here)
         return new Boolean(((EJBObject)args[0]).getPrimaryKey().equals(id));
      }
      
      // If not taken care of, go on and call the container
      else
      {
          // Delegate to container
          // Optimize if calling another bean in same EJB-application
          if (optimize && isLocal())
          {
             return container.invoke( // The entity id, method and arguments for the invocation
                             id, m, args,
                          // Transaction attributes
                          getTransaction(),
                          // Security attributes
                          getPrincipal(), getCredential());
          } else
          {
          // Create a new MethodInvocation for distribution
             RemoteMethodInvocation rmi = new RemoteMethodInvocation(id, m, args);
             
          // Set the transaction context
          rmi.setTransactionPropagationContext(getTransactionPropagationContext());
             
          // Set the security stuff
          // MF fixme this will need to use "thread local" and therefore same construct as above
          // rmi.setPrincipal(sm != null? sm.getPrincipal() : null);
             // rmi.setCredential(sm != null? sm.getCredential() : null);
             // is the credential thread local? (don't think so... but...)
          rmi.setPrincipal( getPrincipal() );
             rmi.setCredential( getCredential() );
          
          // Invoke on the remote server, enforce marshalling
             return container.invoke(new MarshalledObject(rmi)).get();
          }
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
    super.writeExternal(out);
    out.writeObject(id);
   }
   
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
    super.readExternal(in);
    id = in.readObject();
   }
    
   // Private -------------------------------------------------------
    
   // Inner classes -------------------------------------------------
}

