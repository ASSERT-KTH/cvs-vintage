/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cluster;

import java.rmi.*;
import javax.management.*;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public interface ClusterRemote
   extends Remote
{
   // Constants -----------------------------------------------------
    
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setMaster(ClusterRemote node)
      throws RemoteException;
      
   public ClusterRemote getMaster()
      throws RemoteException;
      
   public java.lang.Object invoke(ObjectName name,
                               java.lang.String actionName,
                               java.lang.Object[] params,
                               java.lang.String[] signature)
      throws InstanceNotFoundException,
                               MBeanException,
                               ReflectionException, RemoteException;
}

