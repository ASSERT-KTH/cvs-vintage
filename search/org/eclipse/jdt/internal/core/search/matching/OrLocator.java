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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

public class OrLocator extends PatternLocator {

protected PatternLocator[] patternLocators;

public OrLocator(OrPattern pattern) {
	super(pattern);

	SearchPattern[] patterns = pattern.patterns;
	int length = patterns.length;
	this.patternLocators = new PatternLocator[length];
	for (int i = 0; i < length; i++)
		this.patternLocators[i] = PatternLocator.patternLocator(patterns[i]);
}
public void match(AstNode node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(ConstructorDeclaration node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(Expression node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(FieldDeclaration node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(MethodDeclaration node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(MessageSend node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(Reference node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(TypeDeclaration node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
public void match(TypeReference node, MatchingNodeSet nodeSet) {
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		this.patternLocators[i].match(node, nodeSet);
}
protected int matchContainer() {
	int result = 0;
	for (int i = 0, length = this.patternLocators.length; i < length; i++)
		result |= this.patternLocators[i].matchContainer();
	return result;
}
protected void matchLevelAndReportImportRef(ImportReference importRef, Binding binding, MatchLocator locator) throws CoreException {
	PatternLocator closestPattern = null;
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].resolveLevel(binding);
		if (newLevel > level) {
			closestPattern = this.patternLocators[i];
			if (newLevel == ACCURATE_MATCH) break;
			level = newLevel;
		}
	}
	if (closestPattern != null)
		closestPattern.matchLevelAndReportImportRef(importRef, binding, locator);
}
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	PatternLocator closestPattern = null;
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].matchLevel(importRef);
		if (newLevel > level) {
			closestPattern = this.patternLocators[i];
			if (newLevel == ACCURATE_MATCH) break;
			level = newLevel;
		}
	}
	if (closestPattern != null)
		closestPattern.matchReportImportRef(importRef, binding, element, accuracy, locator);
}
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	PatternLocator closestPattern = null;
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].resolveLevel(reference);
		if (newLevel > level) {
			closestPattern = this.patternLocators[i];
			if (newLevel == ACCURATE_MATCH) break;
			level = newLevel;
		}
	}
	if (closestPattern != null)
		closestPattern.matchReportReference(reference, element, accuracy, locator);
}
public int resolveLevel(AstNode node) {
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].resolveLevel(node);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel; // want to answer the stronger match
		}
	}
	return level;
}
public int resolveLevel(Binding binding) {
	int level = IMPOSSIBLE_MATCH;
	for (int i = 0, length = this.patternLocators.length; i < length; i++) {
		int newLevel = this.patternLocators[i].resolveLevel(binding);
		if (newLevel > level) {
			if (newLevel == ACCURATE_MATCH) return ACCURATE_MATCH;
			level = newLevel; // want to answer the stronger match
		}
	}
	return level;
}
}
