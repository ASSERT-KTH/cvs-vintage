package org.jboss.ejb.plugins.cmp.ejbql;

public final class StringToken implements Token {
	private final String value;
	
	public StringToken(String value) {
		this.value = value;
	}
	
	public int hashCode() {
		return 37 * 17 + value.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o instanceof StringToken) {
			return ((StringToken)o).value.equals(value);
		}
		return false;
	}
	
	public String toString() {
		return value;
	}
}
