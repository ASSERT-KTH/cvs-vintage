/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.4 $
 */
public interface InstancePool
   extends ContainerPlugin
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   /**
    *   Get an instance without identity.
    *   Can be used by finders and create-methods, or stateless beans
    *
    * @return     Context /w instance
    * @exception   RemoteException  
    */
   public EnterpriseContext get()
      throws Exception;
   
   /**
    *   Return an anonymous instance after invocation.
    *
    * @param   ctx  
    */
   public void free(EnterpriseContext ctx);
   
   /**
    *   Discard an anonymous instance after invocation.
    *   This is called if the instance should not be reused, perhaps due to some
    *   exception being thrown from it.
    *
    * @param   ctx  
    */
   public void discard(EnterpriseContext ctx);
}

