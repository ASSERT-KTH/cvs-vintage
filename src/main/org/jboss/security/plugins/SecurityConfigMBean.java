/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.plugins;

import org.jboss.system.ServiceMBean;

/** A security configuration MBean. This establishes the JAAS and Java2
 security properties and related configuration.

 Currently this only sets the "java.security.auth.login.config" system
 property to the URL to the AuthConf attribute.

@author Scott.Stark@jboss.org
@version $Revision: 1.1 $
*/
public interface SecurityConfigMBean extends ServiceMBean
{
   /** Get the resource path to the JAAS login configuration file to use.
    */
   public String getAuthConf();
   /** Set the resource path to the JAAS login configuration file to use.
    The default is "auth.conf".
    */
   public void setAuthConf(String authConf);
}
