/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.security.plugins;

import java.io.Serializable;
import java.util.Set;
import java.security.Principal;
import javax.security.auth.Subject;

import org.jboss.security.RealmMapping;
import org.jboss.security.SubjectSecurityManager;


/** An implementation of SubjectSecurityManager, RealmMapping that authenticates
everyone and for which Principals have any role requested. It can be used
as a pass-through security manager when you want noop security.

@see #isValid(Principal, Object)
@see #Principal getPrincipal(Principal)
@see #doesUserHaveRole(Principal, Set)

@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.2 $
*/
public class NullSecurityManager
    implements SubjectSecurityManager, RealmMapping, Serializable
{
    private String securityDomain;

    /** Creates a default JaasSecurityManager for with the
        given securityDomain name.
    */
    public NullSecurityManager(String securityDomain)
    {
        this.securityDomain = securityDomain;
    }

    /** Get the name of the security domain associated with this security mgr.
        @return Name of the security manager security domain.
     */
    public String getSecurityDomain()
    {
        return securityDomain;
    }
    /** Get the currently authenticated Subject.
        @return Always returns null.
     */
    public Subject getActiveSubject()
    {
        return null;
    }

    /** Validate that the given credential is correct for principal.
    @return true if the principal was authenticated, false otherwise.
     */
    public boolean isValid(Principal principal, Object credential)
    {
        return true;
    }

    /** Always returns the argument principal.
    @return The argument principal
     */
    public Principal getPrincipal(Principal principal)
    {
        Principal result = principal;
        return result;
    }

    /** Does the current Subject have a role(a Principal) that equates to one
        of the role names. This method always returns true.
    @param principal, ignored.
    @param roleNames, ignored.
    @return Always returns true.
    */
    public boolean doesUserHaveRole(Principal principal, Set roleNames)
    {
        boolean hasRole = true;
        return hasRole;
    }

    /** Authenticate principal against credential
     * @param principal, the user id to authenticate
     * @param credential, an opaque credential.
     * @return Always returns true.
     */
    private boolean authenticate(Principal principal, Object credential)
    {
        boolean authenticated = true;
        return authenticated;
    }

}
