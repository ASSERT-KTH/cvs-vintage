/*
 * $Header: /tmp/cvs-vintage/struts/src/share/org/apache/struts/util/Attic/GenericDataSource.java,v 1.3 2001/01/08 23:53:10 husted Exp $
 * $Revision: 1.3 $
 * $Date: 2001/01/08 23:53:10 $
 *
 * ====================================================================
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
 */


package org.apache.struts.util;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import javax.sql.DataSource;


/**
 * <p>Generic data source implementation of the <code>DataSource</code>
 * interface.  <b>WARNING</b> - This implementation does not know how to
 * provide connections with different username/password combinations.  It
 * always returns connections based on the username and password configured
 * with <code>setUser()</code> and <code>setPassword()</code>,
 * respectively. Calling this version of the implementation using the
 * getConnection(username,password) signature will throw an exception.</p>
 *
 * <p>The following properties are supported by the standard
 * <code>GenericDataSource</code> implementation:</p>
 * <table>
 * <tr>
 *   <th width="15%">Property</th>
 *   <th width="85%">Description</th>
 * </tr>
 * <tr>
 *   <td align="center">autoCommit</td>
 *   <td>Set to <code>true</code> if you want the connections returned to you
 *       by calling <code>getConnection()</code> to be configured in
 *       "auto-commit after every statement" mode.  The default value is
 *       <code>true</code>, to conform to JDBC standard conventions.</td>
 * </tr>
 * <tr>
 *   <td align="center">description</td>
 *   <td>A short textual description of this data source.  This property is
 *       required by the <code>javax.sql.DataSource</code> interface, but is
 *       not used within this implementation.</td>
 * </tr>
 * <tr>
 *   <td align="center">driverClass</td>
 *   <td>The fully qualified class name of the JDBC driver to be utilized for
 *       the connections created by this data source.  Consult the
 *       documentation for your JDBC driver to identify the value to be
 *       configured for this property.</td>
 * </tr>
 * <tr>
 *   <td align="center">maxCount</td>
 *   <td>The maximum number of JDBC connections that will be created by this
 *       data source.  This value must be greater than or equal to the value
 *       specified for the <code>minCount</count> property.</td>
 * </tr>
 * <tr>
 *   <td align="center">minCount</td>
 *   <td>The minimum number of JDBC connections to establish when this data
 *       source is first opened.  This value must be less than or equal to the
 *       value specified for the <code>maxCount</code> property.</td>
 * </tr>
 * <tr>
 *   <td align="center">password</td>
 *   <td>The database password used to establish the connections created by
 *       this connection pool, in conjunction with the username specified in
 *       the <code>user</code> property.</td>
 * </tr>
 * <tr>
 *   <td align="center">readOnly</td>
 *   <td>Set to <code>true</code> if you want the connections returned to you
 *       by calling <code>getConnection()</code> to be configured for read only
 *       operations.  This can result in more efficient database access,
 *       because the database will know it does not need to retain undo logs
 *       for rolling back the transaction.  The default value is
 *       <code>false</code>.</td>
 * </tr>
 * <tr>
 *   <td align="center">url</td>
 *   <td>The connection URL to be passed to our JDBC driver when establishing
 *       a new connection.  The value specified typically starts with
 *       <code>jdbc:</code>, and includes a reference to the host (and,
 *       optionally, the port number) at which the database server is listening
 *       for connections, plus the name of the database to be opened.  Consult
 *       the documentation for your JDBC driver to identify the value to be
 *       configured for this property.</td>
 * </tr>
 * <tr>
 *   <td align="center">user</td>
 *   <td>The database username used to establish the connections created by
 *       this connection pool, in conjunction with the password specified in
 *       the <code>password</code> property.</td>
 * </tr>
 * </table>
 *
 * <p>In addition, you can add to the set of <code>Properties</code> passed to
 * the JDBC driver by calling <code>addProperty()</code>.</p>
 *
 * @author Craig R. McClanahan
 * @author Ted Husted
 * @version $Revision: 1.3 $ $Date: 2001/01/08 23:53:10 $
 */

public class GenericDataSource implements DataSource {


    // ----------------------------------------------------- Instance Constants


    private static final String SQLEXCEPTION_GETCONNECTION =
     "getConnection(String username, String password)  Method not supported. Use getConnection() instead.";



    // ----------------------------------------------------- Instance Variables


    /**
     * Has this data source been closed?
     */
    protected boolean closed = false;


    /**
     * The list of Connections (wrapped in our associated wrapper class) that
     * have been created but are not currently in use.
     */
    protected LinkedList connections = new LinkedList();


    /**
     * The JDBC driver that we use as a connection factory.
     */
    protected Driver driver = null;


    /**
     * The login timeout for this data source.
     */
    protected int loginTimeout = 0;


    /**
     * The log writer for this data source.
     */
    protected PrintWriter logWriter = null;


    /**
     * The connection properties for use in establishing connections.
     */
    protected Properties properties = new Properties();



    // ------------------------------------------------------------- Properties


    /**
     * Add a generic property to the list of connection properties to be used.
     *
     * @param name Name of the generic property
     * @param value Corresponding generic property value
     */
    public void addProperty(String name, String value) {
        properties.put(name, value);
    }


    /**
     * The number of connections that have been created by this data source.
     */
    protected int activeCount = 0;

    public int getActiveCount() {
        return (this.activeCount);
    }


    /**
     * The default auto-commit state for newly created connections.
     */
    protected boolean autoCommit = true;

    public boolean getAutoCommit() {
        return (this.autoCommit);
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }


    /**
     * The description of this data source.
     */
    protected String description = null;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * The Java class name of the JDBC driver to use.
     */
    protected String driverClass = null;

    public String getDriverClass() {
        return (this.driverClass);
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }


    /**
     * The maximum number of connections to be created.
     */
    protected int maxCount = 2;

    public int getMaxCount() {
        return (this.maxCount);
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }


    /**
     * The minimum number of connections to be created.
     */
    protected int minCount = 1;

    public int getMinCount() {
        return (this.minCount);
    }

    public void setMinCount(int minCount) {
        this.minCount = minCount;
    }


    /**
     * The database password for use in establishing a connection.
     */
    protected String password = null;

    public String getPassword() {
        return (this.password);
    }

    public void setPassword(String password) {
        this.password = password;
        addProperty("password", this.password);
    }


    /**
     * The default read-only state for newly created connections.
     */
    protected boolean readOnly = false;

    public boolean getReadOnly() {
        return (this.readOnly);
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }


    /**
     * The JDBC URL for the database connection to be opened.
     */
    protected String url = null;

    public String getUrl() {
        return (this.url);
    }

    public void setUrl(String url) {
        this.url = url;
    }


    /**
     * The number of connections created by this data source that are
     * currently in use.
     */
    protected int useCount = 0;

    public int getUseCount() {
        return (this.useCount);
    }


    /**
     * The database username for use in establishing a connection.
     */
    protected String user = null;

    public String getUser() {
        return (this.user);
    }

    public void setUser(String user) {
        this.user = user;
        addProperty("user", this.user);
    }


    // ----------------------------------------------------- DataSource Methods


    /**
     * Attempt to establish a database connection.
     *
     * @exception SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {

        int seconds = 0;

        // Validate the opened status of this data source
        if (closed)
            throw new SQLException("getConnection:  Data source is closed");
        if (driver == null)
            open();

        while (true) {

            // Have we timed out yet?
            if ((loginTimeout > 0) && (seconds >= loginTimeout))
                break;

            // Return an existing connection from the pool if there is one
            synchronized (connections) {
                if (!connections.isEmpty()) {
                    useCount++;
                    GenericConnection connection = (GenericConnection) connections.removeFirst();
                    // unclose the connection's wrapper
                    connection.setClosed(false);
                    return(connection);
                    // return ((Connection) connections.removeFirst()); DEBUG
                }
            }

            // Create a new connection if we are not yet at the maximum
            if (activeCount < maxCount) {
                Connection conn = createConnection();
                useCount++;
                return (conn);
            }

            // Wait for an existing connection to be returned
            try {
                Thread.sleep(1000);
                seconds++;
            } catch (InterruptedException e) {
                ;
            }

        }

        // We have timed out awaiting an available connection
        throw new SQLException
            ("getConnection: Timeout awaiting connection");

    }


    /**
     * Attempt to establish a database connection.  <b>WARNING</b> - The
     * specified username and password are ignored by this implementation.
     *
     * @param username Database username for this connection
     * @param password Database password for this connection
     *
     * @exception SQLException if a database access error occurs
     */



    public Connection getConnection(String username, String password)
        throws SQLException {

        throw new SQLException(SQLEXCEPTION_GETCONNECTION); // Not implemented

    }


    /**
     * Return the login timeout for this data source.
     *
     * @exception SQLException if a database access error occurs
     */
    public int getLoginTimeout() throws SQLException {

        return (this.loginTimeout);

    }



    /**
     * Return the log writer for this data source.
     *
     * @exception SQLException if a database access error occurs
     */
    public PrintWriter getLogWriter() throws SQLException {

        return (this.logWriter);

    }


    /**
     * Set the login timeout for this data source.
     *
     * @param loginTimeout The new login timeout
     *
     * @exception SQLException if a database access error occurs
     */
    public void setLoginTimeout(int loginTimeout) throws SQLException {

        this.loginTimeout = loginTimeout;

    }


    /**
     * Set the log writer for this data source.
     *
     * @param logWriter The new log writer
     *
     * @exception SQLException if a database access error occurs
     */
    public void setLogWriter(PrintWriter logWriter) throws SQLException {

        this.logWriter = logWriter;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Close all connections that have been created by this data source.
     *
     * @exception SQLException if a database access error occurs
     */
    public void close() throws SQLException {

        if (closed)
            throw new SQLException("close:  Data Source already closed");

        // Shut down all active connections
        while (activeCount > 0) {
            GenericConnection conn = (GenericConnection) getConnection();
            conn.getConnection().close();
            activeCount--;
        }

        // Mark this data source as having been closed and release our driver
        closed = true;
        driver = null;

    }


    /**
     * Open the initial connections that are appropriate for this data source.
     *
     * @exception SQLException if a database access error occurs
     */
    public void open() throws SQLException {

        // Have we already been opened?
        if (driver != null)
            return;

        // Instantiate our database driver
        try {
            Class clazz = Class.forName(driverClass);
            driver = (Driver) clazz.newInstance();
        } catch (Throwable t) {
            throw new SQLException("createConnection: " + t);
        }

        // Create the initial minimum number of required connections
        synchronized (connections) {
            for (int i = 0; i < minCount; i++) {
                connections.addLast(createConnection());
            }
        }

        closed = false;

    }


    /**
     * Return a string representation of this component.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("GenericDataSource[");
        sb.append("activeCount=");
        sb.append(activeCount);
        sb.append(", autoCommit=");
        sb.append(autoCommit);
        sb.append(", closed=");
        sb.append(closed);
        if (description != null) {
            sb.append(", description=");
            sb.append(description);
        }
        sb.append(", driverClass=");
        sb.append(driverClass);
        sb.append(", loginTimeout=");
        sb.append(loginTimeout);
        sb.append(", maxCount=");
        sb.append(maxCount);
        sb.append(", minCount=");
        sb.append(minCount);
        sb.append(", password=");
        sb.append(password);
        sb.append(", readOnly=");
        sb.append(readOnly);
        sb.append(", url=");
        sb.append(url);
        sb.append(", useCount=");
        sb.append(useCount);
        sb.append(", user=");
        sb.append(user);
        sb.append("]");
        return (sb.toString());

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Create, configure, and return a new JDBC Connection that has been
     * wrapped in our corresponding wrapper.
     *
     * @exception SQLException if a database access error occurs
     */
    protected synchronized Connection createConnection() throws SQLException {

        Connection conn = driver.connect(url, properties);
        activeCount++;
        return (new GenericConnection(this, conn, autoCommit, readOnly));

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Return this connection to the available connection pool.
     *
     * @param conn The connection being returned
     */
    void returnConnection(GenericConnection conn) {

        synchronized (connections) {
            connections.addLast(conn);
            useCount--;
        }

    }


}
