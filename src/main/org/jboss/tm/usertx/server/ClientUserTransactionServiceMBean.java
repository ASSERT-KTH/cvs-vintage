/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm.usertx.server;

import javax.management.ObjectName;
import org.jboss.util.jmx.ObjectNameFactory;

/**
 * MBean for ClientUserTransaction service.
 *
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @version $Revision: 1.6 $
 */
public interface ClientUserTransactionServiceMBean
   extends org.jboss.system.ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create("jboss:service=ClientUserTransaction");
}

