/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * MBean interface.
 * @see Container
 * @see EntityEnterpriseContext
 */
public interface EntityContainerMBean extends org.jboss.ejb.ContainerMBean {

  long getCacheSize() ;

   /**
    * Flush the cache
    */
  void flushCache() ;

}
