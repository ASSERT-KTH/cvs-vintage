/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;

import org.jboss.ejb.Container;
import org.jboss.ejb.MessageDrivenContainer;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;
/**
 *   This container acquires the given instance. This must be used after
 *   the EnvironmentInterceptor, since acquiring instances requires a proper
 *   JNDI environment to be set
 * For MessageDriven Beans, we inherit the StatelessSession for now,
 * since message driven beans is much like them
 *
 *   @see <related>
 *   @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.4 $
 */
public class MessageDrivenInstanceInterceptor
   extends StatelessSessionInstanceInterceptor
{
    protected MessageDrivenContainer container;

    public void setContainer(Container container) 
   { 
   	this.container = (MessageDrivenContainer)container; 
   }

    // Overriden here, since these bastards don't have homes
    public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
       throw new Error("Not valid for MessageDriven beans");
   }
    // Interceptor implementation --------------------------------------


   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      // Get context
      mi.setEnterpriseContext(container.getInstancePool().get());
      
	  // There is no need for synchronization since the instance is always fresh also there should
	  // never be a tx associated with the instance.
	 
      try
      {
         // Invoke through interceptors
         return getNext().invoke(mi);
      } catch (RuntimeException e) // Instance will be GC'ed at MI return
      {
	  	mi.setEnterpriseContext(null);
	  	throw e;
      } catch (RemoteException e) // Instance will be GC'ed at MI return
      {
    	mi.setEnterpriseContext(null);
    	throw e;
      } catch (Error e) // Instance will be GC'ed at MI return
      {
	    mi.setEnterpriseContext(null);
	    throw e;
      } finally
      {
		// Return context
		if (mi.getEnterpriseContext() != null)
			container.getInstancePool().free(mi.getEnterpriseContext());
      }
   }
}

