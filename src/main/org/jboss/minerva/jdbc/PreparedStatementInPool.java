/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Ref;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Array;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.HashMap;

public class PreparedStatementInPool extends StatementInPool implements PreparedStatement {
    public final static HashMap preparedStatementCache = new HashMap();

    private final static String CLOSED = "PreparedStatement has been closed!";
    private PreparedStatement impl;
    private ConnectionWrapper con;
    private String sql;

    /**
     * Creates a new statement from a source statement and wrapper connection.
     */
    public PreparedStatementInPool(PreparedStatement source, ConnectionWrapper owner, String sql) {
        super(source, owner);
        if(source == null || owner == null) throw new NullPointerException();
        impl = source;
        con = owner;
        this.sql = sql;
    }

    /**
     * Gets a reference to the "real" Statement.  This should only be used if
     * you need to cast that to a specific type to call a proprietary method -
     * you will defeat all the pooling if you use the underlying Statement
     * directly.
     */
    public PreparedStatement getUnderlyingPreparedStatement() {
        return impl;
    }

    /**
     * Returns the SQL Statement string.
     */
    public String getSql() {
        return sql;
    }

    // ---- Implementation of java.sql.Statement ----

    public ResultSet executeQuery() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            setLastUsed();
            return new ResultSetInPool(impl.executeQuery(), this);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public int executeUpdate() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            setLastUsed();
            return impl.executeUpdate();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setNull(parameterIndex, sqlType);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setBoolean(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setByte(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setShort(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setInt(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setLong(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setFloat(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setDouble(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setBigDecimal(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setString(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setBytes(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setDate(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setTime(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setTimestamp(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setAsciiStream(parameterIndex, x, length);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setUnicodeStream(parameterIndex, x, length);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setBinaryStream(parameterIndex, x, length);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void clearParameters() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.clearParameters();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setObject(parameterIndex, x, targetSqlType, scale);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setObject(parameterIndex, x, targetSqlType);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setObject(parameterIndex, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public boolean execute() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            setLastUsed();
            return impl.execute();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void addBatch() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.addBatch();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setCharacterStream(parameterIndex, reader, length);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setRef(int i, Ref x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setRef(i, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setBlob(int i, Blob x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setBlob(i, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setClob(int i, Clob x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setClob(i, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setArray(int i, Array x) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setArray(i, x);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            return impl.getMetaData();
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setDate(parameterIndex, x, cal);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setTime(parameterIndex, x, cal);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setTimestamp(parameterIndex, x, cal);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        try {
            impl.setNull(paramIndex, sqlType, typeName);
        } catch(SQLException e) {
            setError(e);
            throw e;
        }
    }

    public void close() throws SQLException {
        con.statementClosed(this);
        super.clearFields();
        con = null;
        impl = null;
        sql = null;
    }
}
