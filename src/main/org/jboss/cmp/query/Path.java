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

public class Path extends Expression
{
   private final NamedRelation root;
   private final List steps;
   private boolean collection;
   private Object lastStep;

   public Path(NamedRelation root)
   {
      super(root.getType());
      this.root = root;
      steps = new ArrayList();
      collection = true;
   }

   public Path(NamedRelation root, AbstractAttribute attr)
   {
      this(root);
      this.addStep(attr);
   }

   public NamedRelation getRoot()
   {
      return root;
   }

   public void addStep(AbstractAttribute attr)
   {
      steps.add(attr);
      lastStep = attr;
      type = attr.getType();
      collection = false;
   }

   public void addStep(AbstractAssociationEnd end)
   {
      steps.add(end);
      lastStep = end;
      type = end.getPeer().getType();
      collection = end.getPeer().isCollection();
   }

   public Iterator listSteps()
   {
      return steps.iterator();
   }

   public Object getLastStep()
   {
      return lastStep;
   }

   public boolean isCollection()
   {
      return collection;
   }

   public String toString()
   {
      return toString(".");
   }

   public String toString(String delim)
   {
      StringBuffer buf = new StringBuffer();
      buf.append(root.getAlias());
      for (Iterator i = steps.iterator(); i.hasNext();)
      {
         buf.append(delim).append(i.next());
      }
      return buf.toString();
   }

   public Object accept(QueryVisitor visitor, Object param)
   {
      return visitor.visit(this, param);
   }
}
