/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class FloatConstant extends Constant {
	
	float value;
	
	public FloatConstant(float value) {
		this.value = value;
	}
	
	public byte byteValue() {
		return (byte) value;
	}
	
	public char charValue() {
		return (char) value;
	}
	
	public double doubleValue() {
		return (double) value;
	}
	
	public float floatValue() {
		return (float) value;
	}
	
	public int intValue() {
		return (int) value;
	}
	
	public long longValue() {
		return (long) value;
	}
	
	public short shortValue() {
		return (short) value;
	}
	
	public String stringValue() {
		String s = Float.toString(value);
		if (s == null)
			return "null"; //$NON-NLS-1$
		else
			return s;
	}

	public String toString() {
		return "(float)" + value; //$NON-NLS-1$
	} 

	public int typeID() {
		return T_float;
	}
}