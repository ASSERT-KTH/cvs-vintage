/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc2.schema;

/**
 * MBean interface.
 */
public interface PartitionedTableCacheMBean extends org.jboss.system.ServiceMBean {

  void registerListener(Cache.Listener listener) ;

  int size() ;

  int getMaxCapacity() ;

  void setMaxCapacity(int maxCapacity) ;

  int getMinCapacity() ;

  int getPartitionsTotal() ;

  int getMinPartitionCapacity() ;

  int getMaxPartitionCapacity() ;

}
