/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import java.util.Iterator;

import org.jboss.cmp.schema.Query;
import org.jboss.cmp.schema.CollectionRelation;
import org.jboss.cmp.schema.Path;
import org.jboss.cmp.schema.RangeRelation;
import org.jboss.cmp.schema.Relation;

public class EJBQLGenerator
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
         buf.append(first ? " " : ", ");
         first = false;
         Path nav = (Path) i.next();
         if (nav.isCollection())
         {
            buf.append("OBJECT(").append(nav).append(")");
         }
         else
         {
            buf.append(nav);
         }
      }
      buf.append(" FROM");
      first = true;
      for (Iterator i = query.getAliases().iterator(); i.hasNext();)
      {
         buf.append(first ? " " : ", ");
         first = false;
         String alias = (String) i.next();
         Relation relation = query.getRelation(alias);
         if (relation instanceof RangeRelation)
         {
            CMPEntity entity = (CMPEntity) relation.getType();
            buf.append(entity.getSchemaName());
         }
         else if (relation instanceof CollectionRelation)
         {
            buf.append("IN(");
            buf.append(((CollectionRelation) relation).getPath());
            buf.append(")");
         }
         buf.append(" ");
         buf.append(alias);
      }
      return buf.toString();
   }
}
