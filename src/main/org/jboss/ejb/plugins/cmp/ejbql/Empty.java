package org.jboss.ejb.plugins.cmp.ejbql;

public class Empty extends Parser {

   public Empty() {
   }

   public AssemblySet match(AssemblySet inSet) {
      return new AssemblySet(inSet);
   }
}
