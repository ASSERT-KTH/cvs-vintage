package org.jboss.proxy;

import java.util.ArrayList;

import org.jboss.invocation.InvocationContext;

/** An interface implemented by the ClientContainer to provide access to
 * the client proxy interceptors and InvocationContext.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public interface IClientContainer
{
   public ArrayList getInterceptors();
   public void setInterceptors(ArrayList interceptors);
   public InvocationContext getInvocationContext();
}
