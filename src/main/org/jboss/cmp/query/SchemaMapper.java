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
import org.jboss.cmp.schema.AbstractType;

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

   public Object visit(Query query, Object param)
   {
      AbstractType[] oldParams = query.getParameters();
      AbstractType[] params = null;
      if (oldParams != null)
      {
         params = new AbstractType[oldParams.length];
         for (int i = 0; i < oldParams.length; i++)
         {
            params[i] = (AbstractType) map(oldParams[i]);
         }
      }
      Query newQuery = new Query(params);
      newQuery.setRelation((Relation) query.getRelation().accept(this, newQuery));
      newQuery.setProjection((Projection) query.getProjection().accept(this, newQuery));
      if (query.getFilter() != null)
         newQuery.setFilter((QueryNode) query.getFilter().accept(this, newQuery));
      return newQuery;
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

   public Object visit(InnerJoin join, Object param)
   {
      return new InnerJoin((Relation) join.getLeft().accept(this, param),
                           (NamedRelation) join.getJoin().accept(this, param),
                           (Relation) join.getRight().accept(this, param),
                           (AbstractAssociationEnd)map(join.getAssociationEnd()));
   }

   private class InternalNoMapException extends RuntimeException {
      private Object cause;

      public InternalNoMapException(Object cause)
      {
         this.cause = cause;
      }
   }
}
