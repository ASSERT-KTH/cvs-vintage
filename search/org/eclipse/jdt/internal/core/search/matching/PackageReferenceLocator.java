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
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class PackageReferenceLocator extends PatternLocator {

protected PackageReferencePattern pattern;
	
public PackageReferenceLocator(PackageReferencePattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
public int match(ASTNode node, MatchingNodeSet nodeSet) { // interested in ImportReference
	if (!(node instanceof ImportReference)) return IMPOSSIBLE_MATCH;

	return nodeSet.addMatch(node, matchLevel((ImportReference) node));
}
//public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Expression node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(FieldDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
public int match(Reference node, MatchingNodeSet nodeSet) { // interested in QualifiedNameReference
	if (!(node instanceof QualifiedNameReference)) return IMPOSSIBLE_MATCH;

	return nodeSet.addMatch(node, matchLevelForTokens(((QualifiedNameReference) node).tokens));
}
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(TypeReference node, MatchingNodeSet nodeSet) { // interested in QualifiedTypeReference only
	if (!(node instanceof QualifiedTypeReference)) return IMPOSSIBLE_MATCH;

	return nodeSet.addMatch(node, matchLevelForTokens(((QualifiedTypeReference) node).tokens));
}

protected int matchLevel(ImportReference importRef) {
	if (!importRef.onDemand)
		return matchLevelForTokens(importRef.tokens);

	return matchesName(this.pattern.pkgName, CharOperation.concatWith(importRef.tokens, '.'))
		? ACCURATE_MATCH
		: IMPOSSIBLE_MATCH;
}
protected int matchLevelForTokens(char[][] tokens) {
	if (this.pattern.pkgName == null) return ACCURATE_MATCH;

	switch (this.matchMode) {
		case IJavaSearchConstants.EXACT_MATCH:
		case IJavaSearchConstants.PREFIX_MATCH:
			if (CharOperation.prefixEquals(this.pattern.pkgName, CharOperation.concatWith(tokens, '.'), this.isCaseSensitive))
				return POSSIBLE_MATCH;
			break;
		case IJavaSearchConstants.PATTERN_MATCH:
			char[] patternName = this.pattern.pkgName[this.pattern.pkgName.length - 1] == '*'
				? this.pattern.pkgName
				: CharOperation.concat(this.pattern.pkgName, ".*".toCharArray()); //$NON-NLS-1$
			if (CharOperation.match(patternName, CharOperation.concatWith(tokens, '.'), this.isCaseSensitive))
				return POSSIBLE_MATCH;
			break;
	}
	return IMPOSSIBLE_MATCH;
}
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (binding == null) {
		this.matchReportReference(importRef, element, accuracy, locator);
	} else {
		long[] positions = importRef.sourcePositions;
		int last = positions.length - 1;
		if (binding instanceof ProblemReferenceBinding)
			binding = ((ProblemReferenceBinding) binding).original;
		if (binding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) binding).fPackage;
			if (pkgBinding != null)
				last = pkgBinding.compoundName.length;
		}
		locator.report(positions[0], positions[last - 1], element, accuracy);
	}
}
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	long[] positions = null;
	int last = -1;
	if (reference instanceof ImportReference) {
		ImportReference importRef = (ImportReference) reference;
		positions = importRef.sourcePositions;
		last = importRef.onDemand ? positions.length : positions.length - 1;
	} else {
		TypeBinding typeBinding = null;
		if (reference instanceof QualifiedNameReference) {
			QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
			positions = qNameRef.sourcePositions;
			switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
				case BindingIds.FIELD : // reading a field
					typeBinding = qNameRef.actualReceiverType;
					break;
				case BindingIds.TYPE : //=============only type ==============
					if (qNameRef.binding instanceof TypeBinding)
						typeBinding = (TypeBinding) qNameRef.binding;
					break;
				case BindingIds.VARIABLE : //============unbound cases===========
				case BindingIds.TYPE | BindingIds.VARIABLE :
					Binding binding = qNameRef.binding; 
					if (binding instanceof TypeBinding) {
						typeBinding = (TypeBinding) binding;
					} else if (binding instanceof ProblemFieldBinding) {
						typeBinding = qNameRef.actualReceiverType;
						last = qNameRef.tokens.length - (qNameRef.otherBindings == null ? 2 : qNameRef.otherBindings.length + 2);
					} else if (binding instanceof ProblemBinding) {
						ProblemBinding pbBinding = (ProblemBinding) binding;
						typeBinding = pbBinding.searchType;
						last = CharOperation.occurencesOf('.', pbBinding.name);
					}
					break;					
			}
		} else if (reference instanceof QualifiedTypeReference) {
			QualifiedTypeReference qTypeRef = (QualifiedTypeReference) reference;
			positions = qTypeRef.sourcePositions;
			typeBinding = qTypeRef.resolvedType;
		}
		if (typeBinding instanceof ArrayBinding)
			typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
		if (typeBinding instanceof ProblemReferenceBinding)
			typeBinding = ((ProblemReferenceBinding) typeBinding).original;
		if (typeBinding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) typeBinding).fPackage;
			if (pkgBinding != null)
				last = pkgBinding.compoundName.length;
		}
	}
	if (last == -1) {
		last = this.pattern.segments.length;
		if (last > positions.length) last = positions.length;
	}
	locator.report(positions[0], positions[last - 1], element, accuracy);
}
public int resolveLevel(ASTNode node) {
	if (node instanceof QualifiedTypeReference)
		return resolveLevel(((QualifiedTypeReference) node).resolvedType);
	if (node instanceof QualifiedNameReference)
		return this.resolveLevel((QualifiedNameReference) node);
//	if (node instanceof ImportReference) - Not called when resolve is true, see MatchingNodeSet.reportMatching(unit)
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;

	char[][] compoundName = null;
	if (binding instanceof ImportBinding) {
		compoundName = ((ImportBinding) binding).compoundName;
	} else {
		if (binding instanceof ArrayBinding)
			binding = ((ArrayBinding) binding).leafComponentType;
		if (binding instanceof ProblemReferenceBinding)
			binding = ((ProblemReferenceBinding) binding).original;
		if (binding == null) return INACCURATE_MATCH;

		if (binding instanceof ReferenceBinding) {
			PackageBinding pkgBinding = ((ReferenceBinding) binding).fPackage;
			if (pkgBinding == null) return INACCURATE_MATCH;
			compoundName = pkgBinding.compoundName;
		}
	}
	return compoundName != null && matchesName(this.pattern.pkgName, CharOperation.concatWith(compoundName, '.'))
		? ACCURATE_MATCH : IMPOSSIBLE_MATCH;
}
protected int resolveLevel(QualifiedNameReference qNameRef) {
	TypeBinding typeBinding = null;
	switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
		case BindingIds.FIELD : // reading a field
			if (qNameRef.tokens.length < (qNameRef.otherBindings == null ? 3 : qNameRef.otherBindings.length + 3))
				return IMPOSSIBLE_MATCH; // must be at least p1.A.x
			typeBinding = qNameRef.actualReceiverType;
			break;
		case BindingIds.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no package match in it
		case BindingIds.TYPE : //=============only type ==============
			if (qNameRef.binding instanceof TypeBinding)
				typeBinding = (TypeBinding) qNameRef.binding;
			break;
		/*
		 * Handling of unbound qualified name references. The match may reside in the resolved fragment,
		 * which is recorded inside the problem binding, along with the portion of the name until it became a problem.
		 */
		case BindingIds.VARIABLE : //============unbound cases===========
		case BindingIds.TYPE | BindingIds.VARIABLE :
			Binding binding = qNameRef.binding; 
			if (binding instanceof ProblemReferenceBinding) {
				typeBinding = (TypeBinding) binding;
			} else if (binding instanceof ProblemFieldBinding) {
				if (qNameRef.tokens.length < (qNameRef.otherBindings == null ? 3 : qNameRef.otherBindings.length + 3))
					return IMPOSSIBLE_MATCH; // must be at least p1.A.x
				typeBinding = qNameRef.actualReceiverType;
			} else if (binding instanceof ProblemBinding) {
				ProblemBinding pbBinding = (ProblemBinding) binding;
				if (CharOperation.occurencesOf('.', pbBinding.name) <= 0) // index of last bound token is one before the pb token
					return INACCURATE_MATCH;
				typeBinding = pbBinding.searchType;
			}
			break;					
	}
	return resolveLevel(typeBinding);
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
