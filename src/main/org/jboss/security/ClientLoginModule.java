/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
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
import org.jboss.system.SecurityAssociation;
import org.jboss.system.SimplePrincipal;


public class ClientLoginModule implements LoginModule {
    private Subject _subject;
    private CallbackHandler _callbackHandler;

    // username and password
    private String _username;
    private char[] _password;

    /**
     * Initialize this LoginModule.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        _subject = subject;
        _callbackHandler = callbackHandler;
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
            _callbackHandler.handle(callbacks);
            _username = ((NameCallback)callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            if (tmpPassword != null) {
                _password = new char[tmpPassword.length];
                System.arraycopy(tmpPassword, 0, _password, 0, tmpPassword.length);
                ((PasswordCallback)callbacks[1]).clearPassword();
            }
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
        SecurityAssociation.setPrincipal(new SimplePrincipal(_username));
        SecurityAssociation.setCredential(_password);
        return true;
    }    
          
    /**
     * Method to abort the authentication process (phase 2).
     */
    public boolean abort() throws LoginException {
        _username = null;
        if (_password != null) {
            for (int i = 0; i < _password.length; i++)
            _password[i] = ' ';
            _password = null;
        }
        return true;
    }

    public boolean logout() throws LoginException {
        return true;
    }
}
