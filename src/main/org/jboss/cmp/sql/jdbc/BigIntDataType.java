package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class BigIntDataType extends SQLDataType
{
   public BigIntDataType(String name)
   {
      super(name, AbstractType.INTEGER, Types.BIGINT);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      long value = rs.getLong(index);
      return rs.wasNull() ? null : new Long(value);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      long value = cs.getLong(index);
      return cs.wasNull() ? null : new Long(value);
   }
}
