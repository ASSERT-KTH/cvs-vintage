/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;

/** The SecurityAssociation class maintains the security principal and
credentials. This can be done on either a singleton basis or a thread
local basis depending on the server property. When the server property has
been set to true, the security information is maintained in thread local
storage. The type of thread local storage depends on the
org.jboss.security.SecurityAssociation.ThreadLocal property.
If this property is true, then the thread local storage object is of
type java.lang.ThreadLocal which results in the current thread's
security information NOT being propagated to child threads.
 
When the property is false or does not exist, the thread local storage object
is of type java.lang.InheritableThreadLocal, and any threads spawned by the
current thread will inherit the security information of the current thread.
Subseqent changes to the current thread's security information are NOT
propagated to any previously spawned child threads.

When the server property is false, security information is maintained in
class variables which makes the information available to all threads within
the current VM.

@author Daniel O'Connor (docodan@nycap.rr.com)
@author Scott_Stark@displayscape.com
@version $Revision: 1.3 $
*/
public final class SecurityAssociation
{
    private static boolean server;
    private static Principal principal;
    private static Object credential;
    private static ThreadLocal thread_principal;
    private static ThreadLocal thread_credential;
    static
    {
        boolean useThreadLocal = Boolean.getBoolean("org.jboss.security.SecurityAssociation.ThreadLocal");
        if( useThreadLocal )
        {
            thread_principal = new ThreadLocal();
            thread_credential = new ThreadLocal();
        }
        else
        {
            thread_principal = new InheritableThreadLocal();
            thread_credential = new InheritableThreadLocal();
        }
    }

    /** Get the current principal information.
    @return Principal, the current principal identity.
    */
    public static Principal getPrincipal()
    {
      if (server)
        return (Principal) thread_principal.get();
      else
        return principal;
    }

    /** Get the current principal credential information. This can be of
     any type including: a String password, a char[] password, an X509 cert,
     etc.
    @return Object, the credential that proves the principal identity.
    */
    public static Object getCredential()
    {
      if (server)
        return thread_credential.get();
      else
        return credential;
    }

    /** Set the current principal information.
    @param principal, the current principal identity.
    */
    public static void setPrincipal( Principal principal )
    {
      if (server)
        thread_principal.set( principal );
      else
        SecurityAssociation.principal = principal;
    }

    /** Set the current principal credential information. This can be of
     any type including: a String password, a char[] password, an X509 cert,
     etc.
    @param credential, the credential that proves the principal identity.
    */
    public static void setCredential( Object credential )
    {
      if (server)
        thread_credential.set( credential );
      else
        SecurityAssociation.credential = credential;
    }

    /** Set the server mode of operation. When the server property has
    been set to true, the security information is maintained in thread local
    storage. This should be called to enable property security semantics
    in any multi-threaded environment where more than one thread requires
    that security information be restricted to the thread's flow of control.
    */
    public static void setServer()
    {
      server = true;
    }
}

