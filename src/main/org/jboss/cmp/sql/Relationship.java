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

public class Relationship implements AbstractAssociation
{
   private String name;
   private RelationshipEnd leftEnd;
   private RelationshipEnd rightEnd;

   public Relationship(String name, RelationshipEnd leftEnd, RelationshipEnd rightEnd)
   {
      this.name = name;
      this.leftEnd = leftEnd;
      this.rightEnd = rightEnd;
      this.leftEnd.setPeer(this, rightEnd);
      this.rightEnd.setPeer(this, leftEnd);
   }

   public String getName()
   {
      return name;
   }

   public AbstractAssociationEnd getLeftEnd()
   {
      return leftEnd;
   }

   public AbstractAssociationEnd getRightEnd()
   {
      return rightEnd;
   }

   public String getJoinCondition(String leftAlias, String rightAlias)
   {
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      String[] leftCols = leftEnd.getColumnNames();
      String[] rightCols = rightEnd.getColumnNames();
      for (int i = 0; i < leftCols.length; i++)
      {
         String leftCol = leftCols[i];
         String rightCol = rightCols[i];
         if (i > 0)
         {
            buf.append(" AND ");
         }
         buf.append(leftAlias).append(".").append(leftCol);
         buf.append(" = ");
         buf.append(rightAlias).append(".").append(rightCol);
      }
      buf.append(")");
      return buf.toString();
   }
}
