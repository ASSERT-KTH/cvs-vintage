/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

import java.rmi.RemoteException;

/** An extension of the RMI RemoteException that is used to indicate
 * that there are no target services available for an invocation. An example
 * usage is for an HA proxy to throw this exception when all targets are
 * found to be unavailable.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class ServiceUnavailableException extends RemoteException
{
   public ServiceUnavailableException()
   {
      super();
   }

   public ServiceUnavailableException(String s)
   {
      super(s);
   }

   public ServiceUnavailableException(String s, Throwable cause)
   {
      super(s, cause);
   }
}
