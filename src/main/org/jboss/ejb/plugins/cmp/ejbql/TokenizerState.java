package org.jboss.ejb.plugins.cmp.ejbql;

import java.io.PushbackReader;
import java.io.IOException;

public interface TokenizerState {
	public Token nextToken(PushbackReader reader, char character, Tokenizer tokenizer)
			throws IOException;
}
