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
public interface EntityInstanceCacheMBean extends org.jboss.ejb.plugins.AbstractInstanceCacheMBean {

  void remove(java.lang.String id) throws java.lang.Exception;

}
