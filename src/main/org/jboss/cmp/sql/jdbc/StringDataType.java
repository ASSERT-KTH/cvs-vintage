package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class StringDataType extends SQLDataType
{
   public StringDataType(String name)
   {
      super(name, AbstractType.STRING, Types.VARCHAR);
   }

   public void setValue(PreparedStatement ps, int index, Object value) throws SQLException
   {
      ps.setString(index, (String)value);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      return rs.getString(index);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      return cs.getString(index);
   }
}
