package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.Iterator;

public abstract class Terminal extends Parser {
	private boolean discardTokens = false;
	private boolean debug = false;

	public Terminal() {
	}
	
	public boolean getDiscardTokens() {
		return discardTokens;
	}
	
	public void setDiscardTokens(boolean discardTokens) {
		this.discardTokens = discardTokens;
	}
	
	public Terminal discard() {
		discardTokens = true;
		return this;
	}
	
	public Terminal debug() {
		debug = true;
		return this;
	}

	public AssemblySet match(AssemblySet inSet) {
		AssemblySet out = new AssemblySet();
		for(Iterator i = inSet.iterator(); i.hasNext(); ) {
			Assembly a = match((Assembly)i.next());
			if(a != null) {
				out.add(a);
			}
		}
		return out;
	}

	protected Assembly match(Assembly assembly) {
		// if we have more tokens and the next token is valid
		if(assembly.hasNextToken() && 
				isValidToken(assembly.peekToken())) {
					
			// copy the assembly
			Assembly out = new Assembly(assembly);
			
			// get the next token
			Token token = out.nextToken();
			
			// if we are not discarding the tokens
			if(!discardTokens) {
				// push it on the output stack
				out.push(token);
			}
			// return the new assembly
			if(debug)System.out.println("Terminal "+this+" matched. newAssembly="+out);
			return out;
		}
		// no match return null
		if(debug)System.out.println("Terminal "+this+" DID NOT match assembly="+assembly);
		return null;
	}
	
	protected abstract boolean isValidToken(Token token);
}
