/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.cmp.schema.AbstractAssociation;
import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractClass;

public class CMRField implements AbstractAssociationEnd
{
   private EJBRelation relation;
   private String name;
   private Entity type;
   private CMRField peer;
   private boolean collection;

   /* package */
   CMRField(String name, boolean collection, Entity type)
   {
      this.name = name;
      this.collection = collection;
      this.type = type;
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
      return name != null;
   }

   public boolean isCollection()
   {
      return collection;
   }

   public AbstractAssociation getAssociation()
   {
      return relation;
   }

   public AbstractAssociationEnd getPeer()
   {
      return peer;
   }

   /* package */
   void setPeer(EJBRelation relation, CMRField peer)
   {
      this.relation = relation;
      this.peer = peer;
   }

   public String toString()
   {
      return name;
   }
}
