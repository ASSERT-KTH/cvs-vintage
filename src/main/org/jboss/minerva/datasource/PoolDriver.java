/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.datasource;

import java.sql.*;
import java.util.Properties;
import javax.sql.*;
import org.jboss.logging.Logger;

/**
 * JDBC Driver to access pooled JDBC connections.  Supports both JDBC 1.0
 * connections and JDBC 2.0 transactional XAConnections.  You will get a
 * java.sql.Connection back in any case (in the case of XAConnections, the
 * transactional-ness is handled under the covers).  You must create the pools
 * ahead of time by creating and initializing the appropriate DataSource.
 * <TABLE BORDER="1">
 *   <TR><TH>Connection Type</TH><TH>URL Form</TH></TR>
 *   <TR><TD>Connection</TD><TD>jdbc:minerva:<I>PoolName</I></TD></TR>
 *   <TR><TD>XAConnection</TD><TD>jdbc:minervaxa:<I>PoolName</I></TD></TR>
 * </TABLE>
 * <P>Note that you must load this driver like normal in order to use it:<BR>
 * <CODE>Class.forName("org.minerva.datasource.PoolDriver");</CODE></P>
 * @see org.jboss.minerva.datasource.JDBCPoolDataSource
 * @see org.jboss.minerva.datasource.XAPoolDataSource
 * @version $Revision: 1.4 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class PoolDriver implements Driver {
    private final static String URL_START = "jdbc:minerva:";
    private final static PoolDriver instance;
    static {
        instance = new PoolDriver();
        try {
            DriverManager.registerDriver(PoolDriver.instance());
        } catch(SQLException e) {
            Logger.log("Unable to register Minerva DB pool driver!");
            Logger.exception(e);
        }
    }

    /**
     * Gets the singleton driver instance.
     */
    public static PoolDriver instance() {
        return instance;
    }

    private PoolDriver() {
    }

    /**
     * Tells which URLs this driver can handle.
     */
    public boolean acceptsURL(String url) throws java.sql.SQLException {
        return url.startsWith(URL_START);
    }

    /**
     * Retrieves a connection from a connection pool.
     */
    public Connection connect(String url, Properties props) throws java.sql.SQLException {
        if(url.startsWith(URL_START)) {
            Connection con = getXAConnection(url.substring(URL_START.length()));
            if(con == null)
                con = getJDBCConnection(url.substring(URL_START.length()));
            return con;
        }
        return null;  // No SQL Exception here!
    }

    private Connection getJDBCConnection(String name) {
        Connection con = null;
        try {
            DataSource source = JDBCPoolDataSource.getDataSource(name);
            if(source != null)
                con = source.getConnection();
        } catch(Exception e) {
            Logger.exception(e);
        }
        return con;
    }

    private Connection getXAConnection(String name) {
        Connection con = null;
        try {
            DataSource source = XAPoolDataSource.getDataSource(name);
            if(source != null)
                con = source.getConnection();
        } catch(Exception e) {
            Logger.exception(e);
        }
        return con;
    }

    /**
     * Returns the driver version.
     */
    public int getMajorVersion() {
        return 2;
    }

    /**
     * Returns the driver version.
     */
    public int getMinorVersion() {
        return 0;
    }

    /**
     * Returns no properties.  You do not need properties to connect to the
     * pool, and the properties for the underlying driver are not managed here.
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    /**
     * Returns <B>false</B> since it is not known which underlying driver will
     * be used and what its capabilities are.
     */
    public boolean jdbcCompliant() {
        return false;
    }
}