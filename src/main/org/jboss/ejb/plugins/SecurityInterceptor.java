/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import javax.naming.InitialContext;

import org.jboss.ejb.Container;
import org.jboss.ejb.MethodInvocation;
import org.jboss.logging.Logger;

import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;

/** The SecurityInterceptor is where the EJB 2.0 declarative security model
is enforced.

@author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
@author Scott_Stark@displayscape.com
@version $Revision: 1.16 $
*/
public class SecurityInterceptor extends AbstractInterceptor
{
    /**
     * @clientCardinality 0..1
     * @supplierCardinality 1 
     */
    protected Container container;

    /**
     * @supplierCardinality 0..1
     * @supplierQualifier authentication
     * @clientCardinality 1..* 
     */
    protected EJBSecurityManager securityManager;

    /**
     * @supplierCardinality 0..1
     * @clientCardinality 1..*
     * @supplierQualifier identity mapping 
     */
    protected RealmMapping realmMapping;

    public SecurityInterceptor()
    {
    }

    public void setContainer(Container container)
    {
        this.container = container;
        securityManager = container.getSecurityManager();
        realmMapping = container.getRealmMapping();
    }

    public Container getContainer()
    {
        return container;
    }

   // Container implementation --------------------------------------
    public void start() throws Exception
    {
        super.start();
    }

    public Object invokeHome(MethodInvocation mi) throws Exception
    {
        // Authenticate the subject and apply any declarative security checks
        checkSecurityAssociation(mi, true);
        return getNext().invokeHome(mi);
    }
    public Object invoke(MethodInvocation mi) throws Exception
    {
        // Authenticate the subject and apply any declarative security checks
        checkSecurityAssociation(mi, false);
        return getNext().invoke(mi);
    }

    /** The EJB 2.0 declarative security algorithm:
     1. Authenticate the caller using the principal and credentials in the MethodInfocation
     2. Validate access to the method by checking the principal's roles against
        those required to access the method.
     */
    private void checkSecurityAssociation(MethodInvocation mi, boolean home)
        throws Exception
    {
        // if this isn't ok, bean shouldn't deploy
        if (securityManager == null)
        {
            return;
        }
        if (realmMapping == null)
        {
            throw new RemoteException("checkSecurityAssociation", new SecurityException("Role mapping manager has not been set"));
        }

        // Check the security info from the method invocation
        Principal principal = mi.getPrincipal();
        Object credential = mi.getCredential();
        if( securityManager.isValid(principal, credential) == false )
        {
            String msg = "Authentication exception, principal="+principal;
            Logger.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new RemoteException("checkSecurityAssociation", e);
        }
        else
        {
            SecurityAssociation.setPrincipal( principal );
            SecurityAssociation.setCredential( credential );
        }

        Set methodRoles = container.getMethodPermissions(mi.getMethod(), home);
        /* If the method has no assigned roles or the user does not have at
           least one of the roles then access is denied.
        */
        if( methodRoles == null || realmMapping.doesUserHaveRole(principal, methodRoles) == false )
        {
            String method = mi.getMethod().getName();
            String msg = "Insufficient method permissions, principal="+principal
                + ", method="+method+", requiredRoles="+methodRoles;
            Logger.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new RemoteException("checkSecurityAssociation", e);
        }
   }

}
