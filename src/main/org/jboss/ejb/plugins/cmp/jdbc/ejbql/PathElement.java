package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

public abstract class PathElement {
   protected final EntityPathElement parent;

   public PathElement(EntityPathElement parent) {
      this.parent = parent;
   }

   public EntityPathElement getParent() {
      return parent;
   }   

   public abstract String getName();
   public abstract Class getFieldType();
}
