package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Sequence extends Parser {
	private List parsers = new ArrayList();

	public Sequence() {
	}

	public Sequence add(Parser parser) {
		parsers.add(parser);
		return this;
	}
	
	public AssemblySet match(AssemblySet inSet) {
		AssemblySet outSet = new AssemblySet(inSet);

		// call each subparser in order until all have been called
		// or there are no assemblies left to match
		for(Iterator i=parsers.iterator(); i.hasNext() && !outSet.isEmpty(); ) {
			Parser parser = (Parser)i.next();
			outSet = parser.matchAndAssemble(outSet);
		}
		return outSet;
	}
}
