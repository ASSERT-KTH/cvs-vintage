/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security.plugins;

import java.util.*;
import java.io.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;

/**
 * AbstractServerLoginModule
 * written by: Edward Kenworthy 12th Dec 2000
 *
 * This class implements the common functionality required for a JAAS ServerLoginModule.
 * To implement your own implementation you need to add just the user/password/roles lookup
 * functionality.
 *
 * It attaches the roles to the subject as public credentials.
 * According to the JAAS spec it requires privileged access to do this, and as I don't
 * explicilty code that privilege I assume I must have it by virtue of being a ServerLoginModule.
 *
 * As a minimum you must implement:
 *
 *    protected String getUsersRoles(); // returns a csv list of the users roles
 *    protected String getUsersPassword(); // returns the users password
 *
 * You may also wish to override
 *
 *    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
 *
 * In which case the first line of your initialize() method should be super.initialize(subject, callbackHandler, sharedState, options);
 *
 * You may also wish to override
 *
 *    public boolean login() throws LoginException
 *
 * In which case the last line of your login() method should be return super.login();
 *
 * @author <a href="edward.kenworthy@crispgroup.co.uk">Edward Kenworthy</a>
 */
public abstract class AbstractServerLoginModule implements LoginModule
{
    private Subject _subject;
    private CallbackHandler _callbackHandler;

    // username and password
    private String _username;
    protected String getUsername() {return _username;}
    private char[] _password;

    abstract protected Enumeration getUsersRoles();
    abstract protected String getUsersPassword();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
    {
        _subject = subject;
        _callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException
    {
        Callback[] callbacks = new Callback[2];
        // prompt for a username and password
        if (_callbackHandler == null)
        {
            throw new LoginException("Error: no CallbackHandler available " +
                                 "to garner authentication information from the user");
        }
        callbacks[0] = new NameCallback("User name: ", "guest");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try
        {
            _callbackHandler.handle(callbacks);
            _username = ((NameCallback)callbacks[0]).getName();
            char[] tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            if (tmpPassword != null)
            {
                _password = new char[tmpPassword.length];
                System.arraycopy(tmpPassword, 0, _password, 0, tmpPassword.length);
                ((PasswordCallback)callbacks[1]).clearPassword();
            }
        }
        catch (java.io.IOException ioe)
        {
            throw new LoginException(ioe.toString());
        }
        catch (UnsupportedCallbackException uce)
        {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                    " not available to garner authentication information " +
                    "from the user");
        }
        String userPassword = getUsersPassword();
        if (_password == null || userPassword == null || !(new String(_password)).equals(userPassword))
        {
            System.out.print("[JAASSecurity] Bad password.\n");
            throw new FailedLoginException("Password Incorrect/Password Required");
        }
        System.out.print("[JAASSecurity] User '" + _username + "' authenticated.\n");
        return true;
    }

    /**
     * Method to commit the authentication process (phase 2).
     */
    public boolean commit() throws LoginException
    {
        Set roles = _subject.getPublicCredentials();
        Enumeration roleList = getUsersRoles();
        if (roleList != null) {
            while (roleList.hasMoreElements()) {
                roles.add(roleList.nextElement());
            }
        }
        return true;
    }

    /**
     * Method to abort the authentication process (phase 2).
     */
    public boolean abort() throws LoginException
    {
        _username = null;
        if (_password != null)
        {
            for (int i = 0; i < _password.length; i++)
            _password[i] = ' ';
            _password = null;
        }
        return true;
    }

    public boolean logout() throws LoginException
    {
        return true;
    }
}



