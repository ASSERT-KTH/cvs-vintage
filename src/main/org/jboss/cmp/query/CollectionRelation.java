/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import org.jboss.cmp.schema.AbstractClass;

/**
 * Relation representing a set of tuples obtained by following a Path.
 * Used to unnest collection members of a class.
 */
public class CollectionRelation extends NamedRelation
{
   private final Path path;

   /**
    * Constructor for this collection
    * @param name the alias idenfiying this relation in the query
    * @param path the path to a collection member
    */
   public CollectionRelation(String name, Path path)
   {
      super(name, (AbstractClass) path.getType());
      this.path = path;
   }

   /**
    * Return the path to the collection member
    * @return the path to the collection member
    */
   public Path getPath()
   {
      return path;
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
