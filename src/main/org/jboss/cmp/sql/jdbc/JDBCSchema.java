package org.jboss.cmp.sql.jdbc;

import java.util.HashMap;
import java.util.Map;

import org.jboss.cmp.schema.AbstractType;
import org.jboss.cmp.sql.SQL92Schema;

public class JDBCSchema extends SQL92Schema
{
   private static final AbstractType[] builtins = {
      null,
      null,
      new BooleanDataType("BIT"),
      new StringDataType("VARCHAR"),
      new IntegerDataType("INTEGER"),
      new DoubleDataType("DOUBLE"),
      new DecimalDataType("DECIMAL"),
      new DateTimeDataType("TIMESTAMP"),
      new BinaryDataType("BINARY")
   };
   private static final Map initialTypeMap;
   static {
      initialTypeMap = new HashMap();
      for (int i=2; i < builtins.length; i++)
      {
         initialTypeMap.put(builtins[i].getName(), builtins[i]);
      }
   }

   public JDBCSchema()
   {
      super(initialTypeMap);
   }

   public AbstractType getBuiltinType(int family)
   {
      return builtins[family];
   }
}
