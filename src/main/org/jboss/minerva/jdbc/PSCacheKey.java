package org.jboss.minerva.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;

public class PSCacheKey {
    public Connection con;
    public String sql;
    public int rsType;
    public int rsConcur;

    public PSCacheKey(Connection con, String sql) {
        this.con = con;
        this.sql = sql;
        this.rsType = ResultSet.TYPE_FORWARD_ONLY;
        this.rsConcur = ResultSet.CONCUR_READ_ONLY;
    }

    public PSCacheKey(Connection con, String sql, int rsType, int rsConcur) {
        this.con = con;
        this.sql = sql;
        this.rsType = rsType;
        this.rsConcur = rsConcur;
    }

    public boolean equals(Object o) {
        PSCacheKey key = (PSCacheKey)o;
        return key.con.equals(con) && key.sql.equals(sql) &&
               key.rsType == rsType && key.rsConcur == rsConcur;
    }

    public int hashCode() {
        return con.hashCode() ^ sql.hashCode() ^ rsType ^ rsConcur;
    }
}
