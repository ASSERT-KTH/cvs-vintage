/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.adaptor.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.management.ObjectName;
import javax.management.NotificationFilter;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;

/** An RMI interface extension of the standard MBeanServerConnection
 * 
 * @see javax.management.MBeanServerConnection
 *
 * @version $Revision: 1.2 $
 * @author Scott.Stark@jboss.org
 */
public interface RMIAdaptorExt 
   extends Remote, MBeanServerConnection
{
   /**
    *
    * @param name
    * @param listener
    * @param filter
    * @param handback
    * @throws InstanceNotFoundException
    * @throws RemoteException
    */
   void addNotificationListener(ObjectName name,
                                RMINotificationListener listener,
                                NotificationFilter filter,
                                Object handback)
      throws InstanceNotFoundException,
             RemoteException;

   /**
    *
    * @param name
    * @param listener
    * @throws InstanceNotFoundException
    * @throws ListenerNotFoundException
    * @throws RemoteException
    */
   void removeNotificationListener(ObjectName name, RMINotificationListener listener)
      throws InstanceNotFoundException,
             ListenerNotFoundException,
             RemoteException;
}
