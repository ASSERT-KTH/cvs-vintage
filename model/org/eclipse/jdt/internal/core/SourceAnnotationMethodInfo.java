/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.compiler.CharOperation;

/*
 * Element info for annotation method from source.
 */
public class SourceAnnotationMethodInfo extends SourceMethodInfo {

	/*
	 * The positions of a default member value of an annotation method.
	 * These are {-1, -1} if the method is an annotation method with no default value.
	 * Otherwise these are the start and end (inclusive) of the expression representing the default value.
	 */
	protected int defaultValueStart;
	protected int defaultValueEnd;

	public boolean isAnnotationMethod() {
		return true;
	}
	
	public char[] getDefaultValueSource(char[] cuSource) {
		if (this.defaultValueStart == -1 && this.defaultValueEnd == -1) 
			return null;
		return CharOperation.subarray(cuSource, this.defaultValueStart, this.defaultValueEnd+1);
	}
}
