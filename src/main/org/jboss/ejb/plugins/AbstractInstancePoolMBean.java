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
public interface AbstractInstancePoolMBean extends org.jboss.system.ServiceMBean {

  int getCurrentSize() ;

  int getMaxSize() ;

}
