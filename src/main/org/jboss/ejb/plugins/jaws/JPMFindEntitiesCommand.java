/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.jaws;

import java.util.Collection;
import java.lang.reflect.Method;
import org.jboss.ejb.EntityEnterpriseContext;
import java.rmi.RemoteException;
import javax.ejb.FinderException;

/**
 * Interface for JAWSPersistenceManager FindEntities Command.
 *      
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public interface JPMFindEntitiesCommand
{
   // Public --------------------------------------------------------
   
   public Collection execute(Method finderMethod, 
                             Object[] args, 
                             EntityEnterpriseContext ctx)
      throws RemoteException, FinderException;
}
