/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import java.util.HashMap;
import java.util.Map;

import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractClass;

public class Table implements AbstractClass
{
   private String name;
   private Map columns = new HashMap();
   private Map fkConstraints = new HashMap();
   private String[] pkFields;

   public Table(String name)
   {
      this.name = name;
   }

   public void addColumn(AbstractAttribute column)
   {
      columns.put(column.getName(), column);
   }

   public void addConstraintEnd(RelationshipEnd end)
   {
      fkConstraints.put(end.getName(), end);
   }

   public AbstractAttribute getAttributeByName(String name)
   {
      return (Column) columns.get(name);
   }

   public AbstractAssociationEnd getAssocationByName(String name)
   {
      return (RelationshipEnd) fkConstraints.get(name);
   }

   public String getName()
   {
      return name;
   }

   public String[] getPkFields()
   {
      return pkFields;
   }

   public void setPkFields(String[] pkFields)
   {
      this.pkFields = pkFields;
   }
}
