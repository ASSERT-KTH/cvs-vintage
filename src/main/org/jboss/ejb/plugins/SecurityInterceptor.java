/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.EJBContext;
import javax.ejb.EntityContext;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.security.auth.Subject;

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.MethodInvocation;

import org.jboss.logging.Logger;

import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityProxy;
import org.jboss.security.SecurityProxyFactory;

/** The SecurityInterceptor is where the EJB declarative security model
is enforced. It is also the layer where user's can introduce custom
security via the SecurityProxy delegation model.

@author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
@author Scott_Stark@displayscape.com
@version $Revision: 1.15 $
*/
public class SecurityInterceptor extends AbstractInterceptor
{
    /** The JNDI name of the SecurityProxyFactory used to wrap security
        proxy objects that do not implement the SecurityProxy interface
     */
    public final String SECURITY_PROXY_FACTORY_NAME = "java:/SecurityProxyFactory";

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

    /**
     * @supplierCardinality 0..1
     * @clientCardinality 1 
     * @supplierQualifier custom security
     */
    protected SecurityProxy securityProxy;

    public SecurityInterceptor()
    {
    }

    public void setContainer(Container container)
    {
        this.container = container;
        securityManager = container.getSecurityManager();
        realmMapping = container.getRealmMapping();
        Object secProxy = container.getSecurityProxy();
        if( secProxy != null )
        {
            /* If this is not a SecurityProxy instance then use the default
                SecurityProxy implementation
            */
            if( (secProxy instanceof SecurityProxy) == false )
            {
                try
                {
                    // Get default SecurityProxyFactory from JNDI at
                    InitialContext iniCtx = new InitialContext();
                    SecurityProxyFactory proxyFactory = (SecurityProxyFactory) iniCtx.lookup(SECURITY_PROXY_FACTORY_NAME);
                    securityProxy = proxyFactory.create(secProxy);
                }
                catch(Exception e)
                {
                    System.out.println("Failed to initialze DefaultSecurityProxy");
                    e.printStackTrace();
                }
            }
            else
            {
                securityProxy = (SecurityProxy) secProxy;
            }

            // Initialize the securityProxy
            try
            {
                ContainerInvokerContainer ic = (ContainerInvokerContainer) container;
                Class beanHome = ic.getHomeClass();
                Class beanRemote = ic.getRemoteClass();
                securityProxy.init(beanHome, beanRemote, securityManager);
            }
            catch(Exception e)
            {
                System.out.println("Failed to initialze SecurityProxy");
                e.printStackTrace();
            }
            System.out.println("Initialized SecurityProxy="+securityProxy);
        }
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
        // Apply any custom security checks
        if( securityProxy != null )
        {
            EJBContext ctx = mi.getEnterpriseContext().getEJBContext();
            Object[] args = mi.getArguments();
            securityProxy.setEJBContext(ctx);
            try
            {
                securityProxy.invokeHome(mi.getMethod(), args);
            }
            catch(SecurityException e)
            {
                Principal principal = mi.getPrincipal();
                String msg = "SecurityProxy.invokeHome exception, principal="+principal;
                Logger.error(msg);
                SecurityException se = new SecurityException(msg);
                throw new RemoteException("SecurityProxy.invokeHome failure", se);
            }
        }
        return getNext().invokeHome(mi);
    }
    public Object invoke(MethodInvocation mi) throws Exception
    {
        // Authenticate the subject and apply any declarative security checks
        checkSecurityAssociation(mi, false);
        // Apply any custom security checks
        if( securityProxy != null )
        {
            Object bean = mi.getEnterpriseContext().getInstance();
            EJBContext ctx = mi.getEnterpriseContext().getEJBContext();
            Object[] args = mi.getArguments();
            securityProxy.setEJBContext(ctx);
            try
            {
                securityProxy.invoke(mi.getMethod(), args, bean);
            }
            catch(SecurityException e)
            {
                Principal principal = mi.getPrincipal();
                String msg = "SecurityProxy.invoke exception, principal="+principal;
                Logger.error(msg);
                SecurityException se = new SecurityException(msg);
                throw new RemoteException("SecurityProxy.invoke failure", se);
            }
        }
        return getNext().invoke(mi);
    }

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
        if( principal == null || securityManager.isValid(principal, credential) == false )
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
