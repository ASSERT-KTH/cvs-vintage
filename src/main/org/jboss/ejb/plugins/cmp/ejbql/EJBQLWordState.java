package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;
import java.io.CharArrayWriter;

public class EJBQLWordState implements TokenizerState {

	public EJBQLWordState() {
	}

	public Token nextToken(PushbackReader in, char character, Tokenizer tokenizer)
			throws IOException {
		if(!isEJBQLIdentifierStart(character)) {
			throw new IllegalArgumentException("EJBQLWordState must begin with a valid identified start: c="+character);
		}
		if(in == null) {
			throw new IllegalArgumentException("in is null");
		}
		
		CharArrayWriter out = new CharArrayWriter(16);
		
		out.write(character);

		// read all chars in identifier
		int c = in.read();
		while(isEJBQLIdentifierPart((char)c)) {
			out.write(c);
			c = in.read();
		}
		
		// unread last char as it is not part of the identifier
		if(c != -1) {
			in.unread(c);
		}

		return new WordToken(out.toString());
	}
	
	private boolean isEJBQLIdentifierStart(char c) {
		return Character.isJavaIdentifierStart(c) || c=='?';
	}

	private boolean isEJBQLIdentifierPart(char c) {
		return Character.isJavaIdentifierPart(c) && c!='?';
	}
}
