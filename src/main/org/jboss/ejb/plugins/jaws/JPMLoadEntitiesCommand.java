/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.jaws;

import java.rmi.RemoteException;
import org.jboss.ejb.FinderResults;

/**
 * Interface for JAWSPersistenceManager LoadEntities - <b>pre</b> load data for a 
 * batch of entities. 'LoadEntities' is a bit of a misnomer - it should actually 
 * preload the data into a the FinderResults so that the LoadEntity command
 * can later get it. This somewhat circuitous route is needed so that we don't
 * violate the container contract by loading data before ejbActivate is called.
 *      
 * @author <a href="mailto:danch@nvisia.com">Dan Christopherson</a>
 * @version $Revision: 1.6 $
 */
public interface JPMLoadEntitiesCommand
{
   // Public --------------------------------------------------------
   
   public void execute(FinderResults keys)
      throws RemoteException;
}

