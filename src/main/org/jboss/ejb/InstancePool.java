/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;

/**
 * Defines the model for a EnterpriseContext instance pool.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @version $Revision: 1.5 $
 */
public interface InstancePool
   extends ContainerPlugin
{
   /**
    * Get an instance without identity.
    * 
    * <p>Can be used by finders and create-methods, or stateless beans
    *
    * @return    Context/w instance
    * 
    * @throws Exception    RemoteException  
    */
   EnterpriseContext get() throws Exception;
   
   /**
    * Return an anonymous instance after invocation.
    *
    * @param ctx    The context to free.
    */
   void free(EnterpriseContext ctx);
   
   /**
    * Discard an anonymous instance after invocation.
    * This is called if the instance should not be reused, perhaps due to some
    * exception being thrown from it.
    *
    * @param ctx    The context to discard.
    */
   void discard(EnterpriseContext ctx);
}

