/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/


package org.jboss.cmp.query;

import java.util.Iterator;
import java.util.Map;

import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractClass;

/**
 * Transforms a query against one schema into a query against another.
 */
public class SchemaMapper extends QueryCloner
{
   private Map map;

   /**
    * Constructor taking the Map used to define the mapping between schemas.
    * @param map a Map mapping objects in the source schema to those in the
    *            target schema
    */
   public SchemaMapper(Map map)
   {
      this.map = map;
   }

   private Object map(Object entry)
   {
      Object mapped = map.get(entry);
      if (mapped == null)
      {
         throw new InternalNoMapException(entry);
      }
      return mapped;
   }

   /**
    * Transform the query into the target schema.
    * @param query a query against the source schema
    * @return an equivalent query against the target schema
    * @throws UnmappedEntryException if an entry cannot be mapped
    */
   public Query map(Query query) throws UnmappedEntryException {
      try
      {
         return (Query) query.accept(this, null);
      }
      catch (InternalNoMapException e)
      {
         throw new UnmappedEntryException("No map entry found for "+e.cause, e.cause);
      }
   }

   public Object visit(Path path, Object param)
   {
      Path newPath = new Path((NamedRelation) path.getRoot().accept(this, null));
      for (Iterator i = path.listSteps(); i.hasNext();)
      {
         Object o = map(i.next());
         if (o instanceof AbstractAttribute)
         {
            newPath.addStep((AbstractAttribute) o);
         }
         else if (o instanceof AbstractAssociationEnd)
         {
            newPath.addStep((AbstractAssociationEnd) o);
         }
      }
      return newPath;
   }

   public Object visit(RangeRelation relation, Object param)
   {
      return new RangeRelation(relation.getAlias(), (AbstractClass) map(relation.getType()));
   }

   private class InternalNoMapException extends RuntimeException {
      private Object cause;

      public InternalNoMapException(Object cause)
      {
         this.cause = cause;
      }
   }
}
