/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractType;

public class CMPField implements AbstractAttribute
{
   private String name;
   private AbstractType type;

   public CMPField(String name, AbstractType type)
   {
      this.name = name;
      this.type = type;
   }

   public String getName()
   {
      return name;
   }

   public AbstractType getType()
   {
      return type;
   }

   public String toString()
   {
      return name;
   }
}
