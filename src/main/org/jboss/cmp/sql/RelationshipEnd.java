/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import org.jboss.cmp.schema.AbstractAssociation;
import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractClass;
import org.jboss.cmp.query.Path;

public class RelationshipEnd implements AbstractAssociationEnd
{
   private String name;
   private AbstractClass type;
   private boolean collection;
   private Relationship constraint;
   private RelationshipEnd peer;
   private String[] columnNames;

   public RelationshipEnd(String name, AbstractClass type, boolean collection, String[] columnNames)
   {
      this.name = name;
      this.type = type;
      this.collection = collection;
      this.columnNames = columnNames;
   }

   public String getName()
   {
      return name;
   }

   public AbstractClass getType()
   {
      return type;
   }

   public boolean isNavigable()
   {
      return true;
   }

   public boolean isCollection()
   {
      return collection;
   }

   public AbstractAssociationEnd getPeer()
   {
      return peer;
   }

   public AbstractAssociation getAssociation()
   {
      return constraint;
   }

   /* package */
   void setPeer(Relationship constraint, RelationshipEnd peer)
   {
      this.constraint = constraint;
      this.peer = peer;
   }

   public String[] getColumnNames()
   {
      return columnNames;
   }

   public String getJoinCondition(String leftAlias, String rightAlias)
   {
      StringBuffer buf = new StringBuffer(50);
      String[] rightColumns = peer.getColumnNames();
      buf.append("(");
      for (int i = 0; i < columnNames.length; i++)
      {
         if (i > 0)
         {
            buf.append(" AND ");
         }
         buf.append(leftAlias).append(".").append(columnNames[i]);
         buf.append(" = ");
         buf.append(rightAlias).append(".").append(rightColumns[i]);
      }
      buf.append(")");
      return buf.toString();
   }

   public String getIsNullCondition(boolean not, Path path)
   {
      StringBuffer buf = new StringBuffer();
      if (collection)
      {
         // fk end
         buf.append("(");
         for (int i = 0; i < columnNames.length; i++)
         {
            if (i > 0)
            {
               buf.append(" AND ");
            }
            buf.append(path.getRoot().getAlias()).append(".").append(columnNames[i]);
            buf.append(not ? " IS NOT NULL" : " IS NULL");
         }
         buf.append(")");
      }
      else
      {
         // pk end
         String alias = path.toString("_");
         if (not == false)
            buf.append("NOT ");
         buf.append("EXISTS(SELECT 1 FROM ");
         buf.append(peer.getType().getName()).append(' ').append(alias);
         buf.append(" WHERE ");
         buf.append(getJoinCondition(path.getRoot().getAlias(), alias));
         buf.append(')');
      }
      return buf.toString();
   }

   public String toString()
   {
      return name;
   }
}
