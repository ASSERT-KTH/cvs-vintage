/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Class used to represent a Query against a schema. This represents the query
 * in terms of basic relational operators: relations, projections and filters.
 * This can be manipulated to change the behaviour of the query, or to map
 * a abstract query down to operations supported by the underlying store.
 */
public class Query
{
   private final Map relations;
   private final List relationAliases;
   private final List projections;
   private boolean distinct;

   /**
    * Constructor for a basic query.
    */
   public Query()
   {
      relations = new HashMap();
      relationAliases = new ArrayList();
      projections = new ArrayList();
   }

   /**
    * Private constructor used to map a query from one schema to another
    * @param oldQuery the query being mapped
    * @param schemaMap the mapping between the two schemas
    */
   private Query(Query oldQuery, Map schemaMap)
   {
      this.distinct = oldQuery.distinct;
      this.relations = new HashMap(oldQuery.relations.size());
      this.relationAliases = new ArrayList(oldQuery.relationAliases.size());
      this.projections = new ArrayList(oldQuery.projections.size());

      Map relationMap = new HashMap(relations.size());
      for (Iterator i = oldQuery.relationAliases.iterator(); i.hasNext();)
      {
         String alias = (String) i.next();
         Relation oldRelation = (Relation) oldQuery.relations.get(alias);
         Relation newRelation = oldRelation.mapSchema(schemaMap, relationMap);
         addRelation(newRelation);
      }
      for (Iterator i = oldQuery.projections.iterator(); i.hasNext();)
      {
         Path oldNavigation = (Path) i.next();
         addProjection(oldNavigation.mapSchema(schemaMap, relationMap));
      }
   }

   /**
    * Add a Relation to this query. <br />
    * In SQL terms, this would be member of the FROM clause
    * @param relation
    */
   public void addRelation(Relation relation)
   {
      String alias = relation.getName();
      relationAliases.add(alias);
      relations.put(alias, relation);
   }

   /**
    * Return the list of aliases for all the Relations in this query. This
    * is guaranteed to be in the join order.
    * @return a List(String) of the aliases for each relation
    */
   public List getAliases()
   {
      return Collections.unmodifiableList(relationAliases);
   }

   /**
    * Return a specific Relation identified by its alias
    * @param alias the alias used to identify the Relation
    * @return the Relation associated with the alias
    */
   public Relation getRelation(String alias)
   {
      return (Relation) relations.get(alias);
   }

   /**
    * Add a Projection to the Query. Each tuple returned by the query will
    * comprise a element from each Projection. <br />
    * In SQL terms, this would be an entry in the SELECT list.
    * @param projection a Path representing the Projection to add
    */
   public void addProjection(Path projection)
   {
      projections.add(projection);
   }

   /**
    * Return a list of Projections in the order they were added.
    * @return a List(Path) of projections
    */
   public List getProjections()
   {
      return Collections.unmodifiableList(projections);
   }

   /**
    * Whether this Query returns duplicate tuples
    * @return true if the Query will eliminate duplicate tuples
    */
   public boolean isDistinct()
   {
      return distinct;
   }

   /**
    * Set whether this Query should eliminate duplicate tuples from its result.
    * @param distinct set true to eliminate duplicates
    */
   public void setDistinct(boolean distinct)
   {
      this.distinct = distinct;
   }

   /**
    * Translate this Query to a new one against a different schema using the
    * supplied mapping. Each entry in the map should be keyed on objects in
    * this query's schema and return the equivilent schema object in the other
    * schema. All objects referenced by this query should be included.
    * @param schemaMap a Map used to map from this query's schema to another
    * @return a new Query that performs the same operations on another schema
    */
   public Query mapSchema(Map schemaMap)
   {
      Query mapped = new Query(this, schemaMap);
      return mapped;
   }

   /**
    * Reformulate this query so that all path-based Projections are converted
    * to simple Class.Attribute projections, introducing any joins needed to
    * unnest the paths.
    * @return
    */
   public Query convertPathsToJoins()
   {
      Query newQuery = new Query();
      newQuery.distinct = this.distinct;
      int aliasId = 1;
      for (Iterator i = relationAliases.iterator(); i.hasNext();)
      {
         String alias = (String) i.next();
         Relation relation = (Relation) relations.get(alias);
         if (relation instanceof RangeRelation)
         {
            newQuery.addRelation(relation);
         }
         else
         {
            CollectionRelation col = (CollectionRelation) relation;
            Path path = col.getPath();
            Relation left = path.getRoot();
            Iterator steps = path.listSteps();
            while (steps.hasNext())
            {
               AbstractAssociationEnd rightEnd = (AbstractAssociationEnd) steps.next();
               String newAlias = steps.hasNext() ? "R" + aliasId++ : col.getName();
               JoinRelation join = new JoinRelation(newAlias, left, rightEnd);
               newQuery.addRelation(join);
               left = join;
            }
         }
      }
      for (Iterator i = projections.iterator(); i.hasNext();)
      {
         Path path = (Path) i.next();
         newQuery.addProjection(path);
      }
      return newQuery;
   }
}
