/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.persistence.schema.AbstractAssociation;
import org.jboss.persistence.schema.AbstractAssociationEnd;

public class EJBRelation implements AbstractAssociation
{
   private String name;
   private CMRField leftEnd;
   private CMRField rightEnd;

   public EJBRelation(String name, CMRField leftEnd, CMRField rightEnd)
   {
      this.name = name;
      this.leftEnd = leftEnd;
      this.rightEnd = rightEnd;
      this.leftEnd.setPeer(this, rightEnd);
      this.rightEnd.setPeer(this, leftEnd);
   }

   public String getName()
   {
      return name;
   }

   public AbstractAssociationEnd getLeftEnd()
   {
      return leftEnd;
   }

   public AbstractAssociationEnd getRightEnd()
   {
      return rightEnd;
   }
}
