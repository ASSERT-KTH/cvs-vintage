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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class TypeReferenceLocator extends PatternLocator {

protected TypeReferencePattern pattern;
protected boolean isDeclarationOfReferencedTypesPattern;

public TypeReferenceLocator(TypeReferencePattern pattern) {

	super(pattern);

	this.pattern = pattern;
	this.isDeclarationOfReferencedTypesPattern = this.pattern instanceof DeclarationOfReferencedTypesPattern;
}
protected IJavaElement findElement(IJavaElement element, int accuracy) {
	// need exact match to be able to open on type ref
	if (accuracy != SearchMatch.A_ACCURATE) return null;

	// element that references the type must be included in the enclosing element
	DeclarationOfReferencedTypesPattern declPattern = (DeclarationOfReferencedTypesPattern) this.pattern; 
	while (element != null && !declPattern.enclosingElement.equals(element))
		element = element.getParent();
	return element;
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
public int match(Reference node, MatchingNodeSet nodeSet) { // interested in NameReference & its subtypes
	if (!(node instanceof NameReference)) return IMPOSSIBLE_MATCH;

	if (this.pattern.simpleName == null)
		return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	if (node instanceof SingleNameReference) {
		if (matchesName(this.pattern.simpleName, ((SingleNameReference) node).token))
			return nodeSet.addMatch(node, POSSIBLE_MATCH); // resolution is needed to find out if it is a type ref 
	} else {
		char[][] tokens = ((QualifiedNameReference) node).tokens;
		for (int i = 0, max = tokens.length; i < max; i++)
			if (matchesName(this.pattern.simpleName, tokens[i]))
				return nodeSet.addMatch(node, POSSIBLE_MATCH); // resolution is needed to find out if it is a type ref
	}

	return IMPOSSIBLE_MATCH;
}
//public int match(TypeDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
public int match(TypeReference node, MatchingNodeSet nodeSet) {
	if (this.pattern.simpleName == null)
		return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);

	if (node instanceof SingleTypeReference) {
		if (matchesName(this.pattern.simpleName, ((SingleTypeReference) node).token))
			return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
	} else {
		char[][] tokens = ((QualifiedTypeReference) node).tokens;
		for (int i = 0, max = tokens.length; i < max; i++)
			if (matchesName(this.pattern.simpleName, tokens[i]))
				return nodeSet.addMatch(node, POSSIBLE_MATCH); // resolution is needed to find out if it is a type ref
	}

	return IMPOSSIBLE_MATCH;
}

protected int matchLevel(ImportReference importRef) {
	if (this.pattern.qualification == null) {
		if (this.pattern.simpleName == null) return ACCURATE_MATCH;
		char[][] tokens = importRef.tokens;
		for (int i = 0, length = tokens.length; i < length; i++)
			if (matchesName(this.pattern.simpleName, tokens[i])) return ACCURATE_MATCH;
	} else {
		char[][] tokens = importRef.tokens;
		char[] qualifiedPattern = this.pattern.simpleName == null
			? this.pattern.qualification
			: CharOperation.concat(this.pattern.qualification, this.pattern.simpleName, '.');
		char[] qualifiedTypeName = CharOperation.concatWith(tokens, '.');
		switch (this.matchMode) {
			case SearchPattern.R_EXACT_MATCH :
			case SearchPattern.R_PREFIX_MATCH :
				if (CharOperation.prefixEquals(qualifiedPattern, qualifiedTypeName, this.isCaseSensitive)) return POSSIBLE_MATCH;
				break;
			case SearchPattern.R_PATTERN_MATCH:
				if (CharOperation.match(qualifiedPattern, qualifiedTypeName, this.isCaseSensitive)) return POSSIBLE_MATCH;
				break;
		}
	}
	return IMPOSSIBLE_MATCH;
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.matching.PatternLocator#matchLevelAndReportImportRef(org.eclipse.jdt.internal.compiler.ast.ImportReference, org.eclipse.jdt.internal.compiler.lookup.Binding, org.eclipse.jdt.internal.core.search.matching.MatchLocator)
 */
protected void matchLevelAndReportImportRef(ImportReference importRef, Binding binding, MatchLocator locator) throws CoreException {
	Binding refBinding = binding;
	if (importRef.isStatic()) {
		// for static import, binding can be a field binding or a member type binding
		// verify that in this case binding is static and use declaring class for fields
		if (binding instanceof FieldBinding) {
			FieldBinding fieldBinding = (FieldBinding) binding;
			if (!fieldBinding.isStatic()) return;
			refBinding = fieldBinding.declaringClass;
		} else if (binding instanceof MemberTypeBinding) {
			MemberTypeBinding memberBinding = (MemberTypeBinding) binding;
			if (!memberBinding.isStatic()) return;
		}
	}
	super.matchLevelAndReportImportRef(importRef, refBinding, locator);
}
protected void matchReportImportRef(ImportReference importRef, Binding binding, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedTypesPattern) {
		if ((element = findElement(element, accuracy)) != null) {
			SimpleSet knownTypes = ((DeclarationOfReferencedTypesPattern) this.pattern).knownTypes; 
			while (binding instanceof ReferenceBinding) {
				ReferenceBinding typeBinding = (ReferenceBinding) binding;
				reportDeclaration(typeBinding, 1, locator, knownTypes);
				binding = typeBinding.enclosingType();
			}
		}
		return;
	}

	// return if this is not necessary to report
	if (this.pattern.hasTypeArguments() && !this.isEquivalentMatch &&!this.isErasureMatch) {
		return;
	}

	// set match rule
	int rule = SearchMatch.A_ACCURATE;
	if (this.pattern.hasTypeArguments()) { // binding has no type params, compatible erasure if pattern does
		rule = SearchPattern.R_EQUIVALENT_MATCH | SearchPattern.R_ERASURE_MATCH;
	}
	
	// Try to find best selection for match
	if (binding instanceof ReferenceBinding) {
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		int lastIndex = importRef.tokens.length - 1;
		if (importRef.isStatic() && !importRef.onDemand && !typeBinding.isMemberType()) {
			// for field static import, do not use last token
			lastIndex--;
		}
		if (typeBinding instanceof ProblemReferenceBinding) {
			ProblemReferenceBinding pbBinding = (ProblemReferenceBinding) typeBinding;
			typeBinding = pbBinding.original;
			lastIndex = pbBinding.compoundName.length - 1;
		}
		// try to match all enclosing types for which the token matches as well.
		while (typeBinding != null && lastIndex >= 0) {
			if (resolveLevelForType(typeBinding) != IMPOSSIBLE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = importRef.sourcePositions;
					// index now depends on pattern type signature
					int index = lastIndex;
					if (this.pattern.qualification != null) {
						index = lastIndex - this.pattern.segmentsSize;
					}
					if (index < 0) index = 0;
					int start = (int) ((positions[index]) >>> 32);
					int end = (int) positions[lastIndex];
					// report match
					SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, start, end-start+1, rule, importRef);
					locator.report(match);
				}
				return;
			}
			lastIndex--;
			typeBinding = typeBinding.enclosingType();
		}
	}
	locator.reportAccurateTypeReference(importRef, this.pattern.simpleName, element, accuracy, rule);
}
protected void matchReportReference(ArrayTypeReference arrayRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.pattern.simpleName == null) {
		// TODO (frederic) need to add a test for this case while searching generic types...
		if (locator.encloses(element)) {
			int offset = arrayRef.sourceStart;
			SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, offset, arrayRef.sourceEnd-offset+1, arrayRef);
			locator.report(match);
			return;
		}
	}
	if (arrayRef.resolvedType != null) {
		matchReportReference(arrayRef, element, accuracy, arrayRef.sourceStart, arrayRef.sourceEnd, -1, arrayRef.resolvedType.leafComponentType(), locator);
		return;
	}
	locator.reportAccurateTypeReference(arrayRef, this.pattern.simpleName, element, accuracy);
}
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	if (this.isDeclarationOfReferencedTypesPattern) {
		if ((element = findElement(element, accuracy)) != null)
			reportDeclaration(reference, element, locator, ((DeclarationOfReferencedTypesPattern) this.pattern).knownTypes);
		return;
	}

	if (reference instanceof QualifiedNameReference)
		matchReportReference((QualifiedNameReference) reference, element, accuracy, locator);
	else if (reference instanceof QualifiedTypeReference)
		matchReportReference((QualifiedTypeReference) reference, element, accuracy, locator);
	else if (reference instanceof ArrayTypeReference)
		matchReportReference((ArrayTypeReference) reference, element, accuracy, locator);
	else {
		TypeBinding typeBinding = reference instanceof Expression ? ((Expression)reference).resolvedType : null;
		if (typeBinding != null) {
			matchReportReference((Expression)reference, element, accuracy, reference.sourceStart, reference.sourceEnd, -1, typeBinding, locator);
			return;
		}
		int offset = reference.sourceStart;
		SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, offset, reference.sourceEnd-offset+1, reference);
		locator.report(match);
	}
}
protected void matchReportReference(QualifiedNameReference qNameRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	Binding binding = qNameRef.binding;
	TypeBinding typeBinding = null;
	int lastIndex = qNameRef.tokens.length - 1;
	switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
		case Binding.FIELD : // reading a field
			typeBinding = qNameRef.actualReceiverType;
			lastIndex -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
			break;
		case Binding.TYPE : //=============only type ==============
			if (binding instanceof TypeBinding)
				typeBinding = (TypeBinding) binding;
			break;
		case Binding.VARIABLE : //============unbound cases===========
		case Binding.TYPE | Binding.VARIABLE :
			if (binding instanceof ProblemReferenceBinding) {
				typeBinding = (TypeBinding) binding;
			} else if (binding instanceof ProblemFieldBinding) {
				typeBinding = qNameRef.actualReceiverType;
				lastIndex -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
			} else if (binding instanceof ProblemBinding) {
				typeBinding = ((ProblemBinding) binding).searchType;
			}
			break;					
	}
	if (typeBinding instanceof ProblemReferenceBinding) {
		ProblemReferenceBinding pbBinding = (ProblemReferenceBinding) typeBinding;
		typeBinding = pbBinding.original;
		lastIndex = pbBinding.compoundName.length - 1;
	}
	// try to match all enclosing types for which the token matches as well.
	if (typeBinding instanceof ReferenceBinding) {
		ReferenceBinding refBinding = (ReferenceBinding) typeBinding; 
		while (refBinding != null && lastIndex >= 0) {
			if (resolveLevelForType(refBinding) == ACCURATE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = qNameRef.sourcePositions;
					int start = (int) ((positions[this.pattern.qualification == null ? lastIndex : 0]) >>> 32);
					int end = (int) positions[lastIndex];
					SearchMatch match = locator.newTypeReferenceMatch(element, accuracy, start, end-start+1, qNameRef);
					locator.report(match);
				}
				return;
			}
			lastIndex--;
			refBinding = refBinding.enclosingType();
		}
	}
	locator.reportAccurateTypeReference(qNameRef, this.pattern.simpleName, element, accuracy);
}
protected void matchReportReference(QualifiedTypeReference qTypeRef, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	TypeBinding typeBinding = qTypeRef.resolvedType;
	int lastIndex = qTypeRef.tokens.length - 1;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding) {
		ProblemReferenceBinding pbBinding = (ProblemReferenceBinding) typeBinding;
		typeBinding = pbBinding.original;
		lastIndex = pbBinding.compoundName.length - 1;
	}
	// try to match all enclosing types for which the token matches as well
	if (typeBinding instanceof ReferenceBinding) {
		ReferenceBinding refBinding = (ReferenceBinding) typeBinding; 
		while (refBinding != null && lastIndex >= 0) {
			if (resolveLevelForType(refBinding) != IMPOSSIBLE_MATCH) {
				if (locator.encloses(element)) {
					long[] positions = qTypeRef.sourcePositions;
					// index now depends on pattern type signature
					int index = lastIndex;
					if (this.pattern.qualification != null) {
						index = lastIndex - this.pattern.segmentsSize;
					}
					if (index < 0) index = 0;
					int start = (int) ((positions[index]) >>> 32);
					int end = (int) positions[lastIndex];

					//  Look if there's a need to special report for parameterized type
					matchReportReference(qTypeRef, element, accuracy, start, end, lastIndex, refBinding, locator);
				}
				return;
			}
			lastIndex--;
			refBinding = refBinding.enclosingType();
		}
	}
	locator.reportAccurateTypeReference(qTypeRef, this.pattern.simpleName, element, accuracy);
}
void matchReportReference(Expression expr, IJavaElement element, int accuracy, int start, int end, int lastIndex, TypeBinding refBinding, MatchLocator locator) throws CoreException {

	// Look if there's a need to special report for parameterized type
	int rule = SearchPattern.R_EXACT_MATCH;
	int refinedAccuracy = accuracy;
	if (refBinding.isParameterizedType() || refBinding.isRawType()) {

		// Try to refine accuracy
		ParameterizedTypeBinding parameterizedBinding = (ParameterizedTypeBinding)refBinding;
		refinedAccuracy = refineAccuracy(accuracy, parameterizedBinding, this.pattern.getTypeArguments(), this.pattern.hasTypeParameters(), 0, locator);
		
		// See whether it is necessary to report or not
		boolean report = refinedAccuracy != -1; // impossible match
		if (report && (refinedAccuracy & SearchPattern.R_ERASURE_MATCH) != 0) { // erasure match
			if ((refinedAccuracy & SearchPattern.R_EQUIVALENT_MATCH) != 0) { // raw match
				report = this.isEquivalentMatch || this.isErasureMatch; // report only if pattern is equivalent or erasure
			} else {
				report = this.isErasureMatch; // report only if pattern is erasure
			}
		}
		else if (report && (refinedAccuracy & SearchPattern.R_EQUIVALENT_MATCH) != 0) { // equivalent match
			report  = this.isEquivalentMatch || this.isErasureMatch; // report only if pattern is equivalent or erasure
		}
		if (!report)return;

		// Set rule
		rule |= refinedAccuracy & RULE_MASK;
		refinedAccuracy = refinedAccuracy & (~RULE_MASK);

		// Make a special report for parameterized types if necessary
		 if (refBinding.isParameterizedType() && this.pattern.hasTypeArguments())  {
			TypeReference typeRef = null;
			TypeReference[] typeArguments = null;
			if (expr instanceof ParameterizedQualifiedTypeReference) {
				typeRef = (ParameterizedQualifiedTypeReference) expr;
				typeArguments = ((ParameterizedQualifiedTypeReference) expr).typeArguments[lastIndex];
			}
			else if (expr instanceof ParameterizedSingleTypeReference) {
				typeRef = (ParameterizedSingleTypeReference) expr;
				typeArguments = ((ParameterizedSingleTypeReference) expr).typeArguments;
			}
			if (typeRef != null) {
				locator.reportAccurateParameterizedTypeReference(typeRef, start, lastIndex, typeArguments, element, refinedAccuracy, rule);
				return;
			}
		}
	} else if (this.pattern.hasTypeArguments()) { // binding has no type params, compatible erasure if pattern does
		rule = SearchPattern.R_ERASURE_MATCH;
	}

	// Report match
	if (expr instanceof ArrayTypeReference)
		locator.reportAccurateTypeReference(expr, this.pattern.simpleName, element, refinedAccuracy, rule);
	else {
		SearchMatch match = locator.newTypeReferenceMatch(element, refinedAccuracy, start, end-start+1, rule, expr);
		locator.report(match);
	}
}
protected int referenceType() {
	return IJavaElement.TYPE;
}
protected void reportDeclaration(ASTNode reference, IJavaElement element, MatchLocator locator, SimpleSet knownTypes) throws CoreException {
	int maxType = -1;
	TypeBinding typeBinding = null;
	if (reference instanceof TypeReference) {
		typeBinding = ((TypeReference) reference).resolvedType;
		maxType = Integer.MAX_VALUE;
	} else if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qNameRef = (QualifiedNameReference) reference;
		Binding binding = qNameRef.binding;
		maxType = qNameRef.tokens.length - 1;
		switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
			case Binding.FIELD : // reading a field
				typeBinding = qNameRef.actualReceiverType;
				maxType -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
				break;
			case Binding.TYPE : //=============only type ==============
				if (binding instanceof TypeBinding)
					typeBinding = (TypeBinding) binding;
				break;
			case Binding.VARIABLE : //============unbound cases===========
			case Binding.TYPE | Binding.VARIABLE :
				if (binding instanceof ProblemFieldBinding) {
					typeBinding = qNameRef.actualReceiverType;
					maxType -= qNameRef.otherBindings == null ? 1 : qNameRef.otherBindings.length + 1;
				} else if (binding instanceof ProblemBinding) {
					ProblemBinding pbBinding = (ProblemBinding) binding;
					typeBinding = pbBinding.searchType; // second chance with recorded type so far
					char[] partialQualifiedName = pbBinding.name;
					maxType = CharOperation.occurencesOf('.', partialQualifiedName) - 1; // index of last bound token is one before the pb token
					if (typeBinding == null || maxType < 0) return;
				}
				break;
		}
	} else if (reference instanceof SingleNameReference) {
		typeBinding = (TypeBinding) ((SingleNameReference) reference).binding;
		maxType = 1;
	}
	
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding == null || typeBinding instanceof BaseTypeBinding) return;
	if (typeBinding instanceof ProblemReferenceBinding) {
		ReferenceBinding original = ((ProblemReferenceBinding) typeBinding).original;
		if (original == null) return; // original may not be set (bug 71279)
		typeBinding = original;
	}
	typeBinding = typeBinding.erasure();
	reportDeclaration((ReferenceBinding) typeBinding, maxType, locator, knownTypes);
}
protected void reportDeclaration(ReferenceBinding typeBinding, int maxType, MatchLocator locator, SimpleSet knownTypes) throws CoreException {
	IType type = locator.lookupType(typeBinding);
	if (type == null) return; // case of a secondary type

	IResource resource = type.getResource();
	boolean isBinary = type.isBinary();
	IBinaryType info = null;
	if (isBinary) {
		if (resource == null)
			resource = type.getJavaProject().getProject();
		info = locator.getBinaryInfo((org.eclipse.jdt.internal.core.ClassFile) type.getClassFile(), resource);
	}
	while (maxType >= 0 && type != null) {
		if (!knownTypes.includes(type)) {
			if (isBinary) {
				locator.reportBinaryMemberDeclaration(resource, type, info, SearchMatch.A_ACCURATE);
			} else {
				if (typeBinding instanceof ParameterizedTypeBinding)
					typeBinding = ((ParameterizedTypeBinding) typeBinding).type;
				ClassScope scope = ((SourceTypeBinding) typeBinding).scope;
				if (scope != null) {
					TypeDeclaration typeDecl = scope.referenceContext;
					int offset = typeDecl.sourceStart;
					SearchMatch match = new TypeDeclarationMatch(type, SearchMatch.A_ACCURATE, offset, typeDecl.sourceEnd-offset+1, locator.getParticipant(), resource);
					locator.report(match);
				}
			}
			knownTypes.add(type);
		}
		typeBinding = typeBinding.enclosingType();
		IJavaElement parent = type.getParent();
		if (parent instanceof IType) {
			type = (IType)parent;
		} else {
			type = null;
		}
		maxType--;
	}
}
public int resolveLevel(ASTNode node) {
	if (node instanceof TypeReference)
		return resolveLevel((TypeReference) node);
	if (node instanceof NameReference)
		return resolveLevel((NameReference) node);
//	if (node instanceof ImportReference) - Not called when resolve is true, see MatchingNodeSet.reportMatching(unit)
	return IMPOSSIBLE_MATCH;
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof TypeBinding)) return IMPOSSIBLE_MATCH;

	TypeBinding typeBinding = (TypeBinding) binding;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding)
		typeBinding = ((ProblemReferenceBinding) typeBinding).original;

	if (((InternalSearchPattern) this.pattern).focus instanceof IType && typeBinding instanceof ReferenceBinding) {
		IPackageFragment pkg = ((IType) ((InternalSearchPattern) this.pattern).focus).getPackageFragment();
		// check that type is located inside this instance of a package fragment
		if (!PackageReferenceLocator.isDeclaringPackageFragment(pkg, (ReferenceBinding) typeBinding))
			return IMPOSSIBLE_MATCH;
	}

	return resolveLevelForTypeOrEnclosingTypes(this.pattern.simpleName, this.pattern.qualification, typeBinding);
}
protected int resolveLevel(NameReference nameRef) {
	Binding binding = nameRef.binding;

	if (nameRef instanceof SingleNameReference) {
		if (binding instanceof ProblemReferenceBinding)
			binding = ((ProblemReferenceBinding) binding).original;
		if (binding instanceof ReferenceBinding)
			return resolveLevelForType((ReferenceBinding) binding);
		return binding == null || binding instanceof ProblemBinding ? INACCURATE_MATCH : IMPOSSIBLE_MATCH;
	}

	TypeBinding typeBinding = null;
	QualifiedNameReference qNameRef = (QualifiedNameReference) nameRef;
	switch (qNameRef.bits & ASTNode.RestrictiveFlagMASK) {
		case Binding.FIELD : // reading a field
			if (qNameRef.tokens.length < (qNameRef.otherBindings == null ? 2 : qNameRef.otherBindings.length + 2))
				return IMPOSSIBLE_MATCH; // must be at least A.x
			typeBinding = nameRef.actualReceiverType;
			break;
		case Binding.LOCAL : // reading a local variable
			return IMPOSSIBLE_MATCH; // no type match in it
		case Binding.TYPE : //=============only type ==============
			if (binding instanceof TypeBinding)
				typeBinding = (TypeBinding) binding;
			break;
		/*
		 * Handling of unbound qualified name references. The match may reside in the resolved fragment,
		 * which is recorded inside the problem binding, along with the portion of the name until it became a problem.
		 */
		case Binding.VARIABLE : //============unbound cases===========
		case Binding.TYPE | Binding.VARIABLE :
			if (binding instanceof ProblemReferenceBinding) {
				typeBinding = (TypeBinding) binding;
			} else if (binding instanceof ProblemFieldBinding) {
				if (qNameRef.tokens.length < (qNameRef.otherBindings == null ? 2 : qNameRef.otherBindings.length + 2))
					return IMPOSSIBLE_MATCH; // must be at least A.x
				typeBinding = nameRef.actualReceiverType;
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
protected int resolveLevel(TypeReference typeRef) {
	TypeBinding typeBinding = typeRef.resolvedType;
	if (typeBinding instanceof ArrayBinding)
		typeBinding = ((ArrayBinding) typeBinding).leafComponentType;
	if (typeBinding instanceof ProblemReferenceBinding)
		typeBinding = ((ProblemReferenceBinding) typeBinding).original;

	if (typeRef instanceof SingleTypeReference) {
		return resolveLevelForType(typeBinding);
	} else
		return resolveLevelForTypeOrEnclosingTypes(this.pattern.simpleName, this.pattern.qualification, typeBinding);
}
/* (non-Javadoc)
 * Resolve level for type with a given binding.
 * This is just an helper to avoid call of method with all parameters...
 */
protected int resolveLevelForType(TypeBinding typeBinding) {
	return resolveLevelForType(
			this.pattern.simpleName,
			this.pattern.qualification,
			this.pattern.getTypeArguments(),
			0,
			typeBinding);
}
/**
 * Returns whether the given type binding or one of its enclosing types
 * matches the given simple name pattern and qualification pattern.
 * Returns ACCURATE_MATCH if it does.
 * Returns INACCURATE_MATCH if resolve failed.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 */
protected int resolveLevelForTypeOrEnclosingTypes(char[] simpleNamePattern, char[] qualificationPattern, TypeBinding binding) {
	if (binding == null) return INACCURATE_MATCH;

	if (binding instanceof ReferenceBinding) {
		ReferenceBinding type = (ReferenceBinding) binding;
		while (type != null) {
			int level = resolveLevelForType(type);
			if (level != IMPOSSIBLE_MATCH) return level;
	
			type = type.enclosingType();
		}
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}
