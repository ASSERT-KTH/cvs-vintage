/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.persistence.schema.AbstractAssociationEnd;
import org.jboss.persistence.schema.AbstractAttribute;
import org.jboss.persistence.schema.AbstractType;

public class BMPEntity extends Entity
{
   public BMPEntity(String ejbName, AbstractType pkClass)
   {
      super(ejbName, pkClass);
   }

   public AbstractAttribute getAttributeByName(String name)
   {
      throw new UnsupportedOperationException();
   }

   public AbstractAssociationEnd getAssocationByName(String name)
   {
      throw new UnsupportedOperationException();
   }
}
