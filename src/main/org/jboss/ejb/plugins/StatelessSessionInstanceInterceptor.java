/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.ejb.EJBObject;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.NoSuchEntityException;
import javax.ejb.RemoveException;
import javax.ejb.EntityBean;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatelessSessionContainer;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.InstanceCache;
import org.jboss.ejb.InstancePool;
import org.jboss.ejb.MethodInvocation;

/**
 *   This container acquires the given instance. This must be used after
 *   the EnvironmentInterceptor, since acquiring instances requires a proper
 *   JNDI environment to be set
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.7 $
 */
public class StatelessSessionInstanceInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	protected StatelessSessionContainer container;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container) 
   { 
   	this.container = (StatelessSessionContainer)container; 
   }

   public  Container getContainer()
   {
   	return container;
   }
	
   // Interceptor implementation --------------------------------------
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
   	  // We don't need an instance since the call will be handled by container
      return getNext().invokeHome(mi);
   }

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

