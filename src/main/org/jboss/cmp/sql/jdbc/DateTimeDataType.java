package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;
import java.sql.Timestamp;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class DateTimeDataType extends SQLDataType
{
   public DateTimeDataType(String name)
   {
      super(name, AbstractType.DATETIME, Types.TIMESTAMP);
   }

   public void setValue(PreparedStatement ps, int index, Object value) throws SQLException
   {
      ps.setTimestamp(index, (Timestamp)value);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      return rs.getTimestamp(index);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      return cs.getTimestamp(index);
   }
}
