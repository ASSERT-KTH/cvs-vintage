/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;

/**
 * The RunAsIdentity is a CallerIdentity that associates the run-as principal
 * with his run-as role.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.4 $
 */
public class RunAsIdentity extends CallerIdentity
{
   /** The run-as role */
   private Principal runAsRole;
   private boolean anonymousPrincipal;

   // hash code cache
   private int hashCode;

   // The name of the anonymous run-as principal
   private static final String ANONYMOUS_PRINCIPAL = "anonymous";

   /**
    * Construct an unmutable instance of an anonymous RunAsIdentity
    */
   public RunAsIdentity(String runAsRole)
   {
      super(ANONYMOUS_PRINCIPAL, null);

      if (runAsRole == null)
         throw new IllegalArgumentException("runAsRole cannot be null");

      this.runAsRole = new SimplePrincipal(runAsRole);
      this.anonymousPrincipal = true;
   }

   /**
    * Construct an unmutable instance of a RunAsIdentity
    */
   public RunAsIdentity(String runAsRole, String runAsPrincipal, Object runAsCredential)
   {
      super(runAsPrincipal != null ? runAsPrincipal : ANONYMOUS_PRINCIPAL, runAsCredential);
      
      if (runAsRole == null)
         throw new IllegalArgumentException("runAsRole cannot be null");

      this.runAsRole = new SimplePrincipal(runAsRole);
      this.anonymousPrincipal = (runAsPrincipal == null);
   }

   public Principal getRunAsRole()
   {
      return runAsRole;
   }

   public boolean isAnonymousPrincipal()
   {
      return anonymousPrincipal;
   }

   /**
    * Returns a string representation of the object.
    * @return a string representation of the object.
    */
   public String toString()
   {
      return "[role=" + runAsRole + ",principal=" + getName() + "]";
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
         return super.equals(obj) && runAsRole.equals(other.runAsRole);
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
