/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.jdbc;

import java.sql.*;
import java.util.*;
import org.jboss.minerva.pools.*;

/**
 * Wrapper for database connections in a pool.  Handles closing appropriately.
 * The connection is returned to the pool rather than truly closing, any
 * outstanding statements are closed, and the connection is rolled back.  This
 * class is also used by statements, etc. to update the last used time for the
 * connection.
 * @version $Revision: 1.1 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class ConnectionInPool implements PooledObject, ConnectionWrapper {
    private final static String CLOSED = "Connection has been closed!";

    private Connection con;
    private HashSet statements;
    private Vector listeners;

    /**
     * Creates a new connection wrapper.
     * @param con The "real" database connection to wrap.
     * @param pool The database pool this connection came from.
     */
    public ConnectionInPool(Connection con) {
        this.con = con;
        statements = new HashSet();
        listeners = new Vector();
    }

    /**
     * Adds a listener for pool events.
     */
    public void addPoolEventListener(PoolEventListener listener) {
        listeners.addElement(listener);
    }

    /**
     * Removes a listener for pool events.
     */
    public void removePoolEventListener(PoolEventListener listener) {
        listeners.remove(listener);
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
    }

    /**
     * Updates the last used time for this connection to the current time.
     */
    public void setLastUsed() {
        firePoolEvent(new PoolEvent(this, PoolEvent.OBJECT_USED));
    }

    /**
     * Indicates that an error occured on this connection.
     */
    public void setError(SQLException e) {
        firePoolEvent(new PoolEvent(this, PoolEvent.OBJECT_ERROR));
    }

    /**
     * Indicates that a statement has been closed and no longer needs to be
     * tracked.  Outstanding statements are closed when the connection is
     * returned to the pool.
     */
    public void statementClosed(Statement st) {
        statements.remove(st);
    }

    /**
     * Prepares a connection to be returned to the pool.  All outstanding
     * statements are closed, and if AutoCommit is off, the connection is
     * rolled back.  No further SQL calls are possible once this is called.
     */
    public void reset() throws SQLException {
        Connection local = con;
        con = null;

        Collection copy = (Collection)statements.clone();
        Iterator it = copy.iterator();
        while(it.hasNext())
            try {
                ((Statement)it.next()).close();
            } catch(SQLException e) {}
        if(!local.getAutoCommit())
            local.rollback();
    }

    /**
     * Dispatches an event to the listeners.
     */
    protected void firePoolEvent(PoolEvent evt) {
        Vector local = (Vector)listeners.clone();
        for(int i=local.size()-1; i >= 0; i--)
            if(evt.getType() == PoolEvent.OBJECT_CLOSED)
                ((PoolEventListener)local.elementAt(i)).objectClosed(evt);
            else if(evt.getType() == PoolEvent.OBJECT_ERROR)
                ((PoolEventListener)local.elementAt(i)).objectError(evt);
            else
                ((PoolEventListener)local.elementAt(i)).objectUsed(evt);
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
            return con.prepareStatement(sql);
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
        try {
            con.commit();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void rollback() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        try {
            con.rollback();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void close() throws SQLException {
        if(con == null) throw new SQLException(CLOSED);
        firePoolEvent(new PoolEvent(this, PoolEvent.OBJECT_CLOSED));
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
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
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