/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.CallableStatement;

import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractType;

public class Column implements AbstractAttribute
{
   private String name;
   private SQLDataType type;

   public Column(String name, SQLDataType type)
   {
      this.name = name;
      this.type = type;
   }

   public String getName()
   {
      return name;
   }

   public AbstractType getType()
   {
      return type;
   }

   public String toString()
   {
      return name;
   }

   public void setValue(PreparedStatement ps, int index, Object value) throws SQLException
   {
      type.setValue(ps, index, value);
   }

   public Object getValue(ResultSet rs, int index) throws SQLException
   {
      return type.getValue(rs, index);
   }

   public void bindParameter(CallableStatement cs, int index) throws SQLException
   {
      type.bindParameter(cs, index);
   }

   public Object getValue(CallableStatement cs, int index) throws SQLException
   {
      return type.getValue(cs, index);
   }
}
