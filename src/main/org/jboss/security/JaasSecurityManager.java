/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.security;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;

import java.security.Principal;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import com.sun.security.auth.login.ConfigFile;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.transaction.TransactionManager;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

import org.jboss.system.EJBSecurityManager;
import org.jboss.system.RealmMapping;

/**
 * The EJBSecurityManager is responsible for validating credentials
 * associated with principals. Right now it is a "demo" that just
 * ensures name == credential
 *      
 * @see EJBSecurityManager
 * @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 */
public class JaasSecurityManager
        implements EJBSecurityManager, RealmMapping, Serializable {
    
    /**
     * Security manager name.
     */
    private final String _smName;
    
    /**
     * Maps an original principal to authenticated client credential, aka password.
     * Should be a time or size limited cache for better scalability.
     * This is a master cache, when a principal is removed from this cache,
     * the related entries from all other caches should be removed too.
     */
    private final HashMap _passwords = new HashMap();

    /**
     * Maps original principal to principal for the bean.
     */
    private final HashMap _principals = new HashMap();
    
    /**
     * Maps original principal to Set of roles for the bean.
     */
    private final HashMap _roles = new HashMap();

    /**
     * @param smName The name of the security manager
     */
    public JaasSecurityManager(String smName) {
        _smName = smName;
    }
    
    public boolean isValid(Principal principal, Object credential) {
        boolean ok;
        char[] authenticated;

        authenticated = (char[]) _passwords.get(principal);
        if (authenticated == null) {
            return authenticate(_smName, principal, credential);
        } else  {
            if ((credential instanceof char[]) &&
                    Arrays.equals(authenticated, (char[]) credential)) {
                return true;
            } else {
                // the password may have changed - reauthenticate
                return authenticate(_smName, principal, credential);
            }
        }
    }
    
    public Principal getPrincipal(Principal principal) {
        Principal result;
        result = (Principal) _principals.get(principal);
        if (result == null) {
            if (authenticate(_smName, principal, null)) {
                result = (Principal) _principals.get(principal);
            }
        }
        return result;
    }

    public boolean doesUserHaveRole(Principal principal, Set roleNames)
    {
        Set roles;
        Iterator it;

        if (roleNames == null)
            return true;
        roles = (Set) _roles.get(principal);
        if (roles == null) {
            if (!authenticate(_smName, principal, null)) {
                return false;
            }
        } 
        it = roleNames.iterator();
        while (it.hasNext()) {
            if (roles.contains(it.next())) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * @param bean The bean name
     * @param userName The user name
     * @param password The password
     * @return false on failure, true on success.
     */
    private boolean authenticate(String beanName, Principal principal, Object credential) {
        LoginContext lc;
        Subject subj;
        final String userName = principal.getName();
        final char[] password = (char[]) credential;
        Iterator it;
        Principal beanPrincipal;

        try {
            lc = new LoginContext(beanName, new CallbackHandler() {
                public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                    for (int i = 0; i < callbacks.length; i++) {
                        if (callbacks[i] instanceof NameCallback) {
                            ((NameCallback) callbacks[i]).setName(userName);
                        } else if (callbacks[i] instanceof PasswordCallback) {
                            ((PasswordCallback) callbacks[i]).setPassword(password);
                        } else {
                            throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                        }
                    }
                }
            });
            lc.login();
            _passwords.put(principal, password);
            subj = lc.getSubject();
            beanPrincipal = principal;
            it = subj.getPrincipals().iterator();
            if (it.hasNext()) {
                beanPrincipal = (Principal) it.next();
            }
            _principals.put(principal, beanPrincipal);
            _roles.put(principal, subj.getPublicCredentials());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        } 
    }    
}

