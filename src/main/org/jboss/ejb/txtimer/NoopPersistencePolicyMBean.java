/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

/**
 * MBean interface.
 * @since 09-Sep-2004
 */
public interface NoopPersistencePolicyMBean extends org.jboss.ejb.txtimer.PersistencePolicy {

   //default object name
   public static final javax.management.ObjectName OBJECT_NAME = org.jboss.mx.util.ObjectNameFactory.create("jboss.ejb:service=EJBTimerService,persistencePolicy=noop");

}
