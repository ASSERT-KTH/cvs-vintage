/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.resource;

import java.security.Principal;

import javax.security.auth.Subject;

import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.resource.security.PrincipalMapping;
import org.jboss.security.SecurityAssociation;

/**
 *   Provides the configured (via <code>PrincipalMapping</code>)
 *   mapping from the principal an application component is running as
 *   to a <code>Subject</code> identifying the resource principal for
 *   container-managed resource sign-on.
 *
 *   @author Toby Allsopp (toby.allsopp@peace.com)
 *   @version $Revision: 1.1 $
 */
public class JBossResourceSubjectFactory
   implements ResourceSubjectFactory
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private PrincipalMapping principalMapping;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public JBossResourceSubjectFactory(PrincipalMapping principalMapping)
   {
      this.principalMapping = principalMapping;
   }

   // Public --------------------------------------------------------

   // ResourceSubjectFactory implementation -------------------------

   public Subject getSubject(ManagedConnectionFactory mcf, String cmName)
   {
      Principal callerPrincipal = SecurityAssociation.getPrincipal();
      return principalMapping.createSubject(callerPrincipal);
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
