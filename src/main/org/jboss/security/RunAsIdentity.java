/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;
import java.util.ArrayList;
import javax.security.auth.Subject;

/**
 * The RunAsIdentity is a principal that associates the run-as principal
 * with his run-as role.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class RunAsIdentity extends SimplePrincipal
{
   /** The run-as role */
   private Principal runAsRole;

   // hash code cache
   private int hashCode;

   /**
    * Construct an unmutable instance of a RunAsIdentity
    */
   public RunAsIdentity(String runAsRole, String runAsPrincipal)
   {
      super(runAsPrincipal != null ? runAsPrincipal : "nobody");

      if (runAsRole == null)
         throw new IllegalArgumentException("runAsRole cannot be null");

      this.runAsRole = new SimplePrincipal(runAsRole);
   }

   /**
    * Get the run-as role.
    */
   public Principal getRunAsRole()
   {
      return runAsRole;
   }

   /**
    * Returns a string representation of the object.
    * @return a string representation of the object.
    */
   public String toString()
   {
      return "RunAsIdentity[role=" + runAsRole + ",principal=" + getName() + "]";
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    */
   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj instanceof RunAsIdentity)
      {
         RunAsIdentity other = (RunAsIdentity)obj;
         return getName().equals(other.getName()) && runAsRole.equals(other.runAsRole);
      }
      return false;
   }

   /**
    * Returns a hash code value for the object.
    */
   public int hashCode()
   {
      if (hashCode == 0)
      {
         hashCode = toString().hashCode();
      }
      return hashCode;
   }
}
