package org.jboss.ejb.plugins.cmp.ejbql;

public final class WordToken implements Token {
	private final String value;
	
	public WordToken(String value) {
		this.value = value;
	}
	
	public int hashCode() {
		return 37 * 17 + value.hashCode();
	}
	
	public boolean equals(Object o) {
		if(o instanceof WordToken) {
			return ((WordToken)o).value.equals(value);
		}
		return false;
	}
	
	public String toString() {
		return value;
	}
}
