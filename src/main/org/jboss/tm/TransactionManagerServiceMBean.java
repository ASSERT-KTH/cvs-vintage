/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm;

import javax.management.ObjectName;

import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.system.ServiceMBean;

/**
 * The JMX management interface for the {@link TransactionManagerService} MBean.
 *      
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 * @version $Revision: 1.10 $
 */
public interface TransactionManagerServiceMBean
   extends ServiceMBean
{
   ObjectName OBJECT_NAME = ObjectNameFactory.create(":service=TransactionManager");
    
   int getTransactionTimeout();

   void setTransactionTimeout(int timeout);

   String getXidClassName();

   void setXidClassName(String name);
}

