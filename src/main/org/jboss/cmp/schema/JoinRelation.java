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
 * Relation representing a (inner) Join operation.
 */
public class JoinRelation extends Relation
{
   private Relation left;
   private AbstractAssociationEnd rightEnd;

   /**
    * Constructor for the Join
    * @param name the alias name of this relation
    * @param left the Relation on the left hand side of the join
    * @param rightEnd the AssociationEnd corresponding to the join
    */
   public JoinRelation(String name, Relation left, AbstractAssociationEnd rightEnd)
   {
      super(name);
      this.left = left;
      this.rightEnd = rightEnd;
   }

   public AbstractClass getType()
   {
      return rightEnd.getPeer().getType();
   }

   /**
    * Return the join condition in the schema's query language
    * @return the query language text for the join condition
    */
   public String getCondition()
   {
      return rightEnd.getAssociation().getJoinCondition(left.getName(), getName());
   }

   public Relation mapSchema(Map schemaMap, Map relationMap)
   {
      JoinRelation newRelation = new JoinRelation(getName(), (Relation) relationMap.get(left), (AbstractAssociationEnd) schemaMap.get(rightEnd));
      relationMap.put(this, newRelation);
      return newRelation;
   }
}
