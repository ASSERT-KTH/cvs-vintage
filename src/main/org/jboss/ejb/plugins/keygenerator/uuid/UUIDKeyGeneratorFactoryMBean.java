/*
* JBoss, the OpenSource EJB server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.keygenerator.uuid;

import org.jboss.system.ServiceMBean;


/**
 * MBean interface for HiLoKeyGeneratorFactory
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 *
 * @version $Revision: 1.1 $
 */
public interface UUIDKeyGeneratorFactoryMBean
   extends ServiceMBean
{
   // Public ------------------------------------------
   /**
    * @return factory's JNDI name
    */
   public String getFactoryName();

   /**
    * @param factoryJNDI  factory's JNDI name.
    */
   public void setFactoryName(String factoryJNDI);
}
