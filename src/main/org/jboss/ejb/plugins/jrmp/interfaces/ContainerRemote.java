/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.lang.reflect.Method;
import java.security.Principal;
import java.rmi.Remote;
import java.rmi.MarshalledObject;

import javax.transaction.Transaction;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard �berg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.4 $
 */
public interface ContainerRemote
   extends Remote
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------
   public static long startup = System.currentTimeMillis();
   
   // Public --------------------------------------------------------

   // ContainerRemote implementation --------------------------------
   public Object invokeHome(MarshalledObject mi)
      throws Exception;

   public Object invoke(MarshalledObject mi)
      throws Exception;

   public Object invokeHome(Method m, Object[] args, Transaction tx, Principal identity)
      throws Exception;

   public Object invoke(Object id, Method m, Object[] args, Transaction tx, Principal identity)
      throws Exception;
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

