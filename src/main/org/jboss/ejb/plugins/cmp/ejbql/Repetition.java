package org.jboss.ejb.plugins.cmp.ejbql;

public class Repetition extends Parser {
	private Parser subparser;
	
	public Repetition(Parser subparser) {
		this.subparser = subparser;
	}

	public AssemblySet match(AssemblySet inSet) {
		// initialize output set with a copy of the
	   // input set. This supports the zero repete property.
		AssemblySet outSet = new AssemblySet(inSet);
		
		// call the subparser repetely until it doesn't match
		// after each match add the matched assemblies to the output set
		while(!inSet.isEmpty()) {
			inSet = subparser.matchAndAssemble(inSet);
			outSet.addAll(inSet);
		}
		return outSet;
	}
	
}
