/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;
import java.util.ArrayList;

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
@author Scott.Stark@jboss.org
@version $Revision: 1.8 $
*/
public final class SecurityAssociation
{
    private static boolean server;
    private static Principal principal;
    private static Object credential;
    private static ThreadLocal threadPrincipal;
    private static ThreadLocal threadCredential;
    private static RunAsThreadLocalStack threadRunAsStacks = new RunAsThreadLocalStack();

    static
    {
        boolean useThreadLocal = false;
        try
        {
            useThreadLocal = Boolean.getBoolean("org.jboss.security.SecurityAssociation.ThreadLocal");
        }
        catch(SecurityException e)
        {
            // Ignore and use the default
        }

        if( useThreadLocal )
        {
            threadPrincipal = new ThreadLocal();
            threadCredential = new ThreadLocal();
        }
        else
        {
            threadPrincipal = new InheritableThreadLocal();
            threadCredential = new InheritableThreadLocal();
        }
    }

    /** Get the current principal information.
    @return Principal, the current principal identity.
    */
    public static Principal getPrincipal()
    {
      if (server)
        return (Principal) threadPrincipal.get();
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
        return threadCredential.get();
      else
        return credential;
    }

    /** Set the current principal information.
    @param principal, the current principal identity.
    */
    public static void setPrincipal( Principal principal )
    {
      if (server)
        threadPrincipal.set( principal );
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
        threadCredential.set( credential );
      else
        SecurityAssociation.credential = credential;
    }

    /**
     */
    public static void pushRunAsRole(Principal runAsRole)
    {
        threadRunAsStacks.push(runAsRole);
    }
    public static Principal popRunAsRole()
    {
        Principal runAsRole = threadRunAsStacks.pop();
        return runAsRole;
    }
    public static Principal peekRunAsRole()
    {
        Principal runAsRole = threadRunAsStacks.peek();
        return runAsRole;
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

    /**
     */
    private static class RunAsThreadLocalStack extends ThreadLocal
    {
        protected Object initialValue()
        {
            return new ArrayList();
        }
        void push(Principal runAs)
        {
            ArrayList stack = (ArrayList) super.get();
            stack.add(runAs);
        }
        Principal pop()
        {
            ArrayList stack = (ArrayList) super.get();
            Principal runAs = null;
            int lastIndex = stack.size() - 1;
            if( lastIndex >= 0 )
                runAs = (Principal) stack.remove(lastIndex);
            return runAs;
        }
        Principal peek()
        {
            ArrayList stack = (ArrayList) super.get();
            Principal runAs = null;
            int lastIndex = stack.size() - 1;
            if( lastIndex >= 0 )
                runAs = (Principal) stack.get(lastIndex);
            return runAs;
        }
    }
}
