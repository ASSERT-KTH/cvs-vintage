/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.security;


import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;

public class DatabaseServerLoginModule implements LoginModule {
    private String _db;
    private String _table;
    private String _nameCol;
    private String _pswCol;
    private Subject _subject;
    private CallbackHandler _callbackHandler;
    private String _username;

    /**
     * Initialize this LoginModule.
     */
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map sharedState, Map options) {
        _subject = subject;
        _callbackHandler = callbackHandler;
        _db = (String) options.get("db");
        _table = (String) options.get("table");
        _nameCol = (String) options.get("name");
        _pswCol = (String) options.get("password");
    }

    /**
     * Method to authenticate a Subject (phase 1).
     */
    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];
        char[] password;
        char[] tmpPassword;
        InitialContext initial;
        DataSource ds;
        Connection conn = null;
        PreparedStatement ps;
        ResultSet rs;
        Object psw;
        boolean ok;
      
        try {
            // prompt for a username and password
            if (_callbackHandler == null) {
                throw new LoginException("Error: no CallbackHandler available " +
                                         "to garner authentication information from the user");
            }

            callbacks[0] = new NameCallback("User name: ", "guest");
            callbacks[1] = new PasswordCallback("Password: ", false);
            _callbackHandler.handle(callbacks);
            _username = ((NameCallback)callbacks[0]).getName();
            tmpPassword = ((PasswordCallback)callbacks[1]).getPassword();
            if (tmpPassword == null) {
                password = null;
            } else {
                password = new char[tmpPassword.length];
                System.arraycopy(tmpPassword, 0, password, 0, tmpPassword.length);
                ((PasswordCallback)callbacks[1]).clearPassword();
            }

            // password authorization
            if (_pswCol != null) {
                initial = new InitialContext();
                ds = (DataSource) initial.lookup( _db );
                conn = ds.getConnection();
                ps = conn.prepareStatement("SELECT " + _pswCol + " FROM " + _table +
                                           " WHERE " + _nameCol + "=?");
                ps.setString(1, _username);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new FailedLoginException("Incorrect user name");
                }
                psw = rs.getObject(1);
                if (password == null || psw == null) {
                    ok = (password == psw);
                } else if (psw instanceof byte[]) {
                    byte[] bpsw;
                    int len;
                    char[] cpsw;
                    bpsw = (byte[]) psw;
                    // trim zero bytes
                    for (len = bpsw.length; len>0; len--) {
                        if (bpsw[len - 1] != 0) {
                            break;
                        }
                    }
                    cpsw = new char[len];
                    for (int i = 0; i < len; i++) {
                        cpsw[i] = (char) bpsw[i];
                    }
                    ok = Arrays.equals(password, cpsw);
                } else if (psw instanceof String) {
                    // trim spaces
                    ok = (new String(password)).equals(((String) psw).trim());
                } else {
                    throw new LoginException("Unsupported SQL type of password column");
                }
                if (!ok) {
                    throw new FailedLoginException("Incorrect password");
                }                        
            }
        } catch (NamingException ex) {
            throw new LoginException(ex.toString());
        } catch (java.io.IOException ex) {
            throw new LoginException(ex.toString());
        } catch (SQLException ex) {
            throw new LoginException(ex.toString());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException("Error: " + uce.getCallback().toString() +
                    " not available to garner authentication information " +
                    "from the user");
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex) {
                }
            }
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
        _username = null;
        return true;
    }

    public boolean logout() throws LoginException {
        return true;
    }
}
