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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class JavadocArgumentExpression extends Expression {
	public char[] token;
	public Argument argument;

	public JavadocArgumentExpression(char[] name, int startPos, int endPos, TypeReference typeRef) {
		this.token = name;
		this.sourceStart = startPos;
		this.sourceEnd = endPos;
		long pos = (((long) startPos) << 32) + endPos;
		this.argument = new Argument(name, pos, typeRef, IConstants.AccDefault);
		this.bits |= InsideJavadoc;
	}

	/*
	 * Resolves type on a Block or Class scope.
	 */
	private TypeBinding internalResolveType(Scope scope) {
		constant = NotAConstant;
		if (this.resolvedType != null) { // is a shared type reference which was already resolved
			if (!this.resolvedType.isValidBinding()) {
				return null; // already reported error
			}
		}
		else {
			if (this.argument != null) {
				TypeReference typeRef = this.argument.type;
				if (typeRef != null) {
					this.resolvedType = typeRef.getTypeBinding(scope);
					if (!this.resolvedType.isValidBinding()) {
						scope.problemReporter().invalidType(typeRef, this.resolvedType);
						return null;
					}
					if (isTypeUseDeprecated(this.resolvedType, scope)) {
						scope.problemReporter().deprecatedType(this.resolvedType, typeRef);
						return null;
					}
					return this.resolvedType;
				}
			}
		}
		return null;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		if (argument == null) {
			if (token != null) {
				output.append(token);
			}
		}
		else {
			argument.print(indent, output);
		}
		return output;
	}

	public void resolve(BlockScope scope) {
		if (argument != null) {
			argument.resolve(scope);
		}
	}

	public TypeBinding resolveType(BlockScope scope) {
		return internalResolveType(scope);
	}

	public TypeBinding resolveType(ClassScope scope) {
		return internalResolveType(scope);
	}
	
	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#traverse(org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			if (this.argument != null) {
				argument.traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}
