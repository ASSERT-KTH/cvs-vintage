package org.jboss.ejb.plugins.cmp.ejbql;

public class ExactNumericLiteral implements Token {
	private final long value;
	
	public ExactNumericLiteral(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}
	
	public int hashCode() {
		return 37 * 17 + (int)(value ^ (value >>> 32));
	}
	
	public boolean equals(Object o) {
		if(o instanceof ExactNumericLiteral) {
			return ((ExactNumericLiteral)o).value == value;
		}
		return false;
	}
	
	public String toString() {
		return "" + value;
	}
}
