/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.pool;

import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.logging.Log;
import org.jboss.resource.ConnectionFactoryConfig;
import org.jboss.resource.RARMetaData;

/**
 *   Creates {@link org.jboss.resource.pool.PoolStrategy}
 *   implementations.
 *
 *   @see org.jboss.resource.ConnectionManagerImpl
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class PoolStrategyFactory
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   /**
    * Creates a new instance of an implementation of
    * <code>PoolStrategy</code> for managing connections created by
    * the specified managed connection factory.
    *
    * @param metadata used to select the pool strategy implementation
    * @param mcf the managed connection factory from which connections
    *            will be managed
    */
   public static PoolStrategy getStrategy(RARMetaData rarMetadata,
                                          ConnectionFactoryConfig cfConfig,
                                          ManagedConnectionFactory mcf,
                                          Log log)
   {
      String manualStrategy = cfConfig.getPoolStrategy();
      if (manualStrategy != null)
      {
         if (manualStrategy.equals("Single"))
            return new SinglePoolStrategy(mcf, log);
         if (manualStrategy.equals("Group"))
            return new GroupBySecurityStrategy(mcf, log);
         log.warning("Unrecognized pool strategy '" + manualStrategy + "', " +
                     "using automatic selection");
      }

      // We only want to use the single pool strategy if we know that
      // there's likely to only be one security context, i.e. only one
      // resource principal. Also, we should avoid the single pool
      // strategy if the resource adapter can't do reauthentication
      // because otherwise we might end up with lengthy
      // matchManagedConnection calls.
      if (!rarMetadata.getReauthenticationSupport() ||
          !cfConfig.getPrincipalMappingClass().equals(
             "org.jboss.resource.security.ManyToOnePrincipalMapping"))
      {
         log.debug("Choosing \"group by security\" pool strategy");
         return new GroupBySecurityStrategy(mcf, log);
      }
      else
      {
         log.debug("Choosing \"single pool \" pool strategy");
         return new SinglePoolStrategy(mcf, log);
      }
   }

   // Constructors --------------------------------------------------

   /**
    * Do not instantiate.
    */
   private PoolStrategyFactory() {}

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
