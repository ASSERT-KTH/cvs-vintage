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

/**
 * RMI Interface for the server side Connector which
 * is nearly the same as the MBeanServer Interface but
 * has an additional RemoteException.
 *
 * @version <tt>$Revision: 1.6 $</tt>
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author  <A href="mailto:andreas@jboss.org">Andreas &quot;Mad&quot; Schaefer</A>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface RMIAdaptor
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
