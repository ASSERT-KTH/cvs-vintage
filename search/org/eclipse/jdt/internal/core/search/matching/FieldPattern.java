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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;

public class FieldPattern extends VariablePattern {

// declaring type
protected char[] declaringQualification;
protected char[] declaringSimpleName;

// type
protected char[] typeQualification;
protected char[] typeSimpleName;

protected static char[][] REF_CATEGORIES = { FIELD_REF, REF };
protected static char[][] REF_AND_DECL_CATEGORIES = { FIELD_REF, REF, FIELD_DECL };
protected static char[][] DECL_CATEGORIES = { FIELD_DECL };

public static char[] createIndexKey(char[] fieldName) {
	return fieldName != null ? fieldName : CharOperation.NO_CHAR;
}

public FieldPattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	char[] name, 
	char[] declaringQualification,
	char[] declaringSimpleName,	
	char[] typeQualification, 
	char[] typeSimpleName,
	int matchRule) {

	super(FIELD_PATTERN, findDeclarations, readAccess, writeAccess, name, matchRule);

	this.declaringQualification = this.isCaseSensitive ? declaringQualification : CharOperation.toLowerCase(declaringQualification);
	this.declaringSimpleName = this.isCaseSensitive ? declaringSimpleName : CharOperation.toLowerCase(declaringSimpleName);
	this.typeQualification = this.isCaseSensitive ? typeQualification : CharOperation.toLowerCase(typeQualification);
	this.typeSimpleName = this.isCaseSensitive ? typeSimpleName : CharOperation.toLowerCase(typeSimpleName);

	this.mustResolve = mustResolve();
}
public void decodeIndexKey(char[] key) {
	this.name = key;
}
public char[] encodeIndexKey() {
	return encodeIndexKey(this.name);
}
public SearchPattern getBlankPattern() {
	return new FieldPattern(false, false, false, null, null, null, null, null, R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getMatchCategories() {
	return this.findReferences
			? (this.findDeclarations || this.writeAccess ? REF_AND_DECL_CATEGORIES : REF_CATEGORIES)
			: DECL_CATEGORIES;
}
public boolean matchesDecodedPattern(SearchPattern decodedPattern) {
	return matchesName(this.name, ((FieldPattern) decodedPattern).name);
}
/**
 * Returns whether a method declaration or message send will need to be resolved to 
 * find out if this method pattern matches it.
 */
protected boolean mustResolve() {
	if (this.declaringSimpleName != null || this.declaringQualification != null) return true;
	if (this.typeSimpleName != null || this.typeQualification != null) return true;

	return super.mustResolve();
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "FieldCombinedPattern: " //$NON-NLS-1$
			: "FieldDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("FieldReferencePattern: "); //$NON-NLS-1$
	}
	if (declaringQualification != null) buffer.append(declaringQualification).append('.');
	if (declaringSimpleName != null) 
		buffer.append(declaringSimpleName).append('.');
	else if (declaringQualification != null) buffer.append("*."); //$NON-NLS-1$
	if (name == null) {
		buffer.append("*"); //$NON-NLS-1$
	} else {
		buffer.append(name);
	}
	if (typeQualification != null) 
		buffer.append(" --> ").append(typeQualification).append('.'); //$NON-NLS-1$
	else if (typeSimpleName != null) buffer.append(" --> "); //$NON-NLS-1$
	if (typeSimpleName != null) 
		buffer.append(typeSimpleName);
	else if (typeQualification != null) buffer.append("*"); //$NON-NLS-1$
	buffer.append(", "); //$NON-NLS-1$
	switch(this.matchMode) {
		case EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	buffer.append(this.isCaseSensitive ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}
