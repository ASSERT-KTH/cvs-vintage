/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.minerva.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Wraps a result set to track the last used time for the owning connection.
 * That time is updated every time a navigation action is performed on the
 * result set (next, previous, etc.).
 * @version $Revision: 1.2 $
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class ResultSetInPool implements ResultSet {
    private final static String CLOSED = "ResultSet has been closed!";
    private ResultSet impl;
    private StatementInPool st;

    /**
     * Creates a new wrapper from a source result set and statement wrapper.
     */
    ResultSetInPool(ResultSet source, StatementInPool owner) {
        impl = source;
        st = owner;
    }

    /**
     * Updates the last used time for the owning connection to the current time.
     */
    public void setLastUsed() {
        st.setLastUsed();
    }

    /**
     * Indicates that an error occured on the owning statement.
     */
    public void setError(SQLException e) {
        if(st != null)
            st.setError(e);
    }

    /**
     * Gets a reference to the "real" ResultSet.  This should only be used if
     * you need to cast that to a specific type to call a proprietary method -
     * you will defeat all the pooling if you use the underlying ResultSet
     * directly.
     */
    public ResultSet getUnderlyingResultSet() {
        return impl;
    }

    // ---- Implementation of java.sql.ResultSet ----

    public boolean absolute(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        return impl.absolute(arg0);
    }

    public void afterLast() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.afterLast();
    }

    public void beforeFirst() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.beforeFirst();
    }

    public void cancelRowUpdates() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.cancelRowUpdates();
    }

    public void clearWarnings() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.clearWarnings();
    }

    public void close() throws SQLException {
        if(impl != null) {
            impl.close();
            impl = null;
        }
        st = null;
    }

    public void deleteRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.deleteRow();
    }

    public int findColumn(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.findColumn(arg0);
    }

    public boolean first() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        return impl.first();
    }

    public Array getArray(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getArray(arg0);
    }

    public Array getArray(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getArray(arg0);
    }

    public java.io.InputStream getAsciiStream(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getAsciiStream(arg0);
    }

    public java.io.InputStream getAsciiStream(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getAsciiStream(arg0);
    }

    public java.math.BigDecimal getBigDecimal(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBigDecimal(arg0);
    }

    public java.math.BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBigDecimal(arg0, arg1);
    }

    public java.math.BigDecimal getBigDecimal(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBigDecimal(arg0);
    }

    public java.math.BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBigDecimal(arg0, arg1);
    }

    public java.io.InputStream getBinaryStream(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBinaryStream(arg0);
    }

    public java.io.InputStream getBinaryStream(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBinaryStream(arg0);
    }

    public Blob getBlob(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBlob(arg0);
    }

    public Blob getBlob(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBlob(arg0);
    }

    public boolean getBoolean(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBoolean(arg0);
    }

    public boolean getBoolean(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBoolean(arg0);
    }

    public byte getByte(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getByte(arg0);
    }

    public byte getByte(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getByte(arg0);
    }

    public byte[] getBytes(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBytes(arg0);
    }

    public byte[] getBytes(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getBytes(arg0);
    }

    public java.io.Reader getCharacterStream(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getCharacterStream(arg0);
    }

    public java.io.Reader getCharacterStream(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getCharacterStream(arg0);
    }

    public Clob getClob(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getClob(arg0);
    }

    public Clob getClob(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getClob(arg0);
    }

    public int getConcurrency() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getConcurrency();
    }

    public String getCursorName() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getCursorName();
    }

    public Date getDate(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getDate(arg0);
    }

    public Date getDate(int arg0, java.util.Calendar arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getDate(arg0, arg1);
    }

    public Date getDate(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getDate(arg0);
    }

    public Date getDate(String arg0, java.util.Calendar arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getDate(arg0, arg1);
    }

    public double getDouble(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getDouble(arg0);
    }

    public double getDouble(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getDouble(arg0);
    }

    public int getFetchDirection() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getFetchSize();
    }

    public float getFloat(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getFloat(arg0);
    }

    public float getFloat(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getFloat(arg0);
    }

    public int getInt(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getInt(arg0);
    }

    public int getInt(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getInt(arg0);
    }

    public long getLong(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getLong(arg0);
    }

    public long getLong(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getLong(arg0);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getMetaData();
    }

    public Object getObject(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getObject(arg0);
    }

    public Object getObject(int arg0, java.util.Map arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getObject(arg0, arg1);
    }

    public Object getObject(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getObject(arg0);
    }

    public Object getObject(String arg0, java.util.Map arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getObject(arg0, arg1);
    }

    public Ref getRef(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getRef(arg0);
    }

    public Ref getRef(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getRef(arg0);
    }

    public int getRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getRow();
    }

    public short getShort(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getShort(arg0);
    }

    public short getShort(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getShort(arg0);
    }

    public Statement getStatement() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getStatement();
    }

    public String getString(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getString(arg0);
    }

    public String getString(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getString(arg0);
    }

    public Time getTime(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTime(arg0);
    }

    public Time getTime(int arg0, java.util.Calendar arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTime(arg0, arg1);
    }

    public Time getTime(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTime(arg0);
    }

    public Time getTime(String arg0, java.util.Calendar arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTime(arg0, arg1);
    }

    public Timestamp getTimestamp(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTimestamp(arg0);
    }

    public Timestamp getTimestamp(int arg0, java.util.Calendar arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTimestamp(arg0, arg1);
    }

    public Timestamp getTimestamp(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTimestamp(arg0);
    }

    public Timestamp getTimestamp(String arg0, java.util.Calendar arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getTimestamp(arg0, arg1);
    }

    public int getType() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getType();
    }

    public java.io.InputStream getUnicodeStream(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getUnicodeStream(arg0);
    }

    public java.io.InputStream getUnicodeStream(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getUnicodeStream(arg0);
    }

    public SQLWarning getWarnings() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.getWarnings();
    }

    public void insertRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.insertRow();
    }

    public boolean isAfterLast() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.isAfterLast();
    }

    public boolean isBeforeFirst() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.isBeforeFirst();
    }

    public boolean isFirst() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.isFirst();
    }

    public boolean isLast() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.isLast();
    }

    public boolean last() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        return impl.last();
    }

    public void moveToCurrentRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.moveToCurrentRow();
    }

    public void moveToInsertRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.moveToInsertRow();
    }

    public boolean next() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        return impl.next();
    }

    public boolean previous() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        return impl.previous();
    }

    public void refreshRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.refreshRow();
    }

    public boolean relative(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        return impl.relative(arg0);
    }

    public boolean rowDeleted() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.rowDeleted();
    }

    public boolean rowInserted() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.rowInserted();
    }

    public boolean rowUpdated() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.rowUpdated();
    }

    public void setFetchDirection(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.setFetchDirection(arg0);
    }

    public void setFetchSize(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.setFetchSize(arg0);
    }

    public void updateAsciiStream(int arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateAsciiStream(arg0, arg1, arg2);
    }

    public void updateAsciiStream(String arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateAsciiStream(arg0, arg1, arg2);
    }

    public void updateBigDecimal(int arg0, java.math.BigDecimal arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBigDecimal(arg0, arg1);
    }

    public void updateBigDecimal(String arg0, java.math.BigDecimal arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBigDecimal(arg0, arg1);
    }

    public void updateBinaryStream(int arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBinaryStream(arg0, arg1, arg2);
    }

    public void updateBinaryStream(String arg0, java.io.InputStream arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBinaryStream(arg0, arg1, arg2);
    }

    public void updateBoolean(int arg0, boolean arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBoolean(arg0, arg1);
    }

    public void updateBoolean(String arg0, boolean arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBoolean(arg0, arg1);
    }

    public void updateByte(int arg0, byte arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateByte(arg0, arg1);
    }

    public void updateByte(String arg0, byte arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateByte(arg0, arg1);
    }

    public void updateBytes(int arg0, byte[] arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBytes(arg0, arg1);
    }

    public void updateBytes(String arg0, byte[] arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateBytes(arg0, arg1);
    }

    public void updateCharacterStream(int arg0, java.io.Reader arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateCharacterStream(arg0, arg1, arg2);
    }

    public void updateCharacterStream(String arg0, java.io.Reader arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateCharacterStream(arg0, arg1, arg2);
    }

    public void updateDate(int arg0, Date arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateDate(arg0, arg1);
    }

    public void updateDate(String arg0, Date arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateDate(arg0, arg1);
    }

    public void updateDouble(int arg0, double arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateDouble(arg0, arg1);
    }

    public void updateDouble(String arg0, double arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateDouble(arg0, arg1);
    }

    public void updateFloat(int arg0, float arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateFloat(arg0, arg1);
    }

    public void updateFloat(String arg0, float arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateFloat(arg0, arg1);
    }

    public void updateInt(int arg0, int arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateInt(arg0, arg1);
    }

    public void updateInt(String arg0, int arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateInt(arg0, arg1);
    }

    public void updateLong(int arg0, long arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateLong(arg0, arg1);
    }

    public void updateLong(String arg0, long arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateLong(arg0, arg1);
    }

    public void updateNull(int arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateNull(arg0);
    }

    public void updateNull(String arg0) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateNull(arg0);
    }

    public void updateObject(int arg0, Object arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateObject(arg0, arg1);
    }

    public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateObject(arg0, arg1, arg2);
    }

    public void updateObject(String arg0, Object arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateObject(arg0, arg1);
    }

    public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateObject(arg0, arg1, arg2);
    }

    public void updateRow() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        setLastUsed();
        impl.updateRow();
    }

    public void updateShort(int arg0, short arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateShort(arg0, arg1);
    }

    public void updateShort(String arg0, short arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateShort(arg0, arg1);
    }

    public void updateString(int arg0, String arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateString(arg0, arg1);
    }

    public void updateString(String arg0, String arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateString(arg0, arg1);
    }

    public void updateTime(int arg0, Time arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateTime(arg0, arg1);
    }

    public void updateTime(String arg0, Time arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateTime(arg0, arg1);
    }

    public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateTimestamp(arg0, arg1);
    }

    public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        impl.updateTimestamp(arg0, arg1);
    }

    public boolean wasNull() throws SQLException {
        if(impl == null) throw new SQLException(CLOSED);
        return impl.wasNull();
    }

    // ---- End Implementation of ResultSet ----
}
