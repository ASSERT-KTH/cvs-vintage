/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm.usertx.server;

/**
 *  MBean for ClientUserTransaction service.
 *
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.2 $
 */
public interface ClientUserTransactionServiceMBean
   extends org.jboss.util.ServiceMBean
{
   public static final String OBJECT_NAME = ":service=ClientUserTransaction";
}

