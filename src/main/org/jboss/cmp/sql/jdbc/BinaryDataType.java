package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class BinaryDataType extends SQLDataType
{
   public BinaryDataType(String name)
   {
      super(name, AbstractType.BINARY, Types.VARBINARY);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      return rs.getBytes(index);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      return cs.getBytes(index);
   }
}
