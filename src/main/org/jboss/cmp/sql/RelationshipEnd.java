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
}
