/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

/**
 *   Provides access to configuration parameters for a JCA connection
 *   factory.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public interface ConnectionFactoryConfig
{
   // Constants -----------------------------------------------------

   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    * The name of the class implementing {@link
    * org.jboss.resource.security.PrincipalMapping} that is to be used
    * for mapping caller principals to resource principals for this
    * connection factory.
    */
   String getPrincipalMappingClass();
   void setPrincipalMappingClass(String className);

   /**
    * A string in a format parseable by {@link
    * java.util.Properties#load} that defines the properties to set on
    * the <code>PrincipalMapping</code> for this connection factory
    * instance.
    */
   String getPrincipalMappingProperties();
   void setPrincipalMappingProperties(String properties);
    
   // Connection pooling parameters

   /**
    * The name of the pool strategy to use - if not set then an
    * automagic setting is chosen.
    *
    * @see org.jboss.resource.pool.PoolStrategyFactory
    */
   String getPoolStrategy();
   void setPoolStrategy(String strategyName);

   void setMinSize(int minSize);
   int getMinSize();
   void setMaxSize(int maxSize);
   int getMaxSize();
   void setBlocking(boolean blocking);
   boolean getBlocking();
   void setGCEnabled(boolean gcEnabled);
   boolean getGCEnabled();
   void setGCInterval(long interval);
   long getGCInterval();
   void setGCMinIdleTime(long idleMillis);
   long getGCMinIdleTime();
   void setIdleTimeoutEnabled(boolean enabled);
   boolean getIdleTimeoutEnabled();
   void setIdleTimeout(long idleMillis);
   long getIdleTimeout();
   void setMaxIdleTimeoutPercent(float percent);
   float getMaxIdleTimeoutPercent();
   void setInvalidateOnError(boolean invalidate);
   boolean getInvalidateOnError();
   void setTimestampUsed(boolean timestamp);
   boolean getTimestampUsed();
}
