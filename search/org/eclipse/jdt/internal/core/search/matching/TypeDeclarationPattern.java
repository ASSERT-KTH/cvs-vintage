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

import java.io.IOException;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class TypeDeclarationPattern extends JavaSearchPattern implements IIndexConstants {

public char[] simpleName;
public char[] pkg;
public char[][] enclosingTypeNames;

// set to CLASS_SUFFIX for only matching classes 
// set to INTERFACE_SUFFIX for only matching interfaces
// set to ENUM_SUFFIX for only matching enums
// set to ANNOTATION_TYPE_SUFFIX for only matching annotation types
// set to TYPE_SUFFIX for matching both classes and interfaces
public char typeSuffix; 

protected static char[][] CATEGORIES = { TYPE_DECL };

public static char[] createIndexKey(char[] typeName, char[] packageName, char[][] enclosingTypeNames, char typeSuffix) {
	int typeNameLength = typeName == null ? 0 : typeName.length;
	int packageLength = packageName == null ? 0 : packageName.length;
	int enclosingNamesLength = 0;
	if (enclosingTypeNames != null) {
		for (int i = 0, length = enclosingTypeNames.length; i < length;) {
			enclosingNamesLength += enclosingTypeNames[i].length;
			if (++i < length)
				enclosingNamesLength++; // for the '.' separator
		}
	}

	char[] result = new char[typeNameLength + packageLength + enclosingNamesLength + 4];
	int pos = 0;
	if (typeNameLength > 0) {
		System.arraycopy(typeName, 0, result, pos, typeNameLength);
		pos += typeNameLength;
	}
	result[pos++] = SEPARATOR;
	if (packageLength > 0) {
		System.arraycopy(packageName, 0, result, pos, packageLength);
		pos += packageLength;
	}
	result[pos++] = SEPARATOR;
	if (enclosingNamesLength > 0) {
		for (int i = 0, length = enclosingTypeNames.length; i < length;) {
			char[] enclosingName = enclosingTypeNames[i];
			int itsLength = enclosingName.length;
			System.arraycopy(enclosingName, 0, result, pos, itsLength);
			pos += itsLength;
			if (++i < length)
				result[pos++] = '.';
		}
	}
	result[pos++] = SEPARATOR;
	result[pos] = typeSuffix;
	return result;
}

public TypeDeclarationPattern(
	char[] pkg,
	char[][] enclosingTypeNames,
	char[] simpleName,
	char typeSuffix,
	int matchRule) {

	this(matchRule);

	this.pkg = isCaseSensitive() ? pkg : CharOperation.toLowerCase(pkg);
	if (isCaseSensitive() || enclosingTypeNames == null) {
		this.enclosingTypeNames = enclosingTypeNames;
	} else {
		int length = enclosingTypeNames.length;
		this.enclosingTypeNames = new char[length][];
		for (int i = 0; i < length; i++)
			this.enclosingTypeNames[i] = CharOperation.toLowerCase(enclosingTypeNames[i]);
	}
	this.simpleName = isCaseSensitive() ? simpleName : CharOperation.toLowerCase(simpleName);
	this.typeSuffix = typeSuffix;

	((InternalSearchPattern)this).mustResolve = this.pkg != null && this.enclosingTypeNames != null;
}
TypeDeclarationPattern(int matchRule) {
	super(TYPE_DECL_PATTERN, matchRule);
}
/*
 * Type entries are encoded as simpleTypeName / packageName / enclosingTypeName / 'C' or 'I'
 * e.g. Object/java.lang//C
 * e.g. Cloneable/java.lang//I
 * e.g. LazyValue/javax.swing/UIDefaults/C
 */
public void decodeIndexKey(char[] key) {
	int slash = CharOperation.indexOf(SEPARATOR, key, 0);
	this.simpleName = CharOperation.subarray(key, 0, slash);

	int start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.pkg = slash == start ? CharOperation.NO_CHAR : CharOperation.subarray(key, start, slash);

	slash = CharOperation.indexOf(SEPARATOR, key, start = slash + 1);
	if (slash == start) {
		this.enclosingTypeNames = CharOperation.NO_CHAR_CHAR;
	} else {
		char[] names = CharOperation.subarray(key, start, slash);
		this.enclosingTypeNames = CharOperation.equals(ONE_ZERO, names) ? ONE_ZERO_CHAR : CharOperation.splitOn('.', names);
	}

	this.typeSuffix = key[key.length - 1];
}
public SearchPattern getBlankPattern() {
	return new TypeDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
}
public char[][] getIndexCategories() {
	return CATEGORIES;
}
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	TypeDeclarationPattern pattern = (TypeDeclarationPattern) decodedPattern;
	switch(this.typeSuffix) {
		case CLASS_SUFFIX :
		case INTERFACE_SUFFIX :
		case ENUM_SUFFIX :
		case ANNOTATION_TYPE_SUFFIX :
			if (this.typeSuffix != pattern.typeSuffix) return false;
	}

	if (!matchesName(this.simpleName, pattern.simpleName))
		return false;

	// check package - exact match only
	if (this.pkg != null && !CharOperation.equals(this.pkg, pattern.pkg, isCaseSensitive()))
		return false;

	// check enclosingTypeNames - exact match only
	if (this.enclosingTypeNames != null) {
		if (this.enclosingTypeNames.length == 0)
			return pattern.enclosingTypeNames.length == 0;
		if (this.enclosingTypeNames.length == 1 && pattern.enclosingTypeNames.length == 1)
			return CharOperation.equals(this.enclosingTypeNames[0], pattern.enclosingTypeNames[0], isCaseSensitive());
		if (pattern.enclosingTypeNames == ONE_ZERO_CHAR)
			return true; // is a local or anonymous type
		return CharOperation.equals(this.enclosingTypeNames, pattern.enclosingTypeNames, isCaseSensitive());
	}
	return true;
}
EntryResult[] queryIn(Index index) throws IOException {
	char[] key = this.simpleName; // can be null
	int matchRule = getMatchRule();

	switch(getMatchMode()) {
		case R_PREFIX_MATCH :
			// do a prefix query with the simpleName
			break;
		case R_EXACT_MATCH :
			if (this.simpleName != null) {
				matchRule = matchRule - R_EXACT_MATCH + R_PREFIX_MATCH;
				key = this.pkg == null
					? CharOperation.append(this.simpleName, SEPARATOR)
					: CharOperation.concat(this.simpleName, SEPARATOR, this.pkg, SEPARATOR, CharOperation.NO_CHAR);
				break; // do a prefix query with the simpleName and possibly the pkg
			}
			matchRule = matchRule - R_EXACT_MATCH + R_PATTERN_MATCH;
			// fall thru to encode the key and do a pattern query
		case R_PATTERN_MATCH :
			if (this.pkg == null) {
				if (this.simpleName == null) {
					switch(this.typeSuffix) {
						case CLASS_SUFFIX :
						case INTERFACE_SUFFIX :
						case ENUM_SUFFIX :
						case ANNOTATION_TYPE_SUFFIX :
							key = new char[] {ONE_STAR[0],  SEPARATOR,
								isCaseSensitive() ? this.typeSuffix : Character.toLowerCase(this.typeSuffix)}; // find all classes or all interfaces
							break;
					}
				} else if (this.simpleName[this.simpleName.length - 1] != '*') {
					key = CharOperation.concat(this.simpleName, ONE_STAR, SEPARATOR);
				}
				break; // do a pattern query with the current encoded key
			}
			// must decode to check enclosingTypeNames due to the encoding of local types
			key = CharOperation.concat(
				this.simpleName == null ? ONE_STAR : this.simpleName, SEPARATOR, this.pkg, SEPARATOR, ONE_STAR);
			break;
	}

	return index.query(getIndexCategories(), key, matchRule); // match rule is irrelevant when the key is null
}
protected StringBuffer print(StringBuffer output) {
	switch (this.typeSuffix){
		case CLASS_SUFFIX :
			output.append("ClassDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		case INTERFACE_SUFFIX :
			output.append("InterfaceDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		case ENUM_SUFFIX :
			output.append("EnumDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		case ANNOTATION_TYPE_SUFFIX :
			output.append("AnnotationTypeDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
		default :
			output.append("TypeDeclarationPattern: pkg<"); //$NON-NLS-1$
			break;
	}
	if (pkg != null) 
		output.append(pkg);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">, enclosing<"); //$NON-NLS-1$
	if (enclosingTypeNames != null) {
		for (int i = 0; i < enclosingTypeNames.length; i++){
			output.append(enclosingTypeNames[i]);
			if (i < enclosingTypeNames.length - 1)
				output.append('.');
		}
	} else {
		output.append("*"); //$NON-NLS-1$
	}
	output.append(">, type<"); //$NON-NLS-1$
	if (simpleName != null) 
		output.append(simpleName);
	else
		output.append("*"); //$NON-NLS-1$
	output.append(">"); //$NON-NLS-1$
	return super.print(output);
}
}
