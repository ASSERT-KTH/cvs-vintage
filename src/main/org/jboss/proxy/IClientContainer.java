package org.jboss.proxy;

import java.util.ArrayList;

import org.jboss.invocation.InvocationContext;

/** An interface implemented by the ClientContainer to provide access to
 * the client proxy interceptors and InvocationContext.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.2 $
 */
public interface IClientContainer
{
   /**
    * Access a copy of the proxy container interceptor stack.
    * @return ArrayList<org.jboss.proxy.Interceptor>
    */ 
   public ArrayList getInterceptors();
   /**
    * Set the proxy container interceptor stack.
    * @param interceptors - ArrayList<org.jboss.proxy.Interceptor> to
    * install as the new interceptor stack
    */ 
   public void setInterceptors(ArrayList interceptors);
   /**
    * Access the InvocationContext associated with the proxy by the
    * server side proxy factory. The contents of this will depend on
    * the proxy factory.
    * @return The proxy creation time InvocationContext
    */ 
   public InvocationContext getInvocationContext();
}
