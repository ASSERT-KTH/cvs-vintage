package org.jboss.ejb.plugins.cmp.ejbql;

public final class InputParameterToken implements Token {
	private final int num;
	
	public InputParameterToken(int num) {
		this.num = num;
	}
	
	public int hashCode() {
		return 37 * 17 + num;
	}
	
	public boolean equals(Object o) {
		if(o instanceof InputParameterToken) {
			return ((InputParameterToken)o).num == num;
		}
		return false;
	}
	
	public String toString() {
		return "?"+num;
	}
}
