package org.jboss.ejb.plugins.cmp.ejbql;

public class Optional extends Parser {
   private Parser subparser;
   
   public Optional(Parser subparser) {
      this.subparser = subparser;
   }

   public AssemblySet match(AssemblySet inSet) {
      // add no match
      AssemblySet outSet = new AssemblySet(inSet);
      // add match
      outSet.addAll(subparser.matchAndAssemble(inSet));
      
      return outSet;
   }
   
}