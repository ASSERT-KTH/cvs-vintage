/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.xa;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.jboss.minerva.jdbc.ConnectionWrapper;
import org.jboss.minerva.jdbc.PreparedStatementInPool;
import org.jboss.minerva.jdbc.PSCacheKey;
import org.jboss.minerva.jdbc.StatementInPool;
import org.jboss.minerva.pools.PoolEvent;

/**
 * Wrapper for database connections used by an XAConnection.  When close is
 * called, it does not close the underlying connection, just informs the
 * XAConnection that close was called.  The connection will not be closed (or
 * returned to the pool) until the transactional details are taken care of.
 * This instance only lives as long as one client is using it - though we
 * probably want to consider reusing it to save object allocations.
 * @version $Revision: 1.7 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAClientConnection implements ConnectionWrapper {
    private final static String CLOSED = "Connection has been closed!";

    private Connection con;
    private HashSet statements;
    private Vector listeners;
    private XAConnectionImpl xaCon;

    /**
     * Creates a new connection wrapper.
     * @param xaCon The handler for all the transactional details.
     * @param con The "real" database connection to wrap.
     */
    public XAClientConnection(XAConnectionImpl xaCon, Connection con) {
        this.con = con;
        this.xaCon = xaCon;
        statements = new HashSet();
        listeners = new Vector();
    }

    /**
     * Gets a reference to the "real" connection.  This should only be used if
     * you need to cast that to a specific type to call a proprietary method -
     * you will defeat all the pooling if you use the underlying connection
     * directly.
     */
    public Connection getUnderlyingConnection() {
        return con;
    }

    /**
     * Closes this connection wrapper permanently.  All further calls with throw
     * a SQLException.
     */
    public void shutdown() {
        con = null;
        statements = null;
        listeners = null;
        xaCon = null;
    }

    /**
     * Updates the last used time for this connection to the current time.
     * This is not used by the current implementation.
     */
    public void setLastUsed() {
        xaCon.firePoolEvent(new PoolEvent(xaCon, PoolEvent.OBJECT_USED));
    }

    /**
     * Indicates that an error occured on this connection.
     */
    public void setError(SQLException e) {
        xaCon.setConnectionError(e);
    }

    /**
     * Indicates that a statement has been closed and no longer needs to be
     * tracked.  Outstanding statements are closed when the connection is
     * returned to the pool.
     */
    public void statementClosed(Statement st) {
        statements.remove(st);
        if ((con != null) && (st instanceof PreparedStatementInPool)) {
            // Now return the "real" statement to the pool
            PreparedStatementInPool ps = (PreparedStatementInPool) st;
            PreparedStatement ups = ps.getUnderlyingPreparedStatement();
            int rsType = ResultSet.TYPE_FORWARD_ONLY;
            int rsConcur = ResultSet.CONCUR_READ_ONLY;

            // We may have JDBC 1.0 driver
            try {
                rsType = ups.getResultSetType();
                rsConcur = ups.getResultSetConcurrency();
            } catch (Throwable th) {
            }
            PreparedStatementInPool.preparedStatementCache.put(
                    new PSCacheKey(con, ps.getSql(), rsType, rsConcur), ups);
        }
    }

    // ---- Implementation of java.sql.Connection ----
    public Statement createStatement() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            StatementInPool st = new StatementInPool(con.createStatement(), this);
            statements.add(st);
            return st;
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            // Seek in the pool and remove if found: the same "real" PreparedStatement
            // cannot be used by two wrappers since ps.executeQuery() closes all
            // ResultSets of the PreparedStatement that may be in use through other wrappers.
            // The "real" statement will be returned to the pool on PreparedStatementInPool.close().
            PreparedStatement ps = (PreparedStatement)PreparedStatementInPool.preparedStatementCache.remove(
                                        new PSCacheKey(con, sql));
            if(ps == null) {
                ps = con.prepareStatement(sql);
            }

            PreparedStatementInPool wrapper = new PreparedStatementInPool(ps, this, sql);
            statements.add(wrapper);
            return wrapper;
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.prepareCall(sql);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String nativeSQL(String sql) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.nativeSQL(sql);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        if(((XAResourceImpl)xaCon.getXAResource()).isTransaction() && autoCommit)
            throw new SQLException("Cannot set AutoCommit for a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");

        try {
            con.setAutoCommit(autoCommit);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }

    }

    public boolean getAutoCommit() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.getAutoCommit();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void commit() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        if(((XAResourceImpl)xaCon.getXAResource()).isTransaction())
            throw new SQLException("Cannot commit a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");
        try {
            con.commit();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void rollback() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        if(((XAResourceImpl)xaCon.getXAResource()).isTransaction())
            throw new SQLException("Cannot rollback a transactional connection: See JDBC 2.0 Optional Package Specification section 7.1 (p25)");
    }

    public void close() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        Collection copy = (Collection)statements.clone();
        Iterator it = copy.iterator();
        while(it.hasNext())
            try {
                ((Statement)it.next()).close();
            } catch(SQLException e) {}

        xaCon.clientConnectionClosed();
        shutdown();
    }

    public boolean isClosed() throws SQLException {
        if(con == null) return true;
        try {
            return con.isClosed();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.getMetaData();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            con.setReadOnly(readOnly);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean isReadOnly() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.isReadOnly();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setCatalog(String catalog) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            con.setCatalog(catalog);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public String getCatalog() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.getCatalog();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTransactionIsolation(int level) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            con.setTransactionIsolation(level);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int getTransactionIsolation() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.getTransactionIsolation();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public SQLWarning getWarnings() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.getWarnings();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void clearWarnings() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            con.clearWarnings();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            StatementInPool st = new StatementInPool(con.createStatement(resultSetType, resultSetConcurrency), this);
            statements.add(st);
            return st;
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            // Seek in the pool and remove if found: the same "real" PreparedStatement
            // cannot be used by two wrappers since ps.executeQuery() closes all
            // ResultSets of the PreparedStatement that may be in use through other wrappers.
            // The "real" statement will be returned to the pool on PreparedStatementInPool.close().
            PreparedStatement ps = (PreparedStatement)PreparedStatementInPool.preparedStatementCache.remove(
                                        new PSCacheKey(con, sql, resultSetType, resultSetConcurrency));
            if(ps == null) {
                ps = con.prepareStatement(sql, resultSetType, resultSetConcurrency);
            }
            PreparedStatementInPool wrapper = new PreparedStatementInPool(ps, this, sql);
            statements.add(wrapper);
            return wrapper;
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public Map getTypeMap() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            return con.getTypeMap();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTypeMap(Map map) throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            con.setTypeMap(map);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }
}