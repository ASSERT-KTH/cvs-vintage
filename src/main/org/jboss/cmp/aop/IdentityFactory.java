/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cmp.aop;

/**
 * The factory for persistable instance identities.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 */
public class IdentityFactory
{
   // Attributes ----------------------------------------------
   private static final IdentityFactory instance = new IdentityFactory();

   // Static --------------------------------------------------
   public static IdentityFactory getInstance()
   {
      return instance;
   }

   // Public --------------------------------------------------
   /**
    * Generates unique identity for persistable instance depending on instance type
    * and metadata.
    *
    * @return generated unique identity
    */
   public Identity createIdentity()
   {
      return new Identity(){};
   }
}
