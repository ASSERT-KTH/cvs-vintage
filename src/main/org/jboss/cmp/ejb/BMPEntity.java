/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;

public class BMPEntity extends Entity
{
   public BMPEntity(String ejbName)
   {
      super(ejbName);
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
