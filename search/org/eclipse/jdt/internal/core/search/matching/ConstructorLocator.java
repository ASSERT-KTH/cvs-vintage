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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedTypeBinding;

public class ConstructorLocator extends PatternLocator {

protected ConstructorPattern pattern;

public ConstructorLocator(ConstructorPattern pattern) {
	super(pattern);

	this.pattern = pattern;
}
public int match(ASTNode node, MatchingNodeSet nodeSet) { // interested in ExplicitConstructorCall
	if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;
	if (!(node instanceof ExplicitConstructorCall)) return IMPOSSIBLE_MATCH;

	if (this.pattern.parameterSimpleNames != null && !this.pattern.varargs) {
		int length = this.pattern.parameterSimpleNames.length;
		Expression[] args = ((ExplicitConstructorCall) node).arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
	}

	return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
public int match(ConstructorDeclaration node, MatchingNodeSet nodeSet) {
	int referencesLevel = this.pattern.findReferences ? matchLevelForReferences(node) : IMPOSSIBLE_MATCH;
	int declarationsLevel = this.pattern.findDeclarations ? matchLevelForDeclarations(node) : IMPOSSIBLE_MATCH;

	return nodeSet.addMatch(node, referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel); // use the stronger match
}
public int match(Expression node, MatchingNodeSet nodeSet) { // interested in AllocationExpression
	if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;
	if (!(node instanceof AllocationExpression)) return IMPOSSIBLE_MATCH;

	// constructor name is simple type name
	AllocationExpression allocation = (AllocationExpression) node;
	char[][] typeName = allocation.type.getTypeName();
	if (this.pattern.declaringSimpleName != null && !matchesName(this.pattern.declaringSimpleName, typeName[typeName.length-1]))
		return IMPOSSIBLE_MATCH;

	if (this.pattern.parameterSimpleNames != null && !this.pattern.varargs) {
		int length = this.pattern.parameterSimpleNames.length;
		Expression[] args = allocation.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
	}

	return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
public int match(FieldDeclaration field, MatchingNodeSet nodeSet) {
	if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;
	// look only for enum constant
	if (field.type != null || !(field.initialization instanceof AllocationExpression)) return IMPOSSIBLE_MATCH;

	AllocationExpression allocation = (AllocationExpression) field.initialization;
	if (field.binding != null && field.binding.declaringClass != null) {
		if (this.pattern.declaringSimpleName != null && !matchesName(this.pattern.declaringSimpleName, field.binding.declaringClass.sourceName()))
			return IMPOSSIBLE_MATCH;
	}

	if (this.pattern.parameterSimpleNames != null && !this.pattern.varargs) {
		int length = this.pattern.parameterSimpleNames.length;
		Expression[] args = allocation.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
	}

	return nodeSet.addMatch(field, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
//public int match(MethodDeclaration node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(MessageSend node, MatchingNodeSet nodeSet) - SKIP IT
//public int match(Reference node, MatchingNodeSet nodeSet) - SKIP IT
public int match(TypeDeclaration node, MatchingNodeSet nodeSet) {
	if (!this.pattern.findReferences) return IMPOSSIBLE_MATCH;

	// need to look for a generated default constructor
	return nodeSet.addMatch(node, ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH);
}
//public int match(TypeReference node, MatchingNodeSet nodeSet) - SKIP IT

protected int matchConstructor(MethodBinding constructor) {
	if (!constructor.isConstructor()) return IMPOSSIBLE_MATCH;

	// declaring type, simple name has already been matched by matchIndexEntry()
	int level = resolveLevelForType(this.pattern.declaringSimpleName, this.pattern.declaringQualification, constructor.declaringClass);
	if (level == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;

	// parameter types
	int parameterCount = this.pattern.parameterSimpleNames == null ? -1 : this.pattern.parameterSimpleNames.length;
	if (parameterCount > -1) {
		if (constructor.parameters == null) return INACCURATE_MATCH;
		if (parameterCount != constructor.parameters.length) return IMPOSSIBLE_MATCH;
		for (int i = 0; i < parameterCount; i++) {
			int newLevel = resolveLevelForType(this.pattern.parameterSimpleNames[i], this.pattern.parameterQualifications[i], /*this.pattern.parametersTypeArguments[i], 0,*/ constructor.parameters[i]);
			if (level > newLevel) {
				if (newLevel == IMPOSSIBLE_MATCH) return IMPOSSIBLE_MATCH;
				level = newLevel; // can only be downgraded
			}
		}
	}
	return level;
}
protected int matchContainer() {
	if (this.pattern.findReferences) return ALL_CONTAINER; // handles both declarations + references & just references
	// COMPILATION_UNIT_CONTAINER - implicit constructor call: case of Y extends X and Y doesn't define any constructor
	// CLASS_CONTAINER - implicit constructor call: case of constructor declaration with no explicit super call
	// METHOD_CONTAINER - reference in another constructor
	// FIELD_CONTAINER - anonymous in a field initializer

	// declarations are only found in Class
	return CLASS_CONTAINER;
}
protected int matchLevelForReferences(ConstructorDeclaration constructor) {
	ExplicitConstructorCall constructorCall = constructor.constructorCall;
	if (constructorCall == null || constructorCall.accessMode != ExplicitConstructorCall.ImplicitSuper)
		return IMPOSSIBLE_MATCH;

	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		Expression[] args = constructorCall.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;
	}
	return ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
}
protected int matchLevelForDeclarations(ConstructorDeclaration constructor) {
	// constructor name is stored in selector field
	if (this.pattern.declaringSimpleName != null && !matchesName(this.pattern.declaringSimpleName, constructor.selector))
		return IMPOSSIBLE_MATCH;

	if (this.pattern.parameterSimpleNames != null) {
		int length = this.pattern.parameterSimpleNames.length;
		Argument[] args = constructor.arguments;
		int argsLength = args == null ? 0 : args.length;
		if (length != argsLength) return IMPOSSIBLE_MATCH;

		/* Remove as we need to resolve to be really sure that method matches or not...
		for (int i = 0; i < length; i++)
			if (!matchesTypeReference(this.pattern.parameterSimpleNames[i], args[i].type))
				return IMPOSSIBLE_MATCH;
		*/
	}

	// Verify type arguments (do not reject if pattern has no argument as it can be an erasure match)
	if (this.pattern.hasConstructorArguments()) {
		if (constructor.typeParameters == null || constructor.typeParameters.length != this.pattern.constructorArguments.length) return IMPOSSIBLE_MATCH;
	}

	return ((InternalSearchPattern)this.pattern).mustResolve ? POSSIBLE_MATCH : ACCURATE_MATCH;
}
protected void matchReportReference(ASTNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {

	MethodBinding constructorBinding = null;
	boolean isSynthetic = false;
	if (reference instanceof ExplicitConstructorCall) {
		isSynthetic = ((ExplicitConstructorCall) reference).isImplicitSuper();
		constructorBinding = ((ExplicitConstructorCall) reference).binding;
	} else if (reference instanceof AllocationExpression) {
		constructorBinding = ((AllocationExpression) reference).binding;
	} else if (reference instanceof TypeDeclaration || reference instanceof FieldDeclaration) {
		super.matchReportReference(reference, element, accuracy, locator);
	}

	// Look to refine accuracy
	int rule = SearchPattern.R_EXACT_MATCH;
	int refinedAccuracy = accuracy;
	if (constructorBinding instanceof ParameterizedGenericMethodBinding) { // parameterized generic method
		ParameterizedGenericMethodBinding parameterizedMethodBinding = (ParameterizedGenericMethodBinding) constructorBinding;
		refinedAccuracy = refineAccuracy(accuracy, parameterizedMethodBinding.typeArguments, locator, this.pattern.constructorArguments, this.pattern.hasConstructorParameters());
		
		if (constructorBinding.declaringClass.isParameterizedType() || constructorBinding.declaringClass.isRawType()) {
			ParameterizedTypeBinding parameterizedBinding = (ParameterizedTypeBinding)constructorBinding.declaringClass;
			if (!this.pattern.hasTypeArguments() && this.pattern.hasConstructorArguments()) {
				// special case for constructor pattern which defines arguments but no type
				// in this case, we only use refined accuracy for constructor
			} else if (this.pattern.hasTypeArguments() && !this.pattern.hasConstructorArguments()) {
				// special case for constructor pattern which defines no constructor arguments but has type ones
				// in this case, we do not use refined accuracy
				refinedAccuracy = refineAccuracy(accuracy, parameterizedBinding, this.pattern.getTypeArguments(), this.pattern.hasTypeParameters(), 0, locator);
			} else {
				refinedAccuracy = refineAccuracy(refinedAccuracy, parameterizedBinding, this.pattern.getTypeArguments(), this.pattern.hasTypeParameters(), 0, locator);
			}
			if (refinedAccuracy == -1) return;
		} else if (this.pattern.hasTypeArguments()) {
			rule = SearchPattern.R_ERASURE_MATCH;
		}
	} else if (constructorBinding instanceof ParameterizedMethodBinding) {
		if (constructorBinding.declaringClass.isParameterizedType() || constructorBinding.declaringClass.isRawType()) {
			ParameterizedTypeBinding parameterizedBinding = (ParameterizedTypeBinding)constructorBinding.declaringClass;
			if (!this.pattern.hasTypeArguments() && this.pattern.hasConstructorArguments()) {
				// special case for constructor pattern which defines arguments but no type
				refinedAccuracy = refineAccuracy(refinedAccuracy, parameterizedBinding, new char[][][] {this.pattern.constructorArguments}, this.pattern.hasTypeParameters(), 0, locator);
			} else {
				refinedAccuracy = refineAccuracy(refinedAccuracy, parameterizedBinding, this.pattern.getTypeArguments(), this.pattern.hasTypeParameters(), 0, locator);
			}
			if (refinedAccuracy == -1) return;
		} else if (this.pattern.hasTypeArguments()) {
			rule = SearchPattern.R_ERASURE_MATCH;
		}
	} else if (this.pattern.hasConstructorArguments()) { // binding has no type params, compatible erasure if pattern does
		rule = SearchPattern.R_ERASURE_MATCH;
	}

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
	if (!report) return;

	// Set rule
	if (rule != SearchPattern.R_ERASURE_MATCH) {
		rule |= refinedAccuracy & RULE_MASK;
	}
	refinedAccuracy = refinedAccuracy & (~RULE_MASK);

	// Report match
	int offset = reference.sourceStart;
	SearchMatch match = locator.newMethodReferenceMatch(element,
			accuracy,
			offset,
			reference.sourceEnd - offset + 1,
			true, //isConstructor,
			isSynthetic,
			reference);
	match.setMatchRule(rule);
	locator.report(match);
}
public SearchMatch newDeclarationMatch(ASTNode reference, IJavaElement element, int accuracy, int length, MatchLocator locator) {
	SearchMatch match = null;
	int offset = reference.sourceStart;
	if (this.pattern.findReferences) {
		if (reference instanceof TypeDeclaration) {
			TypeDeclaration type = (TypeDeclaration) reference;
			AbstractMethodDeclaration[] methods = type.methods;
			if (methods != null) {
				for (int i = 0, max = methods.length; i < max; i++) {
					AbstractMethodDeclaration method = methods[i];
					boolean synthetic = method.isDefaultConstructor() && method.sourceStart < type.bodyStart;
					match = locator.newMethodReferenceMatch(element, accuracy, offset, length, method.isConstructor(), synthetic, method);
				}
			}
		} else if (reference instanceof ConstructorDeclaration) {
			ConstructorDeclaration constructor = (ConstructorDeclaration) reference;
			ExplicitConstructorCall call = constructor.constructorCall;
			boolean synthetic = call != null && call.isImplicitSuper();
			match = locator.newMethodReferenceMatch(element, accuracy, offset, length, constructor.isConstructor(), synthetic, constructor);
		}
	}
	if (match != null) {
		return match;
	}
	// super implementation...
    return locator.newDeclarationMatch(element, accuracy, reference.sourceStart, length);
}
public int resolveLevel(ASTNode node) {
	if (this.pattern.findReferences) {
		if (node instanceof AllocationExpression)
			return resolveLevel((AllocationExpression) node);
		if (node instanceof ExplicitConstructorCall)
			return resolveLevel(((ExplicitConstructorCall) node).binding);
		if (node instanceof TypeDeclaration)
			return resolveLevel((TypeDeclaration) node);
		if (node instanceof FieldDeclaration)
			return resolveLevel((FieldDeclaration) node);
	}
	if (node instanceof ConstructorDeclaration)
		return resolveLevel((ConstructorDeclaration) node, true);
	return IMPOSSIBLE_MATCH;
}
protected int referenceType() {
	return IJavaElement.METHOD;
}
protected int resolveLevel(AllocationExpression allocation) {
	// constructor name is simple type name
	char[][] typeName = allocation.type.getTypeName();
	if (this.pattern.declaringSimpleName != null && !matchesName(this.pattern.declaringSimpleName, typeName[typeName.length-1]))
		return IMPOSSIBLE_MATCH;

	return resolveLevel(allocation.binding);
}
protected int resolveLevel(FieldDeclaration field) {
	// only accept enum constants
	if (field.type != null || field.binding == null) return IMPOSSIBLE_MATCH;
	if (this.pattern.declaringSimpleName != null && !matchesName(this.pattern.declaringSimpleName, field.binding.type.sourceName()))
		return IMPOSSIBLE_MATCH;
	if (!(field.initialization instanceof AllocationExpression) || field.initialization.resolvedType.isLocalType()) return IMPOSSIBLE_MATCH;

	return resolveLevel(((AllocationExpression)field.initialization).binding);
}
public int resolveLevel(Binding binding) {
	if (binding == null) return INACCURATE_MATCH;
	if (!(binding instanceof MethodBinding)) return IMPOSSIBLE_MATCH;

	MethodBinding constructor = (MethodBinding) binding;
	int level= matchConstructor(constructor);
	if (level== IMPOSSIBLE_MATCH) {
		if (constructor != constructor.original()) {
			level= matchConstructor(constructor.original());
		}
	}
	return level;
}
protected int resolveLevel(ConstructorDeclaration constructor, boolean checkDeclarations) {
	int referencesLevel = IMPOSSIBLE_MATCH;
	if (this.pattern.findReferences) {
		ExplicitConstructorCall constructorCall = constructor.constructorCall;
		if (constructorCall != null && constructorCall.accessMode == ExplicitConstructorCall.ImplicitSuper) {
			// eliminate explicit super call as it will be treated with matchLevel(ExplicitConstructorCall, boolean)
			referencesLevel = resolveLevel(constructorCall.binding);
			if (referencesLevel == ACCURATE_MATCH) return ACCURATE_MATCH; // cannot get better
		}
	}
	if (!checkDeclarations) return referencesLevel;

	int declarationsLevel = this.pattern.findDeclarations ? resolveLevel(constructor.binding) : IMPOSSIBLE_MATCH;
	return referencesLevel >= declarationsLevel ? referencesLevel : declarationsLevel; // answer the stronger match
}
protected int resolveLevel(TypeDeclaration type) {
	// find default constructor
	AbstractMethodDeclaration[] methods = type.methods;
	if (methods != null) {
		for (int i = 0, length = methods.length; i < length; i++) {
			AbstractMethodDeclaration method = methods[i];
			if (method.isDefaultConstructor() && method.sourceStart < type.bodyStart) // if synthetic
				return resolveLevel((ConstructorDeclaration) method, false);
		}
	}
	return IMPOSSIBLE_MATCH;
}
public String toString() {
	return "Locator for " + this.pattern.toString(); //$NON-NLS-1$
}
}