/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.security.plugins.samples;

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
import org.jboss.security.plugins.AbstractServerLoginModule;


/**
 * JaasServerLoginModule
 * written by: Edward Kenworthy 12th Dec 2000
 *
 * An example of a realistic ServerLoginModule that can be used when using JAAS
 * security with jBoss. I took SimpleServerLoginModule, written by Oleg Nitz and extended
 * the functionality.
 *
 * It uses two properties files:
 *        users.properties, which holds users (key) and their password (value).
 *        roles.properties which holds users (key) and a list of their roles as csv (value).
 *
 * Obviously using properties files means it will struggle with very large numbers of users and
 * also as it reads the properties file in at initialisation it will be insensitive to subsequent
 * password changes. It does have the advantage of being realistic in its functionality.
 *
 * The other major change I have made is to pull out an abstract class (AbstractServerLoginModule)
 * so that if you want to implement a more scalable way of looking up users and passwords and roles then you can
 * do so without having to start from scratch.
 *
 * @author <a href="edward.kenworthy@crispgroup.co.uk">Edward Kenworthy</a>
 */
public class JaasServerLoginModule extends AbstractServerLoginModule
{
  // users+passwords, users+roles
    private Properties _users;   // You might think these should be static. The only problem with
    private Properties _roles;   // static attributes is they are shared across the VM. So I chose safety
                                 // over performance.

    /**
     * Initialize this LoginModule.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options)
    {
        super.initialize(subject, callbackHandler, sharedState, options);
        try
        {
// Load the properties file that contains the list of users and passwords
          LoadUsers();
          LoadRoles();
        }
        catch (Exception e)
        {
          System.out.print("[JAASSecurity] PANIC! Couldn't load users/passwords/role files.\n");
          e.printStackTrace();
// Note that although this exception isn't passed on, _users or _roles will be null
// so that any call to login will throw a LoginException.
        }
    }

    /**
     * Method to authenticate a Subject (phase 1).
     *
     * Most of the changes from the original SimpleServerLoginModule
     * are made in this method. They are:
     * users and passwords read from users.properties file
     * users and roles read from roles.properties file
     *
     * I've also removed the notion of a guest login. If you want to provide 'guest'
     * access to your beans then simply disable security on them.
     *
     */
    public boolean login() throws LoginException
    {
        if (_users == null || _roles == null)
        {
          throw new LoginException("Missing _users or _roles properties file.");
        }

        return super.login();
    }

    // Polymorphic, used by the abstract base class.
    protected Enumeration getUsersRoles()
    {
      String roles = _roles.getProperty(getUsername());
      return (roles == null ? null : new StringTokenizer(roles, ","));
    }
    protected String getUsersPassword()
    {
        return _users.getProperty(getUsername(), null);
    }

// utility methods
    private void LoadUsers() throws IOException
    {
      _users = LoadProperties("users.properties");
    }

    private void LoadRoles() throws IOException
    {
      _roles = LoadProperties("roles.properties");
    }

    /**
    * Loads the given properties file and returns a Properties object containing the
    * key,value pairs in that file.
    * The properties files should be in the class path.
    */
    private Properties LoadProperties(String propertiesName) throws IOException
    {
      Properties bundle = null;
      InputStream is =Thread.currentThread().getContextClassLoader().getResource(propertiesName).openStream();

      if (null != is)
      {
         bundle = new Properties();
         bundle.load(is);
      }
      else
      {
         throw new IOException("Properties file " + propertiesName + " not found");
      }
      return bundle;
    }
}




