/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.schema;

import java.util.Map;

/**
 * Relation representing a set of tuples obtained by following a Path.
 * Used to unnest collection members of a class.
 */
public class CollectionRelation extends Relation
{
   private final Path path;

   /**
    * Constructor for this collection
    * @param name the alias idenfiying this relation in the query
    * @param path the path to a collection member
    */
   public CollectionRelation(String name, Path path)
   {
      super(name);
      this.path = path;
   }

   public AbstractClass getType()
   {
      return (AbstractClass) path.getType();
   }

   /**
    * Return the path to the collection member
    * @return the path to the collection member
    */
   public Path getPath()
   {
      return path;
   }

   public Relation mapSchema(Map schemaMap, Map relationMap)
   {
      CollectionRelation newRelation = new CollectionRelation(getName(), path.mapSchema(schemaMap, relationMap));
      relationMap.put(this, newRelation);
      return newRelation;
   }
}
