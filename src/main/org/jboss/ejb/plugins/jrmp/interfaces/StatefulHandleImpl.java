/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.rmi.MarshalledObject;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import java.lang.reflect.Method;

import org.jboss.logging.Logger;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 * 	@author <a href="mailto:marc.fleury@telkel.com>Marc Fleury</a>
 *	@version $Revision: 1.2 $
 */
public class StatefulHandleImpl
   implements Handle
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   String name;
   Object id;
   
   // Static --------------------------------------------------------

    static Method getEJBObjectMethod;
    
    static {
        try {
            
            getEJBObjectMethod = Class.forName("javax.ejb.Handle").getMethod("getEJBObject", new Class[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
        }   
    }
    
   // Constructors --------------------------------------------------
   public StatefulHandleImpl(String name, Object id)
   {
      this.name = name;
      this.id = id;
   }
   
   // Public --------------------------------------------------------

   // Handle implementation -----------------------------------------
   public EJBObject getEJBObject()
      throws RemoteException
   {
      try
      {
         ContainerRemote container = (ContainerRemote) new InitialContext().lookup("invokers/"+name);
         
          // Create a new MethodInvocation for distribution
          System.out.println("I am about to invoke and getEJBOBject is "+getEJBObjectMethod.getName() +" My ID is "+id);
          RemoteMethodInvocation rmi = new RemoteMethodInvocation(null, getEJBObjectMethod, new Object[] {id});
             
          // MF FIXME: WE DEFINITLY NEED THE SECURITY ON SUCH A CALL...
          // We also need a pointer to the TM...:(
          
          // Set the transaction context
          //rmi.setTransaction(tm != null? tm.getTransaction() : null);
             
          // Set the security stuff
          // MF fixme this will need to use "thread local" and therefore same construct as above
          // rmi.setPrincipal(sm != null? sm.getPrincipal() : null);
          // rmi.setCredential(sm != null? sm.getCredential() : null);
          // is the credential thread local? (don't think so... but...)
          //rmi.setPrincipal( getPrincipal() );
          // rmi.setCredential( getCredential() );
          
          // Invoke on the remote server, enforce marshalling
          return (EJBObject) container.invokeHome(new MarshalledObject(rmi));
         
      
      } catch (Exception e) {
         throw new ServerException("Could not get EJBObject", e);
      }
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

