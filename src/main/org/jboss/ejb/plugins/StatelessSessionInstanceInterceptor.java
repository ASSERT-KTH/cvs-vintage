/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
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
      return getNext().invokeHome(mi);
   }

   public Object invoke(MethodInvocation mi)
      throws Exception
   {
      // Get context
      mi.setEnterpriseContext(container.getInstancePool().get());
      
      try
      {
         // Invoke through interceptors
         return getNext().invoke(mi);
      } finally
      {
         // Return context
         container.getInstancePool().free(mi.getEnterpriseContext());
      }
   }
}

