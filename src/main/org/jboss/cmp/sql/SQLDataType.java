/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jboss.cmp.schema.AbstractType;

public abstract class SQLDataType implements AbstractType
{
   protected final String name;
   protected final int family;
   protected final int jdbcType;

   public SQLDataType(String name, int family, int jdbcType)
   {
      this.name = name;
      this.family = family;
      this.jdbcType = jdbcType;
   }

   public String getName()
   {
      return name;
   }

   public int getFamily()
   {
      return family;
   }

   public int getJdbcType()
   {
      return jdbcType;
   }

   public void setValue(PreparedStatement ps, int index, Object value) throws SQLException
   {
      ps.setObject(index, value, jdbcType);
   }

   public abstract Object getValue(ResultSet rs, int index) throws SQLException;

   public void bindParameter(CallableStatement cs, int index) throws SQLException
   {
      cs.registerOutParameter(index, jdbcType);
   }

   public abstract Object getValue(CallableStatement cs, int index) throws SQLException;
}
