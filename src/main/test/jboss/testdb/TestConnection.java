/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package test.jboss.testdb;

import java.sql.*;
import java.util.Map;

/**
 * Database connection for unit tests.  Currently nothing is implemented except
 * close, isClosed, isAutoCommit, setAutoCommit(true), and rollback.  Everything
 * else throws a SQLException.
 * @version $Revision: 1.3 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 */
public class TestConnection implements Connection {
    private final static String TEST_DB = "Not implemented in test database.";
    private boolean closed = false;

    public TestConnection() {
    }

    public void clearWarnings() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public void close() throws java.sql.SQLException {
        closed = true;
    }

    public void commit() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public Statement createStatement() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public Statement createStatement(int parm1, int parm2) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public boolean getAutoCommit() throws java.sql.SQLException {
        return true;
    }

    public String getCatalog() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public DatabaseMetaData getMetaData() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public int getTransactionIsolation() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public Map getTypeMap() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public SQLWarning getWarnings() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public boolean isClosed() throws java.sql.SQLException {
        return closed;
    }

    public boolean isReadOnly() throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public String nativeSQL(String parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public CallableStatement prepareCall(String parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public CallableStatement prepareCall(String parm1, int parm2, int parm3) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public PreparedStatement prepareStatement(String parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public PreparedStatement prepareStatement(String parm1, int parm2, int parm3) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public void rollback() throws java.sql.SQLException {
    }

    public void setAutoCommit(boolean ac) throws java.sql.SQLException {
        if(!ac)
            throw new SQLException(TEST_DB);
    }

    public void setCatalog(String parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public void setReadOnly(boolean parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public void setTransactionIsolation(int parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }

    public void setTypeMap(Map parm1) throws java.sql.SQLException {
        throw new SQLException(TEST_DB);
    }
}