/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

/**
 * MBean interface.
 */
public interface AbstractInstanceCacheMBean {

   /**
    * From InstanceCache interface
    */
  void remove(java.lang.Object id) ;

   /**
    * Get the current cache size
    * @return the size of the cache    */
  long getCacheSize() ;

   /**
    * Flush the cache.
    */
  void flush() ;

   /**
    * Get the passivated count.
    * @return the number of passivated instances.    */
  long getPassivatedCount() ;

   /**
    * Display the cache policy.
    * @return the cache policy as a string.    */
  java.lang.String getCachePolicyString() ;

}
