/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.cmp.schema.AbstractType;

public class JavaType implements AbstractType
{
   private String name;

   public JavaType(Class clazz)
   {
      this.name = clazz.getName();
   }

   public String getName()
   {
      return name;
   }
}
