package org.jboss.ejb.plugins.cmp.ejbql;

public class Symbol extends Terminal {
	private SymbolToken symbol;

	public Symbol(String symbolString) {
		if(symbolString == null) {
			throw new IllegalArgumentException("symbolString is null");
		}
		symbol = new SymbolToken(symbolString);
	}

	protected boolean isValidToken(Token token) {
		return symbol.equals(token);
	}
}
