/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.security;


import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

/** A simple implementation of LoginModule for use by JBoss clients for
the establishment of the caller identity and credentials. This simply sets
the SecurityAssociation principal to the value of the NameCallback
filled in by the CallbackHandler, and the SecurityAssociation credential
to the value of the PasswordCallback filled in by the CallbackHandler.

It has one option: multi-threaded=[true|false]
When the multi-threaded option is set to true, the SecurityAssociation.setServer()
so that each login thread has its own principal and credential storage.

@author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
@author Scott_Stark@displayscape.com
*/
public class ClientLoginModule implements LoginModule {
    private CallbackHandler _callbackHandler;

    /**
     * Initialize this LoginModule.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        _callbackHandler = callbackHandler;
        // Check for multi-threaded option
        String mt = (String) options.get("multi-threaded");
        if( mt != null && Boolean.valueOf(mt).booleanValue() == true )
        {   /* Turn on the server mode which uses thread local storage for
                the principal information.
            */
            SecurityAssociation.setServer();
        }
    }

    /**
     * Method to authenticate a Subject (phase 1).
     */
    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];
        
        // prompt for a username and password
        if (_callbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available " +
                "to garner authentication information from the user");

        callbacks[0] = new NameCallback("User name: ", "guest");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            String username;
            char[] password = null;
            char[] tmpPassword;

            _callbackHandler.handle(callbacks);
            username = ((NameCallback)callbacks[0]).getName();
            SecurityAssociation.setPrincipal(new SimplePrincipal(username));
            tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            if (tmpPassword != null) {
                password = new char[tmpPassword.length];
                System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
                ((PasswordCallback)callbacks[1]).clearPassword();
            }
            SecurityAssociation.setCredential(password);
        } catch (java.io.IOException ioe) {
            throw new LoginException(ioe.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                    " not available to garner authentication information " +
                    "from the user");
        }
        return true;
    }
          
    /**
     * Method to commit the authentication process (phase 2).
     */
    public boolean commit() throws LoginException {
        return true;
    }    
          
    /**
     * Method to abort the authentication process (phase 2).
     */
    public boolean abort() throws LoginException {
        SecurityAssociation.setPrincipal(null);
        SecurityAssociation.setCredential(null);
        return true;
    }

    public boolean logout() throws LoginException {
        SecurityAssociation.setPrincipal(null);
        SecurityAssociation.setCredential(null);
        return true;
    }
}
