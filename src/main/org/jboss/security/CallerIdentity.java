/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;



/**
 * The CallerIdentity is a principal that may have a credential.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class CallerIdentity extends SimplePrincipal
{
   /** The run-as role */
   private Object credential;

   // hash code cache
   private int hashCode;

   /**
    * Construct an unmutable instance of a CallerIdentity
    */
   public CallerIdentity(String principal, Object credential)
   {
      super(principal);
      this.credential = credential;
   }

   public Object getCredential()
   {
      return credential;
   }

   /**
    * Returns a string representation of the object.
    * @return a string representation of the object.
    */
   public String toString()
   {
      return "[principal=" + getName() + "]";
   }

   /**
    * Indicates whether some other object is "equal to" this one.
    */
   public boolean equals(Object obj)
   {
      if (obj == null) return false;
      if (obj instanceof CallerIdentity)
      {
         CallerIdentity other = (CallerIdentity)obj;
         return getName().equals(other.getName());
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
