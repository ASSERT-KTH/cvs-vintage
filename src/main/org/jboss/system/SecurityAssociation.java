/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.security.Principal;

public class SecurityAssociation
{
    private static boolean server;
    private static Principal principal;
    private static Object credential;

    public static Principal getPrincipal()
    {
      return principal;
    }

    public static Object getCredential()
    {
      return credential;
    }

    public static void setPrincipal( Principal principal )
    {
      if (!server)
        SecurityAssociation.principal = principal;
    }

    public static void setCredential( Object credential )
    {
      if (!server)
        SecurityAssociation.credential = credential;
    }

    public static void setServer()
    {
      server = true;
    }
}
