package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class BooleanDataType extends SQLDataType
{
   public BooleanDataType(String name)
   {
      super(name, AbstractType.BOOLEAN, Types.BIT); // JDBC3.0 also has BOOLEAN
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      return rs.getBoolean(index) ? Boolean.TRUE : Boolean.FALSE;
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      return cs.getBoolean(index) ? Boolean.TRUE : Boolean.FALSE;
   }
}
