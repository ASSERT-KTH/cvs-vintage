/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.sql;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.jboss.cmp.schema.AbstractClass;
import org.jboss.cmp.schema.AbstractSchema;
import org.jboss.cmp.schema.AbstractType;
import org.jboss.cmp.schema.DuplicateNameException;

/**
 * Implementaion of an AbstractSchema for SQL92 based systems.
 */
public class SQL92Schema implements AbstractSchema
{
   private Map tables = new HashMap();
   private Map constraints = new HashMap();

   private static final AbstractType[] builtins = {
      new SQLDataType("NULL", AbstractType.VOID, Types.NULL),
      new SQLDataType("TYPE", AbstractType.OBJECT, Types.JAVA_OBJECT),
      new SQLDataType("BIT", AbstractType.BOOLEAN, Types.BIT),
      new SQLDataType("VARCHAR", AbstractType.STRING, Types.VARCHAR),
      new SQLDataType("INTEGER", AbstractType.INTEGER, Types.INTEGER),
      new SQLDataType("DOUBLE", AbstractType.FLOAT, Types.DOUBLE),
      new SQLDataType("TIMESTAMP", AbstractType.DATETIME, Types.TIMESTAMP)
   };

   public AbstractClass getClassByName(String name)
   {
      return (AbstractClass) tables.get(name);
   }

   public boolean isClassNameInUse(String name)
   {
      return tables.keySet().contains(name);
   }

   public AbstractType getBuiltinType(int family)
   {
      return builtins[family];
   }

   /**
    * Add a Table to the schema
    * @param name the name of the table
    * @return a Table representing the table
    */
   public Table addTable(String name) throws DuplicateNameException
   {
      if (tables.keySet().contains(name))
      {
         throw new DuplicateNameException(name);
      }
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
      RelationshipEnd leftEnd = new RelationshipEnd(parentEndName, parent, false, parent.getPkFields());
      RelationshipEnd rightEnd = new RelationshipEnd(childEndName, child, true, fkColumnNames);
      parent.addConstraintEnd(leftEnd);
      child.addConstraintEnd(rightEnd);
      return addFKConstraint(name, leftEnd, rightEnd);
   }

   /**
    * Add a foreign key constraint referencing the primary key of the parent
    * @param name the name of the constraint
    * @param parentEndName a name used to identify this constraint from the parent
    * @param parent the parent Table
    * @param fkColumnNames column names in the child table in this constraint
    * @param childEndName a name used to identify this constraint from the child
    * @param child the child Table
    * @return a Relationship describing this constraint
    */
   public Relationship addFKConstraint(String name, String parentEndName, Table parent, String[] fkColumnNames, String childEndName, Table child)
   {
      RelationshipEnd leftEnd = new RelationshipEnd(parentEndName, parent, true, fkColumnNames);
      RelationshipEnd rightEnd = new RelationshipEnd(childEndName, child, false, child.getPkFields());
      parent.addConstraintEnd(leftEnd);
      child.addConstraintEnd(rightEnd);
      return addFKConstraint(name, leftEnd, rightEnd);
   }

   private Relationship addFKConstraint(String name, RelationshipEnd leftEnd, RelationshipEnd rightEnd)
   {
      Relationship cons = new Relationship(name, leftEnd, rightEnd);
      constraints.put(cons.getName(), cons);
      return cons;
   }

   public Relationship getConstraintByName(String name)
   {
      return (Relationship) constraints.get(name);
   }
}
