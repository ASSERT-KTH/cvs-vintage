/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.jms.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;

/**
 * The resource adapters own ConnectionManager, used in non-managed
 * environments.
 * 
 * <p>Will handle some of the houskeeping an appserver nomaly does.
 *
 * <p>Created: Thu Mar 29 16:09:26 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.4 $
 */
public class JmsConnectionManager
   implements ConnectionManager
{
   /**
    * Construct a <tt>JmsConnectionManager</tt>.
    */
   public JmsConnectionManager() {
      super();
   }

   /**
    * Allocate a new connection.
    *
    * @param mcf
    * @param cxRequestInfo
    * @return                   A new connection
    *
    * @throws ResourceException Failed to create connection.
    */
   public Object allocateConnection(ManagedConnectionFactory mcf,
                                    ConnectionRequestInfo cxRequestInfo) 
      throws ResourceException
   {
      ManagedConnection mc = mcf.createManagedConnection(null, cxRequestInfo);
      return mc.getConnection(null, cxRequestInfo);
   }
}
