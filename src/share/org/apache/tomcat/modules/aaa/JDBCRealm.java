/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/tomcat/modules/aaa/JDBCRealm.java,v 1.6 2001/04/10 09:00:59 nacho Exp $
 * $Revision: 1.6 $
 * $Date: 2001/04/10 09:00:59 $
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.tomcat.modules.aaa;

import org.apache.tomcat.core.*;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.aaa.*;
import java.security.*;
import java.security.Principal;
import java.util.Vector;
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

/**
 * Implmentation of <b>Realm</b> that works with any JDBC supported database.
 * See the JDBCRealm.howto for more details on how to set up the database and
 * for configuration options.
 *
 *
 * TODO: - Work on authentication with non-plaintext passwords
 *
 *
 * @author Craig R. McClanahan
 * @author Carson McDonald
 * @author Ignacio J. Ortega
 * @author Bip Thelin
 */
public final class JDBCRealm extends BaseInterceptor {
    int reqRolesNote;
    int userNote;
    int passwordNote;
    // ----------------------------------------------------- Instance Variables

    /** The connection to the database. */
    private Connection dbConnection = null;

    /** The PreparedStatement to use for authenticating users. */
    private PreparedStatement preparedAuthenticate = null;

    /** The PreparedStatement to use for identifying the roles for a specified user. */
    private PreparedStatement preparedRoles = null;

    /** The connection URL to use when trying to connect to the databse */
    private String connectionURL = null;

    /** The connection URL to use when trying to connect to the databse */
    private String connectionName = null;

    /** The connection URL to use when trying to connect to the databse */
    private String connectionPassword = null;

    /** The table that holds user data. */
    private String userTable = null;

    /** The column in the user table that holds the user's name */
    private String userNameCol = null;

    /** The column in the user table that holds the user's credintials */
    private String userCredCol = null;

    /** The table that holds the relation between user's and roles */
    private String userRoleTable = null;

    /** The column in the user role table that names a role */
    private String roleNameCol = null;

    /** The JDBC driver to use. */
    private String driverName = null;

    /** The string manager for this package. */
    private static StringManager sm = StringManager.getManager("org.apache.tomcat.resources");

    /** Has this component been started? */
    private boolean started = false;

    /** Has the JDBC connection been started? */
    private boolean JDBCstarted = false;

    /**
     * Digest algorithm used in passwords thit is same values accepted by MessageDigest  for algorithm
     * plus "No" ( no encode ) that is the default
     */
    private String digest = "No";

    boolean connectOnInit = false;
    // ------------------------------------------------------------- Properties

    /**
     * Set the JDBC driver that will be used.
     * @param driverName The driver name
     */
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    /**
     * Set the URL to use to connect to the database.
     * @param connectionURL The new connection URL
     */
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    /**
     * Set the name to use to connect to the database.
     * @param connectionName User name
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * Set the password to use to connect to the database.
     * @param connectionPassword User password
     */
    public void setConnectionPassword(String connectionPassword) {
        this.connectionPassword = connectionPassword;
    }

    /**
     * Set the table that holds user data.
     * @param userTable The table name
     */
    public void setUserTable(String userTable) {
        this.userTable = userTable;
    }

    /**
     * Set the column in the user table that holds the user's name
     * @param userNameCol The column name
     */
    public void setUserNameCol(String userNameCol) {
        this.userNameCol = userNameCol;
    }

    /**
     * Set the column in the user table that holds the user's credintials
     * @param userCredCol The column name
     */
    public void setUserCredCol(String userCredCol) {
        this.userCredCol = userCredCol;
    }

    /**
     * Set the table that holds the relation between user's and roles
     * @param userRoleTable The table name
     */
    public void setUserRoleTable(String userRoleTable) {
        this.userRoleTable = userRoleTable;
    }

    /**
     * Set the column in the user role table that names a role
     * @param roleNameCol The column name
     */
    public void setRoleNameCol(String roleNameCol) {
        this.roleNameCol = roleNameCol;
    }

    /**
     * Gets the digest algorithm  used for credentials in the database
     * could be the same that MessageDigest accepts vor algorithm and "No" that
     * is the Default
     * @return
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Sets the digest algorithm  used for credentials in the database
     * could be the same that MessageDigest accepts vor algorithm and "No"
     * that is the Default
     * @param algorithm the Encode type
     */
    public void setDigest(String algorithm) {
        digest = algorithm;
    }

    /**
     * When connectOnInit is true the JDBC connection is started at tomcat init
     * if false the connection is started the first times it is needed.
     * @param b
     */
    public void setConnectOnInit(boolean b) {
        connectOnInit = b;
    }

    /**
     * If there are any errors with the JDBC connection, executing
     * the query or anything we return false (don't authenticate). This event
     * is also logged.
     * If there is some SQL exception the connection is set to null.
     * This will allow a retry on the next auth attempt. This might not
     * be the best thing to do but it will keep tomcat from needing a restart
     * if the database goes down.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in authenticating this username
     */
    private synchronized boolean checkPassword(String username,String credentials) {
        try {
            if (!checkConnection())
                return false;
            // Create the authentication search prepared statement if necessary
            if (preparedAuthenticate == null) {
                String sql = "SELECT " + userCredCol
                    + " FROM " + userTable
                    + " WHERE " + userNameCol + " = ?";
                if (debug >= 1)
                    log("JDBCRealm.authenticate: " + sql);
                preparedAuthenticate = dbConnection.prepareStatement(sql);
            }
            // Perform the authentication search
            preparedAuthenticate.setString(1, username);
            ResultSet rs1 = preparedAuthenticate.executeQuery();
            if (rs1.next()) {
                String dbCredentials=rs1.getString(1).trim();
                if( digest.equals("") || digest.equalsIgnoreCase("No")){
                    if (credentials.equals(dbCredentials)) {
                        if (debug >= 2)
                            log(sm.getString("jdbcRealm.authenticateSuccess", username));
                        return true;
                    }
                } else {
                    if (digest(credentials,digest).equals(dbCredentials)) {
                        if (debug >= 2)
                            log(sm.getString("jdbcRealm.authenticateSuccess", username));
                        return true;
                    }
                }
            }
            rs1.close();
            if (debug >= 2)
                log(sm.getString("jdbcRealm.authenticateFailure", username));
            return false;
        } catch (SQLException ex) {
            // Log the problem for posterity
            log(sm.getString("jdbcRealm.checkPasswordSQLException", username), ex);
            // Clean up the JDBC objects so that they get recreated next time
            if (preparedAuthenticate != null) {
                try {
                    preparedAuthenticate.close();
                } catch (Throwable t) {
                    ;
                }
                preparedAuthenticate = null;
            }
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (Throwable t) {
                    ;
                }
                dbConnection = null;
            }
            // Return "not authenticated" for this request
            return false;
        }
    }

    private boolean checkConnection() {
        try {
            if ((dbConnection == null) || dbConnection.isClosed()) {
                Class.forName(driverName);
                if( JDBCstarted )
                        log(sm.getString("jdbcRealm.checkConnectionDBClosed"));
                if ((connectionName == null || connectionName.equals("")) ||
                    (connectionPassword == null || connectionPassword.equals(""))) {
                        dbConnection = DriverManager.getConnection(connectionURL);
                } else {
                    dbConnection = DriverManager.getConnection(connectionURL,
                        connectionName, connectionPassword);
                }
                JDBCstarted=true;
                if (dbConnection == null || dbConnection.isClosed()) {
                    log(sm.getString("jdbcRealm.checkConnectionDBReOpenFail"));
                    return false;
                }
            }
            return true;
        } catch (SQLException ex) {
            log(sm.getString("jdbcRealm.checkConnectionSQLException"), ex);
            return false;
        }
        catch (ClassNotFoundException ex) {
            throw new RuntimeException("JDBCRealm.checkConnection: " + ex);
        }
    }

/**
 * returns all the roles for a given user.
 *
 * @param username the user name
 * @return the roles array
 */
    public synchronized String[] getUserRoles(String username) {
        try {
            if (!checkConnection())
                return null;
            if (preparedRoles == null) {
                String sql = "SELECT " + roleNameCol + " FROM " + userRoleTable
                           + " WHERE " + userNameCol + " = ?";
                if (debug >= 1)
                    log("JDBCRealm.roles: " + sql);
                preparedRoles = dbConnection.prepareStatement(sql);
            }
            preparedRoles.clearParameters();
            preparedRoles.setString(1, username);
            ResultSet rs = preparedRoles.executeQuery();
            // Next we convert the resultset into a String[]
            Vector vrol = new Vector();
            while (rs.next()) {
                vrol.addElement(rs.getString(1).trim());
            }
            String[] res = new String[vrol.size() > 0 ? vrol.size() : 1];
            // no roles case
            if (vrol.size() == 0) {
                res[0] = "";
                return res;
            }
            for (int i = 0; i < vrol.size(); i++)
                res[i] = (String)vrol.elementAt(i);
            return res;
        }
        catch (SQLException ex) {
            // Set the connection to null.
            // Next time we will try to get a new connection.
            log(sm.getString("jdbcRealm.getUserRolesSQLException", username));
            if (preparedRoles != null) {
                try {
                    preparedRoles.close();
                } catch (Throwable t) {
                    ;
                }
                preparedRoles = null;
            }
            if (dbConnection != null) {
                try {
                    dbConnection.close();
                } catch (Throwable t) {
                    ;
                }
                dbConnection = null;
            }
        }
        return null;
    }

    // -------------------- Tomcat hooks --------------------
    public void contextInit(Context ctx) throws org.apache.tomcat.core.TomcatException {
        super.contextInit(ctx);
        init(ctx.getContextManager());
        // Validate and update our current component state
    }

    public void contextShutdown(Context ctx) throws org.apache.tomcat.core.TomcatException {
        shutdown();
    }

    public void shutdown() throws org.apache.tomcat.core.TomcatException {
        // Validate and update our current component state
        if (started) {
            started = false;
            try {
                if (dbConnection != null && !dbConnection.isClosed())
                    dbConnection.close();
            } catch (SQLException ex) {
                log("dbConnection.close Exception!!!", ex);
            }
        }
    }

    /** Authenticate hook implementation  */

    public int authenticate(Request req, Response response) {
        String user = (String)req.getNote(userNote);
        String password = (String)req.getNote(passwordNote);
        if (user == null) return DECLINED;
        if (checkPassword(user, password)) {
            if (debug > 0) log("Auth ok, user=" + user);
            Context ctx = req.getContext();
            if (ctx != null)
                req.setAuthType(ctx.getAuthMethod());
            if (user != null) {
                req.setRemoteUser(user);
		req.setUserPrincipal( new JdbcPrincipal( user ));
                String userRoles[] = getUserRoles(user);
                req.setUserRoles(userRoles);
                return OK;
            }
        }
        return DECLINED;
    }

    /**
     * Digest password using the algorithm especificied and
     * convert the result to a corresponding hex string.
     * If exception, the plain credentials string is returned
     * @param credentials Password or other credentials to use in authenticating this username
     * @param algorithm Algorithm used to do th digest
     */
    public final static String digest(String credentials, String algorithm) {
        try {
            // Obtain a new message digest with MD5 encryption
            MessageDigest md = (MessageDigest)MessageDigest.getInstance(algorithm).clone();
            // encode the credentials
            md.update(credentials.getBytes());
            // obtain the byte array from the digest
            byte[] dig = md.digest();
            // convert the byte array to hex string
            //            Base64 enc=new Base64();
            //            return new String(enc.encode(HexUtils.convert(dig).getBytes()));
            return HexUtils.convert(dig);
        } catch (Exception ex) {
            ex.printStackTrace();
            return credentials;
        }
    }

    /**
     * JDBCRealm can be used as a standalone tool for offline password digest
     * @param args
     */
    public static void main(String args[]) {
        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("-a")) {
                for (int i = 2; i < args.length; i++) {
                    System.out.print(args[i] + ":");
                    System.out.println(digest(args[i], args[1]));
                }
            }
        }
    }

     /** Called when the ContextManager is started */
    public void engineInit(ContextManager cm) throws TomcatException {
        super.engineInit(cm);
        init(cm);
    }

    void init(ContextManager cm) {
        if (!started) {
            started = true;
            // set-up a per/container note for maps
            try {
                // XXX make the name a "global" static - after everything is stable!
                reqRolesNote = cm.getNoteId(ContextManager.REQUEST_NOTE, "required.roles");
                userNote = cm.getNoteId(ContextManager.REQUEST_NOTE, "credentials.user");
                passwordNote = cm.getNoteId(ContextManager.REQUEST_NOTE, "credentials.password");
                if (connectOnInit && !checkConnection())
                        throw new RuntimeException("JDBCRealm cannot be started");
            }
            catch (TomcatException ex) {
                log("setting up note for " + cm, ex);
                throw new RuntimeException("Invalid state ");
            }
        }
    }

    public void engineShutdown(ContextManager cm) throws TomcatException {
        shutdown();
    }

    // Nothing - except cary on the class name information 
    public static class JdbcPrincipal extends SimplePrincipal {
	private String name;

	JdbcPrincipal(String name) {
	    super(name);
	}
    }
}

