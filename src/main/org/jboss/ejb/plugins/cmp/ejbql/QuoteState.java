package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;
import java.io.CharArrayWriter;

public class QuoteState implements TokenizerState {
	private static final char SINGLE_QUOTE = '\'';
	
	public QuoteState() {
	}

	public Token nextToken(PushbackReader in, char character, Tokenizer tokenizer)
			throws IOException {
		if(character != SINGLE_QUOTE) {
			throw new IllegalArgumentException("QuoteState can only tokenize single quoted strings");
		}
		if(in == null) {
			throw new IllegalArgumentException("in is null");
		}
		
		CharArrayWriter out = new CharArrayWriter(16);
		
		// the returned token is quoted
		out.write(SINGLE_QUOTE);
		
		for(int c = in.read(); c != -1; c = in.read()) {
			// check for bad characters
			if(c == '\n' || c == '\r') {
				throw new IOException("Error: Line terminator in string literal");
			}
			
			//
			// Should we check for unicode encoded characters here?
			//
			
			out.write(c);

			// check for end of string
			if(c == SINGLE_QUOTE) {
				// we have a possible end, check if the next character is 
				// a single quote. If it is, we just have an escaped single quote.
				int nextChar = in.read();
				if(nextChar != SINGLE_QUOTE) {
					// unread next char if it is not an end of stream
					if(nextChar != -1) {
						in.unread(nextChar);
					}
					return new StringToken(out.toString());
				} 
			}
		}
		throw new IOException("Error: Unterminated string literal");
	}
}
