/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import javax.security.auth.Subject;

import org.jboss.logging.Logger;

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
@version $Revision: 1.20 $
 */
public final class SecurityAssociation
{
   private static Logger log = Logger.getLogger(SecurityAssociation.class);
   /** A flag indicating if trace level logging should be performed */
   private static boolean trace;
   /** A flag indicating if security information is global or thread local */
   private static boolean server;
   /** The SecurityAssociation principal used when the server flag is false */
   private static Principal principal;
   /** The SecurityAssociation credential used when the server flag is false */
   private static Object credential;
   /** The SecurityAssociation Subject used when the server flag is false */
   private static Subject subject;

   /** The SecurityAssociation principal used when the server flag is true */
   private static ThreadLocal threadPrincipal;
   /** The SecurityAssociation credential used when the server flag is true */
   private static ThreadLocal threadCredential;
   /** The SecurityAssociation Subject used when the server flag is true */
   private static ThreadLocal threadSubject;
   /** The SecurityAssociation HashMap<String, Object> */
   private static ThreadLocal threadContextMap;

   /** Thread local stacks of run-as principal roles used to implement J2EE
    run-as identity propagation */
   private static RunAsThreadLocalStack threadRunAsStacks = new RunAsThreadLocalStack();
   /** The permission required to access getPrincpal, getCredential */
   private static final RuntimePermission getPrincipalInfoPermission =
      new RuntimePermission("org.jboss.security.SecurityAssociation.getPrincipalInfo");
   /** The permission required to access getSubject */
   private static final RuntimePermission getSubjectPermission =
      new RuntimePermission("org.jboss.security.SecurityAssociation.getSubject");
   /** The permission required to access setPrincpal, setCredential, setSubject */
   private static final RuntimePermission setPrincipalInfoPermission =
      new RuntimePermission("org.jboss.security.SecurityAssociation.setPrincipalInfo");
   /** The permission required to access setServer */
   private static final RuntimePermission setServerPermission =
      new RuntimePermission("org.jboss.security.SecurityAssociation.setServer");
   /** The permission required to access pushRunAsIdentity/popRunAsIdentity */
   private static final RuntimePermission setRunAsIdentity =
      new RuntimePermission("org.jboss.security.SecurityAssociation.setRunAsRole");
   /** The permission required to get the current security context info */
   private static final RuntimePermission getContextInfo =
      new RuntimePermission("org.jboss.security.SecurityAssociation.accessContextInfo", "get");
   /** The permission required to set the current security context info */
   private static final RuntimePermission setContextInfo =
      new RuntimePermission("org.jboss.security.SecurityAssociation.accessContextInfo", "set");

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

      trace = log.isTraceEnabled();
      if( useThreadLocal )
      {
         threadPrincipal = new ThreadLocal();
         threadCredential = new ThreadLocal();
         threadSubject = new ThreadLocal();
         threadContextMap = new ThreadLocal()
         {
            protected Object initialValue()
            {
               return new HashMap();
            }
         };
      }
      else
      {
         threadPrincipal = new InheritableThreadLocal();
         threadCredential = new InheritableThreadLocal();
         threadSubject = new InheritableThreadLocal();
         threadContextMap = new InheritableThreadLocal()
         {
            protected Object initialValue()
            {
               return new HashMap();
            }
         };
      }
   }

   /** Get the current principal information.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.getPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    @return Principal, the current principal identity.
    */
   public static Principal getPrincipal()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(getPrincipalInfoPermission);

      if (peekRunAsIdentity() != null)
         return peekRunAsIdentity();

      if (server)
         return (Principal) threadPrincipal.get();
      else
         return principal;
   }

   /** Get the caller's principal information.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.getPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    @return Principal, the current principal identity.
    */
   public static Principal getCallerPrincipal()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(getPrincipalInfoPermission);

      if (peekRunAsIdentity(1) != null)
         return peekRunAsIdentity(1);
      if (server)
         return (Principal) threadPrincipal.get();
      else
         return principal;
   }

   /** Get the current principal credential information. This can be of
    any type including: a String password, a char[] password, an X509 cert,
    etc.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.getPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    @return Object, the credential that proves the principal identity.
    */
   public static Object getCredential()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(getPrincipalInfoPermission);

      if (peekRunAsIdentity() != null)
         return peekRunAsIdentity().getCredential();

      if (server)
         return threadCredential.get();
      else
         return credential;
   }

   /** Get the current Subject information. If a security manager is present,
    then this method calls the security manager's checkPermission
    method with a  RuntimePermission("org.jboss.security.SecurityAssociation.getSubject")
    permission to ensure it's ok to access principal information.
    If not, a SecurityException will be thrown. Note that this method does not
    consider whether or not a run-as identity exists. For access to this
    information see the JACC PolicyContextHandler registered under the key
    "javax.security.auth.Subject.container"
    @see javax.security.jacc.PolicyContext#getContext(String)

    @return Subject, the current Subject identity.
    */
   public static Subject getSubject()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(getSubjectPermission);

      if (server)
         return (Subject) threadSubject.get();
      else
         return subject;
   }

   /** Set the current principal information.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.setPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    @param principal - the current principal identity.
    */
   public static void setPrincipal( Principal principal )
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setPrincipalInfoPermission);

      if( trace )
         log.trace("setPrincipal, p="+principal+", server="+server);
      if (server)
         threadPrincipal.set( principal );
      else
         SecurityAssociation.principal = principal;
   }
   
   /** Set the current principal credential information. This can be of
    any type including: a String password, a char[] password, an X509 cert,
    etc.

    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.setPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    @param credential - the credential that proves the principal identity.
    */
   public static void setCredential( Object credential )
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setPrincipalInfoPermission);

      if (server)
         threadCredential.set( credential );
      else
         SecurityAssociation.credential = credential;
   }

   /** Set the current Subject information.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.setPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    @param subject - the current identity.
    */
   public static void setSubject(Subject subject)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setPrincipalInfoPermission);

      if( trace )
         log.trace("setSubject, s="+subject+", server="+server);
      if (server)
         threadSubject.set( subject );
      else
         SecurityAssociation.subject = subject;
   }

   /** Get the current thread context info.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.accessContextInfo", "get")
    </code>
    permission to ensure it's ok to access context information.
    If not, a <code>SecurityException</code> will be thrown.
    @param key - the context key
    @return the mapping for the key in the current thread context
    */
   public static Object getContextInfo(Object key)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(getContextInfo);

      HashMap contextInfo = (HashMap) threadContextMap.get();
      return contextInfo.get(key);
   }

   /** Set the current thread context info.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.accessContextInfo", "set")
    </code>
    permission to ensure it's ok to access context information.
    If not, a <code>SecurityException</code> will be thrown.
    @param key - the context key
    @param value - the context value to associate under key
    @return the previous mapping for the key if one exists
    */
   public static Object setContextInfo(Object key, Object value)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setContextInfo);

      HashMap contextInfo = (HashMap) threadContextMap.get();
      return contextInfo.put(key, value);
   }

   /** Clear all principal information.
    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.setPrincipalInfo")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    */
   public static void clear()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setPrincipalInfoPermission);

      if( trace )
         log.trace("clear, server="+server);
      if( server == true )
      {
         threadPrincipal.set(null);
         threadCredential.set(null);
         threadSubject.set(null);
      }
      else
      {
         SecurityAssociation.principal = null;
         SecurityAssociation.credential = null;
         SecurityAssociation.subject = null;
      }
   }

   /** Push the current thread of control's run-as identity.
    */
   public static void pushRunAsIdentity(RunAsIdentity runAs)
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setRunAsIdentity);
      if( trace )
         log.trace("pushRunAsIdentity, runAs="+runAs);
      threadRunAsStacks.push(runAs);
   }
   /** Pop the current thread of control's run-as identity.
    */
   public static RunAsIdentity popRunAsIdentity()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setRunAsIdentity);
      RunAsIdentity runAs = threadRunAsStacks.pop();
      if( trace )
         log.trace("popRunAsIdentity, runAs="+runAs);
      return runAs;
   }

   /** Look at the current thread of control's run-as identity on the top
    * of the stack.
    */
   public static RunAsIdentity peekRunAsIdentity()
   {
      return peekRunAsIdentity(0);
   }

   /** Look at the current thread of control's run-as identity at the indicated
    * depth. Typically depth is either 0 for the identity the current caller
    * run-as that will be assumed, or 1 for the active run-as the previous
    * caller has assumed.
    * 
    * @return RunAsIdentity depth frames up.
    */
   public static RunAsIdentity peekRunAsIdentity(int depth)
   {
      RunAsIdentity runAs = threadRunAsStacks.peek(depth);
      return runAs;
   }

   /** Set the server mode of operation. When the server property has
    been set to true, the security information is maintained in thread local
    storage. This should be called to enable property security semantics
    in any multi-threaded environment where more than one thread requires
    that security information be restricted to the thread's flow of control.

    If a security manager is present, then this method calls the security
    manager's <code>checkPermission</code> method with a
    <code>
    RuntimePermission("org.jboss.security.SecurityAssociation.setServer")
    </code>
    permission to ensure it's ok to access principal information.
    If not, a <code>SecurityException</code> will be thrown.
    */
   public static void setServer()
   {
      SecurityManager sm = System.getSecurityManager();
      if( sm != null )
         sm.checkPermission(setServerPermission);

      server = true;
   }

   /** A subclass of ThreadLocal that implements a value stack
    using an ArrayList and implements push, pop and peek stack
    operations on the thread local ArrayList.
    */
   private static class RunAsThreadLocalStack extends ThreadLocal
   {
      protected Object initialValue()
      {
         return new ArrayList();
      }
      void push(RunAsIdentity runAs)
      {
         ArrayList stack = (ArrayList) super.get();
         stack.add(runAs);
      }
      RunAsIdentity pop()
      {
         ArrayList stack = (ArrayList) super.get();
         RunAsIdentity runAs = null;
         int lastIndex = stack.size() - 1;
         if( lastIndex >= 0 )
            runAs = (RunAsIdentity) stack.remove(lastIndex);
         return runAs;
      }
      RunAsIdentity peek(int depth)
      {
         ArrayList stack = (ArrayList) super.get();
         RunAsIdentity runAs = null;
         int lastIndex = stack.size() - 1 - depth;
         if( lastIndex >= 0 )
            runAs = (RunAsIdentity) stack.get(lastIndex);
         return runAs;
      }
   }
}
