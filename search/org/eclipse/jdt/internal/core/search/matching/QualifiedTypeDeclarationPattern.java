/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class QualifiedTypeDeclarationPattern extends TypeDeclarationPattern implements IIndexConstants {

protected char[] qualification;

public QualifiedTypeDeclarationPattern(char[] qualification, char[] simpleName, char typeSuffix, int matchRule) {
	this(matchRule);

	this.qualification = isCaseSensitive() ? qualification : CharOperation.toLowerCase(qualification);
	this.simpleName = isCaseSensitive() ? simpleName : CharOperation.toLowerCase(simpleName);
	this.typeSuffix = typeSuffix;

	((InternalSearchPattern)this).mustResolve = this.qualification != null;
}
QualifiedTypeDeclarationPattern(int matchRule) {
	super(matchRule);
}
public void decodeIndexKey(char[] key) {
	int slash = CharOperation.indexOf(SEPARATOR, key, 0);
	this.simpleName = CharOperation.subarray(key, 0, slash);

	int start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	int secondSlash = CharOperation.indexOf(SEPARATOR, key, slash + 1);
	if (start + 1 == secondSlash) {
		this.qualification = CharOperation.NO_CHAR; // no package name or enclosingTypeNames
	} else if (slash + 1 == secondSlash) {
		this.qualification = CharOperation.subarray(key, start, slash); // only a package name
	} else {
		this.qualification = CharOperation.subarray(key, start, secondSlash);
		this.qualification[slash - start] = '.';
	}

	this.typeSuffix = key[key.length - 1];
}
public SearchPattern getBlankPattern() {
	return new QualifiedTypeDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	QualifiedTypeDeclarationPattern pattern = (QualifiedTypeDeclarationPattern) decodedPattern;
	switch(this.typeSuffix) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
		case ENUM_SUFFIX :
		case ANNOTATION_TYPE_SUFFIX :
			if (this.typeSuffix != pattern.typeSuffix) return false;
	}

	return matchesName(this.simpleName, pattern.simpleName) && matchesName(this.qualification, pattern.qualification);
}
protected StringBuffer print(StringBuffer output) {
	switch (this.typeSuffix){
		case CLASS_SUFFIX :
			output.append("ClassDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			output.append("InterfaceDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case ENUM_SUFFIX :
			output.append("EnumDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		case ANNOTATION_TYPE_SUFFIX :
			output.append("AnnotationTypeDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
		default :
			output.append("TypeDeclarationPattern: qualification<"); //$NON-NLS-1$
			break;
	}
	if (this.qualification != null) 
		output.append(this.qualification);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) 
		output.append(simpleName);
	else
		output.append("*"); //$NON-NLS-1$
	return super.print(output);
}
}
