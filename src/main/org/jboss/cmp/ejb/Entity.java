/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejb;

import org.jboss.persistence.schema.AbstractClass;
import org.jboss.persistence.schema.AbstractType;

public abstract class Entity implements AbstractClass
{
   protected String ejbName;

   protected Entity(String ejbName)
   {
      this.ejbName = ejbName;
   }

   public String getName()
   {
      return ejbName;
   }

   public int getFamily()
   {
      return AbstractType.OBJECT;
   }
}
