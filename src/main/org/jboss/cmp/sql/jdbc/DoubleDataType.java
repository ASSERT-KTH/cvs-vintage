package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class DoubleDataType extends SQLDataType
{
   public DoubleDataType(String name)
   {
      super(name, AbstractType.DOUBLE, Types.DOUBLE);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      double value = rs.getDouble(index);
      return rs.wasNull() ? null : new Double(value);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      double value = cs.getDouble(index);
      return cs.wasNull() ? null : new Double(value);
   }
}
