package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;

/**
 * Note: this is not a robust implementation of a SymbolState and only works because
 * the ejb-ql symbols are simple
 * 
 */
public class SymbolState implements TokenizerState {

	public SymbolState() {
	}

	public Token nextToken(PushbackReader in, char character, Tokenizer tokenizer)
			throws IOException {
		if(character == '<') {
			// check for <> or <=
			int nextChar = in.read();
			if(nextChar == '=') {
				return new SymbolToken("<=");
			} else if(nextChar == '>') {
				return new SymbolToken("<>");
			} else {
				// nope, unread nextChar
				if(nextChar != -1) {
					in.unread(nextChar);
				}
			} 
		} else if(character == '>') {
			// check for >=
			int nextChar = in.read();
			if(nextChar == '=') {
				return new SymbolToken(">=");
			} else {
				// nope, unread nextChar
				if(nextChar != -1) {
					in.unread(nextChar);
				}
			}
		}
		return new SymbolToken(""+character);
	}
}
