package org.jboss.ejb.plugins.cmp.ejbql;

public class StringLiteral extends Terminal {

	public StringLiteral() {
	}

	protected boolean isValidToken(Token token) {
		return token instanceof StringToken;
	}
}
