package org.columba.core.util;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 * 
 */
public class Interval {

	public int a, b;
	public int type;

	public Interval(int a, int b) {
		this.a = a;
		this.b = b;
	}

	public Interval() {
		a = -1;
		b = -1;
	}

	public void reset() {
		a = -1;
		b = -2;
	}

}
