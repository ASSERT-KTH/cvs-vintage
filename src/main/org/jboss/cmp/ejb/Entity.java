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
   protected final String ejbName;
   protected final AbstractType pkClass;
   protected boolean unknownPk = false;
   protected boolean identityGenerated = false;

   protected Entity(String ejbName, AbstractType pkClass)
   {
      this.ejbName = ejbName;
      this.pkClass = pkClass;
   }

   public String getName()
   {
      return ejbName;
   }

   public AbstractType.Family getFamily()
   {
      return AbstractType.Family.OBJECT;
   }

   public AbstractType getIdentityType()
   {
      return pkClass;
   }

   public boolean isSystemIdentity()
   {
      return false;
   }

   public boolean isIdentityGenerated()
   {
      return false;
   }
}
