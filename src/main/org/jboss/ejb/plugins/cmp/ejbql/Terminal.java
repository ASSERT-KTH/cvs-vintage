package org.jboss.ejb.plugins.cmp.ejbql;

import java.util.Iterator;

public abstract class Terminal extends Parser {
	private boolean discardTokens = false;

	public Terminal() {
	}
	
	public boolean getDiscardTokens() {
		return discardTokens;
	}
	
	public void setDiscardTokens(boolean discardTokens) {
		this.discardTokens = discardTokens;
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
			return out;
		}
		// no match return null
		return null;
	}
	
	protected abstract boolean isValidToken(Token token);
}
