/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import java.util.*;

/**
 * The meta data object for the assembly-descriptor element.
 * This implementation only contains the security-role meta data
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.2 $
 */
public class AssemblyDescriptorMetaData extends MetaData
{
   /** The assembly-descriptor/security-roles */
   private HashMap securityRoles = new HashMap();

   public void addSecurityRoleMetaData(SecurityRoleMetaData srMetaData)
   {
      securityRoles.put(srMetaData.getRoleName(), srMetaData);
   }

   public HashMap getSecurityRoles()
   {
      return new HashMap(securityRoles);
   }

   public SecurityRoleMetaData getSecurityRoleByName(String roleName)
   {
      return (SecurityRoleMetaData)securityRoles.get(roleName);
   }

   public Set getSecurityRoleNamesByPrincipal(String userName)
   {
      HashSet roleNames = new HashSet();
      Iterator it = securityRoles.values().iterator();
      while (it.hasNext())
      {
         SecurityRoleMetaData srMetaData = (SecurityRoleMetaData) it.next();
         if (srMetaData.getPrincipals().contains(userName))
            roleNames.add(srMetaData.getRoleName());
      }
      return roleNames;
   }
}
