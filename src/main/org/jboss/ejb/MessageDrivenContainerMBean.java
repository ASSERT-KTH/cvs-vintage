/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

/**
 * MBean interface.
 */
public interface MessageDrivenContainerMBean extends org.jboss.ejb.ContainerMBean {

  long getMessageCount() ;

}
