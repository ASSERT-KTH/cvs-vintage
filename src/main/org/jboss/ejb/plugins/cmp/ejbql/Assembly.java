package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Assembly implements DeepCloneable {
	private DeepCloneable target;
	private List stack = new ArrayList();
	private List tokens = new ArrayList();
	private int tokenIndex;

	public Assembly(Assembly a) {
		if(a.target != null) {
			target = (DeepCloneable)a.target.deepClone();
		}
		stack = new ArrayList(a.stack);
		tokens = new ArrayList(a.tokens);
		tokenIndex = a.tokenIndex;
	}
		
	public Assembly(String string) {
		this(new Tokenizer(string));
	}
	
	public Assembly(Reader reader) {
		this(new Tokenizer(reader));
	}
	
	public Assembly(Tokenizer tokenizer) {
		while(tokenizer.hasNext()) {
			tokens.add(tokenizer.nextToken());
		}
		tokenIndex = 0;
	}
	
	public void push(Object object) {
		stack.add(object);
	}

	public Object pop() {
		return stack.remove(stack.size()-1);
	}
	
	public boolean isEmpty() {
		return stack.isEmpty();
	}
	
	public DeepCloneable getTarget() {
		return target;
	}
	
	public void setTarget(DeepCloneable target) {
		this.target = target;
	}
	
	public boolean hasNextToken() {
		return tokenIndex < tokens.size();
	}

	public Token nextToken() {
		return (Token)tokens.get(tokenIndex++);
	}
	
	public Token peekToken() {
		if(hasNextToken()) {
			return (Token)tokens.get(tokenIndex);
		}
		return null;
	}
	
	public int getTokensUsed() {
		return tokenIndex;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer(128);
		
		buf.append(stack);
		for(int i=0; i<tokens.size(); i++) {
			if(i==tokenIndex) {
				buf.append("^");
			} else if(i != 0) {
				buf.append("/");
			}
			buf.append(tokens.get(i));
		}
		if(tokenIndex >= tokens.size()) {
			buf.append("^");
		}
		return buf.toString();
	}
	
	public Object deepClone() {
		return new Assembly(this);
	}
}
