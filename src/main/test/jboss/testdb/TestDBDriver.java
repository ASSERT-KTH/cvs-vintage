/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test.jboss.testdb;

import java.sql.*;
import java.util.Properties;

/**
 * Database driver for unit tests.  Creates connections that implement virtually
 * nothing - enough to test with, in other words.
 * @version $Revision: 1.5 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 */
public class TestDBDriver implements Driver {
    private final static String URL_START = "jdbc:testdb:";
    private final static TestDBDriver instance;
    static {
        instance = new TestDBDriver();
        try {
            DriverManager.registerDriver(TestDBDriver.instance());
        } catch(SQLException e) {
            Logger.error("Unable to register Test DB pool driver!");
            Logger.exception(e);
        }
    }
    public static TestDBDriver instance() {
        return instance;
    }

    public TestDBDriver() {
    }

    public boolean acceptsURL(String url) throws java.sql.SQLException {
        return url.startsWith(URL_START);
    }

    public Connection connect(String url, Properties props) throws java.sql.SQLException {
        if(!url.startsWith(URL_START)) throw new SQLException("Wrong URL!");
        return new TestConnection();
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public DriverPropertyInfo[] getPropertyInfo(String parm1, Properties parm2) throws java.sql.SQLException {
        return new DriverPropertyInfo[0];
    }

    public boolean jdbcCompliant() {
        return false;
    }
}