/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import java.util.Iterator;

import org.jboss.cmp.schema.Query;
import org.jboss.cmp.schema.AbstractType;
import org.jboss.cmp.schema.JoinRelation;
import org.jboss.cmp.schema.Path;
import org.jboss.cmp.schema.RangeRelation;
import org.jboss.cmp.schema.Relation;

public class SQL92Generator
{
   public String generate(Query query)
   {
      boolean first;

      StringBuffer buf = new StringBuffer(1000);
      buf.append("SELECT");
      if (query.isDistinct())
      {
         buf.append(" DISTINCT");
      }
      first = true;
      for (Iterator i = query.getProjections().iterator(); i.hasNext();)
      {
         if (first)
         {
            buf.append(" ");
            first = false;
         }
         else
         {
            buf.append(", ");
         }
         Path nav = (Path) i.next();
         AbstractType type = nav.getType();
         if (type instanceof Table)
         {
            Table table = (Table) type;
            String[] columnNames = table.getPkFields();
            for (int j = 0; j < columnNames.length; j++)
            {
               if (j > 0)
               {
                  buf.append(", ");
               }
               String columnName = columnNames[j];
               buf.append(nav.getRoot().getName()).append(".").append(columnName);
            }
         }
         else
         {
            buf.append(nav);
         }
      }
      buf.append(" FROM ");
      first = true;
      for (Iterator i = query.getAliases().iterator(); i.hasNext();)
      {
         String alias = (String) i.next();
         Relation rel = query.getRelation(alias);
         if (rel instanceof RangeRelation)
         {
            if (!first)
            {
               buf.append(" CROSS JOIN ");
            }
            else
            {
               first = false;
            }
            buf.append(rel.getType().getName());
            buf.append(" ").append(alias);
         }
         else if (rel instanceof JoinRelation)
         {
            buf.append(" INNER JOIN ").append(rel.getType().getName());
            buf.append(" ").append(alias);
            buf.append(" ON ").append(((JoinRelation) rel).getCondition());
         }
      }
      return buf.toString();
   }
}
