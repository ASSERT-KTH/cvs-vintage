/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * A basic implementation of <code>ICompilationUnit</code>
 * for use in the <code>SourceMapper</code>.
 * @see ICompilationUnit
 */
public class BasicCompilationUnit implements ICompilationUnit {
	protected char[] contents;
	protected char[] fileName;
	protected char[][] packageName;
	protected char[] mainTypeName;
	protected String encoding;
	
public BasicCompilationUnit(char[] contents, char[][] packageName, String fileName, String encoding) {
	this.contents = contents;
	this.fileName = fileName.toCharArray();
	this.packageName = packageName;

	int start = fileName.lastIndexOf("/") + 1; //$NON-NLS-1$
	if (start == 0 || start < fileName.lastIndexOf("\\")) //$NON-NLS-1$
		start = fileName.lastIndexOf("\\") + 1; //$NON-NLS-1$

	int end = fileName.lastIndexOf("."); //$NON-NLS-1$
	if (end == -1)
		end = fileName.length();

	this.mainTypeName = fileName.substring(start, end).toCharArray();
	this.encoding = encoding;
}
public char[] getContents() {
	if (this.contents != null)
		return this.contents;   // answer the cached source

	// otherwise retrieve it
	try {
		return Util.getFileCharContent(new File(new String(this.fileName)), this.encoding);
	} catch (IOException e) {
		// could not read file: returns an empty array
	}
	return CharOperation.NO_CHAR;
}
public char[] getFileName() {
	return this.fileName;
}
public char[] getMainTypeName() {
	return this.mainTypeName;
}
public char[][] getPackageName() {
	return this.packageName;
}
public String toString(){
	return "CompilationUnit: "+new String(this.fileName); //$NON-NLS-1$
}
}
