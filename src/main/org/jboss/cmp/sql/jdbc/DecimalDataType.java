package org.jboss.cmp.sql.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;
import java.math.BigDecimal;

import org.jboss.cmp.sql.SQLDataType;
import org.jboss.cmp.schema.AbstractType;

public class DecimalDataType extends SQLDataType
{
   public DecimalDataType(String name)
   {
      super(name, AbstractType.DECIMAL, Types.DECIMAL);
   }

   public void setValue(PreparedStatement ps, int index, Object value) throws SQLException
   {
      ps.setBigDecimal(index, (BigDecimal)value);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      return rs.getBigDecimal(index);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      return cs.getBigDecimal(index);
   }
}
