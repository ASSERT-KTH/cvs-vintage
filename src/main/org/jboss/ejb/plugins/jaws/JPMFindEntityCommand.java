/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.jaws;

import java.lang.reflect.Method;
import org.jboss.ejb.EntityEnterpriseContext;
import java.rmi.RemoteException;
import javax.ejb.FinderException;

/**
 * Interface for JAWSPersistenceManager FindEntity Command.
 *      
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public interface JPMFindEntityCommand
{
   // Public --------------------------------------------------------
   
   public Object execute(Method finderMethod, 
                         Object[] args, 
                         EntityEnterpriseContext ctx)
      throws RemoteException, FinderException;
}
