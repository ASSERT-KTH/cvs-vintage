/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.factories;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.jboss.minerva.pools.*;
import org.jboss.minerva.jdbc.*;
import org.jboss.logging.Logger;

/**
 * Object factory that creates java.sql.Connections.  This is meant for use
 * outside a J2EE/JTA environment - servlets alone, client/server, etc.  If
 * you're interested in creating transactional-aware connections, see
 * XAConnectionFactory, which complies with the JDBC 2.0 standard extension.
 * @see org.jboss.minerva.factories.XAConnectionFactory
 * @version $Revision: 1.4 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class JDBCConnectionFactory extends PoolObjectFactory {
    private String url;
    private Properties props;
    private String userName;
    private String password;
    private PrintWriter log;

    /**
     * Creates a new factory.  You must configure it with JDBC properties
     * before you can use it.
     */
    public JDBCConnectionFactory() {
    }

    /**
     * Sets the JDBC URL used to create new connections.
     */
    public void setConnectURL(String url) {this.url = url;}

    /**
     * Gets the JDBC URL used to create new connections.
     */
    public String getConnectURL() {return url;}

    /**
     * Sets the JDBC Propeties used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setConnectProperties(Properties props) {this.props = props;}

    /**
     * Gets the JDBC Properties used to create new connections.
     */
    public Properties getConnectProperties() {return props;}

    /**
     * Sets the JDBC user name used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setUser(String userName) {this.userName = userName;}

    /**
     * Gets the JDBC user name used to create new connections.
     */
    public String getUser() {return userName;}

    /**
     * Sets the JDBC password used to create new connections.
     * This is optional, and will only be used if present.
     */
    public void setPassword(String password) {this.password = password;}

    /**
     * Gets the JDBC password used to create new connections.
     */
    public String getPassword() {return password;}

    /**
     * Validates that connection properties were set (at least a URL).
     */
    public void poolStarted(ObjectPool pool, PrintWriter log) {
        super.poolStarted(pool, log);
        if(url == null)
            throw new IllegalStateException("Must specify JDBC connection URL to "+getClass().getName());
    }

    /**
     * Creates a new JDBC Connection.
     */
    public Object createObject() {
        try {
            if(userName != null && userName.length() > 0)
                return DriverManager.getConnection(url, userName, password);
            else if(props != null)
                return DriverManager.getConnection(url, props);
            else
                return DriverManager.getConnection(url);
        } catch(SQLException e) {
            Logger.exception(e);
        }
        return null;
    }

    /**
     * Wraps the connection with a ConnectionInPool.
     * @see org.jboss.minerva.jdbc.ConnectionInPool
     */
    public Object prepareObject(Object pooledObject) {
        Connection con = (Connection)pooledObject;
        ConnectionInPool wrapper = new ConnectionInPool(con);
        return wrapper;
    }

    /**
     * Returns the original connection from a ConnectionInPool.
     * @see org.jboss.minerva.jdbc.ConnectionInPool
     */
    public Object translateObject(Object clientObject) {
        return ((ConnectionInPool)clientObject).getUnderlyingConnection();
    }

    /**
     * Closes all outstanding work for the connection, rolls it back, and
     * returns the underlying connection to the pool.
     */
    public Object returnObject(Object clientObject) {
        ConnectionInPool wrapper = (ConnectionInPool)clientObject;
        Connection con = wrapper.getUnderlyingConnection();
        try {
            wrapper.reset();
        } catch(SQLException e) {}
        return con;
    }

    /**
     * Closes a connection.
     */
    public void deleteObject(Object pooledObject) {
        Connection con = (Connection)pooledObject;
        try {
            con.rollback();
        } catch(SQLException e) {}

        // Removed all the cached PreparedStatements for this Connection
        Iterator it = ((Map)PreparedStatementInPool.preparedStatementCache.clone()).keySet().iterator();
        PreparedStatement ps;
        while(it.hasNext()) {
            PSCacheKey key = (PSCacheKey)it.next();
            if(key.con.equals(con)) {
                ps = (PreparedStatement)PreparedStatementInPool.preparedStatementCache.remove(key);
                if(ps != null) // Sanity check
                    try {ps.close();} catch(SQLException e) {}
            }
        }

        try {
            con.close();
        } catch(SQLException e) {}
    }
}