/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractType;

public class Path extends BaseNode
{
   private final NamedRelation root;
   private final List steps;
   private AbstractType type;
   private boolean collection;

   public Path(NamedRelation root)
   {
      this.root = root;
      steps = new ArrayList();
      type = root.getType();
      collection = true;
   }

   public NamedRelation getRoot()
   {
      return root;
   }

   public void addStep(AbstractAttribute attr)
   {
      steps.add(attr);
      type = attr.getType();
      collection = false;
   }

   public void addStep(AbstractAssociationEnd end)
   {
      steps.add(end);
      type = end.getPeer().getType();
      collection = end.getPeer().isCollection();
   }

   public Iterator listSteps()
   {
      return steps.iterator();
   }

   public AbstractType getType()
   {
      return type;
   }

   public boolean isCollection()
   {
      return collection;
   }

   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append(root.getAlias());
      for (Iterator i = steps.iterator(); i.hasNext();)
      {
         buf.append(".").append(i.next());
      }
      return buf.toString();
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
