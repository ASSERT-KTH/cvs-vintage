/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.inflow;

/**
 * MBean interface.
 */
public interface JBossMessageEndpointFactoryMBean extends org.jboss.system.ServiceMBean {

   /**
    * Display the configuration
    * @return the configuration    */
  java.lang.String getConfig() ;

}
