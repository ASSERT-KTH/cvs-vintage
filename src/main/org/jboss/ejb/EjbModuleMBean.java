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
 * @see EJBDeployer
 */
public interface EjbModuleMBean extends org.jboss.system.ServiceMBean {

   /**
    * Get all containers in this deployment unit.
    * @return a collection of containers for each enterprise bean in this deployment unit.
    */
  java.util.Collection getContainers() ;

}
