/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Method;

import java.rmi.RemoteException;

import javax.ejb.RemoveException;


/**
 * The interface for persisting stateful session beans.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.8 $
 */
public interface StatefulSessionPersistenceManager
   extends ContainerPlugin
{
   void createSession(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
      throws Exception;
      
   void activateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException;
   
   void passivateSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException;

   void removeSession(StatefulSessionEnterpriseContext ctx)
      throws RemoteException, RemoveException;
   
   void removePassivated(Object key);
}

