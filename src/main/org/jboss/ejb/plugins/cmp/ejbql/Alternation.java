package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Alternation extends Parser {
   private List parsers = new ArrayList();

   public Alternation() {
   }

   public Alternation add(Parser parser) {
      parsers.add(parser);
      return this;
   }
   
   public AssemblySet match(AssemblySet inSet) {
      AssemblySet outSet = new AssemblySet();

      // call each subparser with the input set appending
      // the result assemblies to the out set until all have
      // been called
      for(Iterator i=parsers.iterator(); i.hasNext(); ) {
         Parser parser = (Parser)i.next();
         outSet.addAll(parser.matchAndAssemble(inSet));
      }
      return outSet;
   }
}
