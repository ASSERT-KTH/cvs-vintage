/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.pool;

import java.util.Set;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;

import javax.security.auth.Subject;

import org.jboss.logging.Log;
import org.jboss.minerva.pools.ObjectPool;
import org.jboss.minerva.pools.PoolEvent;
import org.jboss.minerva.pools.PoolObjectFactory;
import org.jboss.resource.RARMetaData;

/**
 *   Pooling strategy for the connection manager
 *
 *   @see org.jboss.resource.ConnectionManagerImpl
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public interface PoolStrategy
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * Sets the connection event listener that will be registered with
    * every connection returned by <code>getManagedConnection</code>.
    */
   void setConnectionEventListener(ConnectionEventListener listener);

   /**
    * Obtains a managed connection, returning either a newly created
    * connection or one from a pool. The parameters are passed as-is
    * to the managed connection factory.
    */
   ManagedConnection getManagedConnection(Subject subject,
                                          ConnectionRequestInfo cxRequestInfo)
      throws ResourceException;

   /**
    * Indicates that the application component has finished with the
    * managed connection and it can be returned to the pool from
    * whence it came.
    */
   void releaseManagedConnection(ManagedConnection conn)
      throws ResourceException;

   /**
    * Consigns the managed connection to the deepest pits of hell, for
    * example in the case of a connection error. Note that the managed
    * connection <b>must still be released using
    * <code>releaseManagedConnection</code></b>.
    */
   void condemnManagedConnection(ManagedConnection conn)
      throws ResourceException;

   /**
    * Destroys all pooled managed connections.
    */
   void shutdown();

   // Inner classes -------------------------------------------------
}
