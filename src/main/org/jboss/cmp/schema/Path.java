/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Path
{
   private final Relation root;
   private final List steps;
   private AbstractType type;
   private boolean collection;

   public Path(Relation root)
   {
      this.root = root;
      steps = new ArrayList();
      type = root.getType();
      collection = true;
   }

   private Path(Path oldNav, Map schemaMap, Map relationMap)
   {
      this.root = (Relation) relationMap.get(oldNav.root);
      this.steps = new ArrayList(oldNav.steps.size());
      for (Iterator i = oldNav.steps.iterator(); i.hasNext();)
      {
         steps.add(schemaMap.get(i.next()));
      }
      this.type = (AbstractType) schemaMap.get(oldNav.type);
      this.collection = oldNav.collection;
   }

   public Relation getRoot()
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
      buf.append(root.getName());
      for (Iterator i = steps.iterator(); i.hasNext();)
      {
         buf.append(".").append(i.next());
      }
      return buf.toString();
   }

   public Path mapSchema(Map schemaMap, Map relationMap)
   {
      return new Path(this, schemaMap, relationMap);
   }
}
