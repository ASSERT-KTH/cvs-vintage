/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.persistence.schema.AbstractType;

public class JavaType implements AbstractType
{
   private final Class clazz;
   private final AbstractType.Family family;

   public JavaType(Class clazz, AbstractType.Family family)
   {
      this.clazz = clazz;
      this.family = family;
   }

   public Class getJavaClass()
   {
      return clazz;
   }

   public String getName()
   {
      return clazz.getName();
   }

   public AbstractType.Family getFamily()
   {
      return family;
   }

   public boolean equals(Object o)
   {
      if (o instanceof JavaType) {
         return clazz.equals(((JavaType)o).clazz);
      } else {
         return false;
      }
   }

   public int hashCode()
   {
      return clazz.hashCode();
   }
}
