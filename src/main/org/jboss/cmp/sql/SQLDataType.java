/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import org.jboss.cmp.schema.AbstractType;

public class SQLDataType implements AbstractType
{
   private String name;
   private int family;
   private int jdbcType;

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
}
