/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource.security;

import java.security.Principal;

import javax.security.auth.Subject;

import javax.resource.spi.security.PasswordCredential;

/**
 *   A principal mapping that maps all caller principals to a single
 *   resource principal. Currently only basic password authentication
 *   is supported.
 *
 *   <p> The properties string is expected to contain (in
 *   <code>Properties.load</code> format) two properties:
 *   <code>userName</code> and <code>password</code>. These are used
 *   to construct the <code>PasswordCredential</code> attached to the
 *   resource principal.
 *
 *   <p> Additionally, the properties can contain a
 *   <code>principalName</code> property that specifies the name of
 *   the resource principal. If this property is not set then the
 *   <code>userName</code> is used.
 *
 *   @see org.jboss.resource.ConnectionManagerImpl
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class ManyToOnePrincipalMapping
   extends PrincipalMappingSupport
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private Principal resourcePrincipal;
   private String userName;
   private String password;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // PrincipalMappingSupport overrides -----------------------------

   public Subject createSubject(Principal callerPrincipal)
   {
      if (userName == null)
      {
         return null;
      }

      Subject subject = new Subject();
      subject.getPrincipals().add(resourcePrincipal);

      if (metadata.getAuthMechType().equals("basic-password"))
      {
         // The spec says that we need a new instance of this every
         // time, because it is specific to a managed connection
         // factory instance. We could probably get away with caching
         // one per MCF, but who really cares?
         PasswordCredential cred =
            new PasswordCredential(userName, password.toCharArray());
         cred.setManagedConnectionFactory(mcf);
         subject.getPrivateCredentials().add(cred);
      }
      else
      {
         throw new RuntimeException("Unsupported auth-mech-type: '" +
                                    metadata.getAuthMechType() + "'");
      }

      return subject;
   }

   protected void afterSetProperties()
   {
      userName = (String) properties.get("userName");
      password = (String) properties.get("password");
      if (password == null) password = "";

      String principalName = (String) properties.get("principalName");
      if (principalName == null)
         principalName = userName;
      resourcePrincipal = new ResourcePrincipal(principalName);
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   private static class ResourcePrincipal
      implements Principal
   {
      private final String name;

      private ResourcePrincipal(String name) { this.name = name; }

      public String getName() { return name; }

      public int hashCode() { return name.hashCode(); }

      public boolean equals(Object other)
      {
         if (other instanceof ResourcePrincipal)
            return ((ResourcePrincipal) other).name.equals(name);
         else
            return false;
      }

      public String toString() { return name; }

   }
}
