/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.system;

import java.security.Principal;

/**
 *      <description> 
 *      
 *      @see <related>
 *      @author Daniel O'Connor (docodan@nycap.rr.com)
 */

public final class SecurityAssociation
{
    private static boolean server;
    private static Principal principal;
    private static Object credential;
    private static ThreadLocal thread_principal = new ThreadLocal();
    private static ThreadLocal thread_credential = new ThreadLocal();

    public static Principal getPrincipal()
    {
      if (server)
        return (Principal) thread_principal.get();
      else
        return principal;
    }

    public static Object getCredential()
    {
      if (server)
        return thread_credential.get();
      else
        return credential;
    }

    public static void setPrincipal( Principal principal )
    {
      if (server)
        thread_principal.set( principal );
      else
        SecurityAssociation.principal = principal;
    }

    public static void setCredential( Object credential )
    {
      if (server)
        thread_credential.set( credential );
      else
        SecurityAssociation.credential = credential;
    }

    public static void setServer()
    {
      server = true;
    }
}

