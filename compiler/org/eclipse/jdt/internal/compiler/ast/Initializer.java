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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;

public class Initializer extends FieldDeclaration {
	
	public Block block;
	public int lastFieldID;
	public int bodyStart;
	public Initializer(Block block, int modifiers) {
		this.block = block;
		this.modifiers = modifiers;

		declarationSourceStart = sourceStart = bodyStart = block.sourceStart;
	}

	public FlowInfo analyseCode(
		MethodScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return block.analyseCode(currentScope, flowContext, flowInfo);
	}

	/**
	 * Code generation for a non-static initializer: 
	 *    standard block code gen
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		int pc = codeStream.position;
		block.generateCode(currentScope, codeStream);
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public boolean isField() {

		return false;
	}

	public boolean isStatic() {

		return (modifiers & AccStatic) != 0;
	}

	public void parseStatements(
		Parser parser,
		TypeDeclaration typeDeclaration,
		CompilationUnitDeclaration unit) {

		//fill up the method body with statement
		parser.parse(this, typeDeclaration, unit);
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		if (modifiers != 0) {
			printIndent(indent, output);
			printModifiers(modifiers, output).append("{\n"); //$NON-NLS-1$
			block.printBody(indent, output);
			printIndent(indent, output).append('}'); 
			return output;
		} else {
			return block.printStatement(indent, output);
		}
	}
	
	public void resolve(MethodScope scope) {

		int previous = scope.fieldDeclarationIndex;
		try {
			scope.fieldDeclarationIndex = lastFieldID;
			if (isStatic()) {
				ReferenceBinding declaringType = scope.enclosingSourceType();
				if (declaringType.isNestedType() && !declaringType.isStatic())
					scope.problemReporter().innerTypesCannotDeclareStaticInitializers(
						declaringType,
						this);
			}
			block.resolve(scope);
		} finally {
			scope.fieldDeclarationIndex = previous;
		}
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, MethodScope scope) {

		if (visitor.visit(this, scope)) {
			block.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
