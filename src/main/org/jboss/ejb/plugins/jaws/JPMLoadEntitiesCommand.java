/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.ejb.plugins.jaws;

import java.rmi.RemoteException;
import java.util.Map;
import org.jboss.util.FinderResults;
import org.jboss.ejb.plugins.jaws.metadata.PkFieldMetaData;

/**
 * Interface for JAWSPersistenceManager LoadEntities
 *      
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson)</a>
 * @version $Revision: 1.1 $
 */
public interface JPMLoadEntitiesCommand
{
   // Public --------------------------------------------------------
   
   public void execute(FinderResults keys)
      throws RemoteException;
}

