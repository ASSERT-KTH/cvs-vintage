/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.rmi.RemoteException;

/**
 * ???
 *
 * @author Unknown
 * @version $Revision: 1.3 $
 */
public interface EntityPersistenceStore2
   extends EntityPersistenceStore
{
   /**
    * Returns a new instance of the bean class or a subclass of the bean class.
    * 
    * @return   the new instance
    *
    * @throws Exception
    */
   public Object createBeanClassInstance() throws Exception;

   /**
    * Initializes the instance context.
    * 
    * <p>This method is called before createEntity, and should
    *    reset the value of all cmpFields to 0 or null.
    *
    * @param ctx
    * 
    * @throws RemoteException
    */
   public void initEntity(EntityEnterpriseContext ctx) throws RemoteException;
}

