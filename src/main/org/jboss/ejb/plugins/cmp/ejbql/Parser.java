package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.Iterator;

public abstract class Parser {
	private Assembler assembler;

	public Parser() {
	}

	public abstract AssemblySet match(AssemblySet in);
	
	public AssemblySet matchAndAssemble(AssemblySet in) {
		AssemblySet out = match(in);
		if(assembler != null) {
			for(Iterator i = out.iterator(); i.hasNext(); ) {
				assembler.workOn((Assembly)i.next());
			}
		}
		return out;
	}
	
	public Assembly bestMatch(Assembly a) {
		AssemblySet set = new AssemblySet();
		set.add(a);
		set = matchAndAssemble(set);
		return best(set);
	}
	
	public Assembly completeMatch(Assembly a) {
		Assembly best = bestMatch(a);
		if(best == null || best.hasNextToken()) {
			return null;
		}
		return best;
	}
	
	public void setAssembler(Assembler a) {
		this.assembler = assembler;
	}
	
	private Assembly best(AssemblySet set) {
		Assembly best = null;
		for(Iterator i = set.iterator(); i.hasNext(); ) {
			Assembly a = (Assembly) i.next();

			// is this a complete match, can't get better then that
			if(!a.hasNextToken()) {
				return a;
			}
			
			// do we have a best
			if(best == null) {
				best = a;
			} else {
				// did a match more tokens then the current best
				if(a.getTokensUsed() > best.getTokensUsed()) {
					best = a;
				}
			}
		}
		return best;
	}	
}
