/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * The RunAsIdentity is a Principal that associates the run-as principal
 * with his run-as role(s).
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.5 $
 */
public class RunAsIdentity extends CallerIdentity
{
   /** The run-as role principals */
   private Set runAsRoles = new HashSet();

   private static final String ANOYMOUS_PRINCIPAL = "anonymous";

   /**
    * Construct an inmutable instance of a RunAsIdentity
    */
   public RunAsIdentity(String roleName, String principalName)
   {
      // we don't support run-as credetials
      super(principalName != null ? principalName : ANOYMOUS_PRINCIPAL, null);

      if (roleName == null)
         throw new IllegalArgumentException("The run-as identity must have at least one role");

      runAsRoles.add(new SimplePrincipal(roleName));
   }

   /**
    * Construct an inmutable instance of a RunAsIdentity
    */
   public RunAsIdentity(String roleName, String principalName, Set extraRoleNames)
   {
      this(roleName, principalName);

      // these come from the assembly-descriptor
      if (extraRoleNames != null)
      {
         Iterator it = extraRoleNames.iterator();
         while (it.hasNext())
         {
            String extraRoleName = (String) it.next();
            runAsRoles.add(new SimplePrincipal(extraRoleName));
         }
      }
   }

   public Set getRunAsRoles()
   {
      return new HashSet(runAsRoles);
   }

   public boolean doesUserHaveRole(Principal role)
   {
      return runAsRoles.contains(role);
   }

   /**
    * True if the run-as principal has any of the method roles
    */
   public boolean doesUserHaveRole(Set methodRoles)
   {
      Iterator it = methodRoles.iterator();
      while (it.hasNext())
      {
         Principal role = (Principal) it.next();
         if (doesUserHaveRole(role))
            return true;
      }
      return false;
   }

   /**
    * Returns a string representation of the object.
    * @return a string representation of the object.
    */
   public String toString()
   {
      return "[roles=" + runAsRoles + ",principal=" + getName() + "]";
   }
}
