package org.jboss.minerva.jdbc;

import java.sql.Connection;

public class PSCacheKey {
    public Connection con;
    public String sql;

    public PSCacheKey(Connection con, String sql) {
        this.con = con;
        this.sql = sql;
    }

    public boolean equals(Object o) {
        PSCacheKey key = (PSCacheKey)o;
        return key.con.equals(con) && key.sql.equals(sql);
    }

    public int hashCode() {
        return con.hashCode() ^ sql.hashCode();
    }
}
