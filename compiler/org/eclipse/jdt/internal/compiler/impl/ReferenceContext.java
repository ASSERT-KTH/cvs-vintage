package org.eclipse.jdt.internal.compiler.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Implementors are valid compilation contexts from which we can
 * escape in case of error:
 *	i.e. method | type | compilation unit
 */

import org.eclipse.jdt.internal.compiler.CompilationResult;

public interface ReferenceContext {
	void abort(int abortLevel);
	CompilationResult compilationResult();
	void tagAsHavingErrors();
	boolean hasErrors();
}
