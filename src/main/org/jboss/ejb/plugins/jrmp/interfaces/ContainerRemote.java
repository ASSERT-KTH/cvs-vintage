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

import javax.naming.Name;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.3 $
 */
public interface ContainerRemote
   extends Remote
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------
   public static long startup = System.currentTimeMillis();
   
   // Public --------------------------------------------------------

   // ContainerRemote implementation --------------------------------
   public Object invokeHome(MarshalledObject mi, Object tx, Principal user)
      throws Exception;

   public Object invoke(MarshalledObject mi, Object tx, Principal user)
      throws Exception;

   public Object invokeHome(Method m, Object[] args, Object tx, Principal user)
      throws Exception;

   public Object invoke(Object id, Method m, Object[] args, Object tx, Principal user)
      throws Exception;
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

