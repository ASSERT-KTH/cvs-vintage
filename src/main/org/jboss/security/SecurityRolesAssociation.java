/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.util.Map;

/**
 * The SecurityRolesAssociation uses a ThreadLocal to accociatw the SecurityRoleMetaData
 * from the deployment with the current thread.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public final class SecurityRolesAssociation
{
   /** Thread local that holds the deployment security roles */
   private static ThreadLocal threadSecurityRoleMapping = new ThreadLocal();

   /**
    * Get the current map of SecurityRoleMetaData.
    * @return A Map that stores SecurityRoleMetaData by roleName
    */
   public static Map getSecurityRoles()
   {
      return (Map) threadSecurityRoleMapping.get();
   }

   /**
    * Get the current map of SecurityRoleMetaData.
    */
   public static void setSecurityRoles(Map securityRoles)
   {
      threadSecurityRoleMapping.set(securityRoles);
   }

}
