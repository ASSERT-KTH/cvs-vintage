/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.keygenerator.uuid;

import javax.management.ObjectName;

import org.jboss.system.ServiceMBean;

import org.jboss.ejb.plugins.keygenerator.KeyGeneratorFactory;

/**
 * MBean interface for UUIDKeyGeneratorFactoryService
 *
 * @author <a href="mailto:loubyansky@ukr.net">Alex Loubyansky</a>
 *
 * @version $Revision: 1.2 $
 */
public interface UUIDKeyGeneratorFactoryServiceMBean
   extends org.jboss.system.ServiceMBean
{
   //default object name
   public static final javax.management.ObjectName OBJECT_NAME =
      org.jboss.util.jmx.ObjectNameFactory.create(
         "jboss.system:service=KeyGeneratorFactory,type=UUID");
}
