/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.HashSet;
import java.util.Set;

/**
 * The meta data object for the security-role-mapping element.
 *
 * The security-role-mapping element maps the user principal
 * to a different principal on the server. It can for example
 * be used to map a run-as-principal to more than one role.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class SecurityRoleMetaData extends MetaData
{
   private String roleName;
   private Set principals;

   public SecurityRoleMetaData(String roleName)
   {
      this.roleName = roleName;
      this.principals = new HashSet();
   }

   public void addPrincipalName(String principalName)
   {
      principals.add(principalName);
   }

   public String getRoleName()
   {
      return roleName;
   }

   public Set getPrincipals()
   {
      return principals;
   }
}
