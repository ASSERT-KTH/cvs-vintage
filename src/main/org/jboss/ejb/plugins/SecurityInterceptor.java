/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import org.jboss.ejb.Container;
import org.jboss.ejb.MethodInvocation;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SecurityIdentityMetaData;
import org.jboss.security.AnybodyPrincipal;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/** The SecurityInterceptor is where the EJB 2.0 declarative security model
is enforced. This is where the caller identity propagation is controlled as well.

@author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
@author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
@version $Revision: 1.27 $
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
    protected AuthenticationManager securityManager;

    /**
     * @supplierCardinality 0..1
     * @clientCardinality 1..*
     * @supplierQualifier identity mapping
     */
    protected RealmMapping realmMapping;
    protected Principal runAsRole;

    public SecurityInterceptor()
    {
    }

    /** Called by the super class to set the container to which this interceptor
     belongs. We obtain the security manager and runAs identity to use here.
     */
    public void setContainer(Container container)
    {
        this.container = container;
        BeanMetaData beanMetaData = container.getBeanMetaData();
        SecurityIdentityMetaData secMetaData = beanMetaData.getSecurityIdentityMetaData();
        if( secMetaData != null && secMetaData.getUseCallerIdentity() == false )
        {
            String roleName = secMetaData.getRunAsRoleName();
            runAsRole = new SimplePrincipal(roleName);
        }
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
        /* If a run-as role was specified, push it so that any calls made
         by this bean will have the runAsRole available for declarative
         security checks.
        */
        if( runAsRole != null )
        {
            SecurityAssociation.pushRunAsRole(runAsRole);
        }
        try
        {
            Object returnValue = getNext().invokeHome(mi);
            return returnValue;
        }
        finally
        {
            if( runAsRole != null )
            {
                SecurityAssociation.popRunAsRole();
            }
        }
    }
    public Object invoke(MethodInvocation mi) throws Exception
    {
        // Authenticate the subject and apply any declarative security checks
        checkSecurityAssociation(mi, false);
        /* If a run-as role was specified, push it so that any calls made
         by this bean will have the runAsRole available for declarative
         security checks.
        */
        if( runAsRole != null )
        {
            SecurityAssociation.pushRunAsRole(runAsRole);
        }
        try
        {
            Object returnValue = getNext().invoke(mi);
            return returnValue;
        }
        finally
        {
            if( runAsRole != null )
            {
                SecurityAssociation.popRunAsRole();
            }
        }
    }

    /** The EJB 2.0 declarative security algorithm:
     1. Authenticate the caller using the principal and credentials in the MethodInfocation
     2. Validate access to the method by checking the principal's roles against
        those required to access the method.
     */
    private void checkSecurityAssociation(MethodInvocation mi, boolean home)
        throws Exception
    {
        Principal principal = mi.getPrincipal();
        Object credential = mi.getCredential();
        // If there is not a security manager then there is no authentication required
        if (securityManager == null)
        {
            // Allow for the progatation of caller info to other beans
            SecurityAssociation.setPrincipal( principal );
            SecurityAssociation.setCredential( credential );
            return;
        }
        if (realmMapping == null)
        {
            throw new RemoteException("checkSecurityAssociation", new SecurityException("Role mapping manager has not been set"));
        }

        // Check the security info from the method invocation
        if( securityManager.isValid(principal, credential) == false )
        {
            String msg = "Authentication exception, principal="+principal;
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new RemoteException("checkSecurityAssociation", e);
        }
        else
        {
            SecurityAssociation.setPrincipal( principal );
            SecurityAssociation.setCredential( credential );
        }

        Set methodRoles = container.getMethodPermissions(mi.getMethod(), home);
        if( methodRoles == null )
        {
            String method = mi.getMethod().getName();
            String msg = "No method permissions assigned to method="+method;
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new RemoteException("checkSecurityAssociation", e);
        }

        /* See if there is a runAs role associated with this thread. If there
            is, this is the security role against which the assigned method
            permissions must be checked.
        */
        Principal threadRunAsRole = SecurityAssociation.peekRunAsRole();
        if( threadRunAsRole != null )
        {
            // Check the runAs role
            if( methodRoles.contains(threadRunAsRole) == false &&
               methodRoles.contains(AnybodyPrincipal.ANYBODY_PRINCIPAL) == false )

            {
                String method = mi.getMethod().getName();
                String msg = "Insufficient method permissions, runAsRole="+threadRunAsRole
                    + ", method="+method+", requiredRoles="+methodRoles;
                log.error(msg);
                SecurityException e = new SecurityException(msg);
                throw new RemoteException("checkSecurityAssociation", e);
            }
        }
        /* If the method has no assigned roles or the user does not have at
           least one of the roles then access is denied.
        */
        else if( realmMapping.doesUserHaveRole(principal, methodRoles) == false )
        {
            String method = mi.getMethod().getName();
            Set userRoles = realmMapping.getUserRoles(principal);
            String msg = "Insufficient method permissions, principal="+principal
                + ", method="+method+", requiredRoles="+methodRoles+", principalRoles="+userRoles;
            log.error(msg);
            SecurityException e = new SecurityException(msg);
            throw new RemoteException("checkSecurityAssociation", e);
        }
   }

  // Monitorable implementation ------------------------------------
  public void sample(Object s)
  {
    // Just here to because Monitorable request it but will be removed soon
  }
  public Map retrieveStatistic()
  {
    return null;
  }
  public void resetStatistic()
  {
  }
}
