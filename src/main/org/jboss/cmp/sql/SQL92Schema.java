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

import org.jboss.cmp.schema.AbstractClass;
import org.jboss.cmp.schema.AbstractSchema;

/**
 * Implementaion of an AbstractSchema for SQL92 based systems.
 */
public class SQL92Schema implements AbstractSchema
{
   private Map tables = new HashMap();
   private Map constraints = new HashMap();

   public AbstractClass getClassByName(String name)
   {
      return (AbstractClass) tables.get(name);
   }

   public boolean isClassNameInUse(String name)
   {
      return tables.keySet().contains(name);
   }

   /**
    * Add a Table to the schema
    * @param name the name of the table
    * @return a Table representing the table
    */
   public Table addTable(String name)
   {
      Table table = new Table(name);
      tables.put(name, table);
      return table;
   }

   public Table getTableByName(String name)
   {
      return (Table) getClassByName(name);
   }

   /**
    * Add a foreign key constraint referencing the primary key of the parent
    * @param name the name of the constraint
    * @param parentEndName a name used to identify this constraint from the parent
    * @param parent the parent Table
    * @param childEndName a name used to identify this constraint from the child
    * @param child the child Table
    * @param fkColumnNames column names in the child table in this constraint
    * @return a Relationship describing this constraint
    */
   public Relationship addFKConstraint(String name, String parentEndName, Table parent, String childEndName, Table child, String[] fkColumnNames)
   {
      return addFKConstraint(name, parentEndName, parent, parent.getPkFields(), childEndName, child, fkColumnNames);
   }

   /**
    * Add a foreign key constraint referencing the a arbitrary unqiue key of the parent
    * @param name the name of the constraint
    * @param parentEndName a name used to identify this constraint from the parent
    * @param parent the parent Table
    * @param pkColumnNames column names in the parent table in this constraint
    * @param childEndName a name used to identify this constraint from the child
    * @param child the child Table
    * @param fkColumnNames column names in the child table in this constraint
    * @return a Relationship describing this constraint
    */
   public Relationship addFKConstraint(String name, String parentEndName, Table parent, String[] pkColumnNames, String childEndName, Table child, String[] fkColumnNames)
   {
      RelationshipEnd leftEnd = new RelationshipEnd(parentEndName, parent, false, pkColumnNames);
      RelationshipEnd rightEnd = new RelationshipEnd(childEndName, child, true, fkColumnNames);
      Relationship cons = new Relationship(name, leftEnd, rightEnd);
      parent.addConstraintEnd(leftEnd);
      child.addConstraintEnd(rightEnd);
      constraints.put(cons.getName(), cons);
      return cons;
   }

   public Relationship getConstraintByName(String name)
   {
      return (Relationship) constraints.get(name);
   }
}
