package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class IntegerDataType extends SQLDataType
{
   public IntegerDataType(String name)
   {
      super(name, AbstractType.INTEGER, Types.INTEGER);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      int value = rs.getInt(index);
      return rs.wasNull() ? null : new Integer(value);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      int value = cs.getInt(index);
      return cs.wasNull() ? null : new Integer(value);
   }
}
