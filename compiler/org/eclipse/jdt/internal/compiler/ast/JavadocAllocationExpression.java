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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class JavadocAllocationExpression extends AllocationExpression {

	public int tagSourceStart, tagSourceEnd;
	public int tagValue;
	public boolean superAccess = false;
	
	public JavadocAllocationExpression(long pos) {
		this.sourceStart = (int) (pos >>> 32);
		this.sourceEnd = (int) pos;
		this.bits |= InsideJavadoc;
	}

	private TypeBinding internalResolveType(Scope scope) {
	
		// Propagate the type checking to the arguments, and check if the constructor is defined.
		this.constant = NotAConstant;
		if (this.type == null) {
			this.resolvedType = scope.enclosingSourceType();
		} else if (scope.kind == Scope.CLASS_SCOPE) {
			this.resolvedType = this.type.resolveType((ClassScope)scope);
		} else {
			this.resolvedType = this.type.resolveType((BlockScope)scope, true /* check bounds*/);
		}
	
		// buffering the arguments' types
		TypeBinding[] argumentTypes = NoParameters;
		boolean hasTypeVarArgs = false;
		if (this.arguments != null) {
			boolean argHasError = false;
			int length = this.arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++) {
				Expression argument = this.arguments[i];
				if (scope.kind == Scope.CLASS_SCOPE) {
					argumentTypes[i] = argument.resolveType((ClassScope)scope);
				} else {
					argumentTypes[i] = argument.resolveType((BlockScope)scope);
				}
				if (argumentTypes[i] == null) {
					argHasError = true;
				} else if (!hasTypeVarArgs) {
					hasTypeVarArgs = argumentTypes[i].isTypeVariable();
				}
			}
			if (argHasError) {
				return null;
			}
		}
	
		// check resolved type
		if (this.resolvedType == null) {
			return null;
		}
		this.resolvedType = scope.convertToRawType(this.type.resolvedType);
		this.superAccess = scope.enclosingSourceType().isCompatibleWith(this.resolvedType);
	
		ReferenceBinding allocationType = (ReferenceBinding) this.resolvedType;
		this.binding = scope.getConstructor(allocationType, argumentTypes, this);
		if (!this.binding.isValidBinding()) {
			MethodBinding methodBinding = scope.getMethod(this.resolvedType, this.resolvedType.sourceName(), argumentTypes, this);
			if (methodBinding.isValidBinding()) {
				this.binding = methodBinding;
			} else {
				if (this.binding.declaringClass == null) {
					this.binding.declaringClass = allocationType;
				}
				scope.problemReporter().javadocInvalidConstructor(this, this.binding, scope.getDeclarationModifiers());
			}
			return this.resolvedType;
		} else if (hasTypeVarArgs) {
			MethodBinding problem = new ProblemMethodBinding(this.binding, this.binding.selector, argumentTypes, ProblemReasons.NotFound);
			scope.problemReporter().javadocInvalidConstructor(this, problem, scope.getDeclarationModifiers());
		} else if (this.binding instanceof ParameterizedMethodBinding) {
			if (allocationType.isGenericType() || allocationType.isRawType() || allocationType.isParameterizedType()) {
				MethodBinding exactMethod = scope.findExactMethod(allocationType, this.binding.selector, argumentTypes, this);
				if (exactMethod == null) {
					MethodBinding problem = new ProblemMethodBinding(this.binding, this.binding.selector, argumentTypes, ProblemReasons.NotFound);
					scope.problemReporter().javadocInvalidConstructor(this, problem, scope.getDeclarationModifiers());
				}
			}
		}
		if (isMethodUseDeprecated(this.binding, scope)) {
			scope.problemReporter().javadocDeprecatedMethod(this.binding, this, scope.getDeclarationModifiers());
		}
		// TODO (frederic) add support for unsafe type operation warning
		return allocationType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.lookup.InvocationSite#isSuperAccess()
	 */
	public boolean isSuperAccess() {
		return this.superAccess;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding resolveType(BlockScope scope) {
		return internalResolveType(scope);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#resolveType(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public TypeBinding resolveType(ClassScope scope) {
		return internalResolveType(scope);
	}
}
