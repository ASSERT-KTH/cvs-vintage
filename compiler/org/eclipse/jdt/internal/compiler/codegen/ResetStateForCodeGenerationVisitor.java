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
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.BranchStatement;
import org.eclipse.jdt.internal.compiler.ast.DoStatement;
import org.eclipse.jdt.internal.compiler.ast.ForStatement;
import org.eclipse.jdt.internal.compiler.ast.LabeledStatement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.WhileStatement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class ResetStateForCodeGenerationVisitor
	extends ASTVisitor {

	public boolean visit(SwitchStatement statement, BlockScope scope) {
		statement.resetStateForCodeGeneration();
		return true;
	}
	
	public boolean visit(ForStatement forStatement, BlockScope scope) {
		forStatement.resetStateForCodeGeneration();
		return true;
	}
	
	public boolean visit(WhileStatement whileStatement, BlockScope scope) {
		whileStatement.resetStateForCodeGeneration();
		return true;
	}

	public boolean visit(LabeledStatement labeledStatement, BlockScope scope) {
		labeledStatement.resetStateForCodeGeneration();
		return true;
	}

	public boolean visit(DoStatement doStatement, BlockScope scope) {
		doStatement.resetStateForCodeGeneration();
		return true;
	}

	public boolean visit(BranchStatement branchStatement, BlockScope scope) {
		branchStatement.resetStateForCodeGeneration();
		return true;
	}

	public boolean visit(TryStatement tryStatement, BlockScope scope) {
		tryStatement.resetStateForCodeGeneration();
		return true;
	}

	public boolean visit(SynchronizedStatement synchronizedStatement, BlockScope scope) {
		synchronizedStatement.resetStateForCodeGeneration();
		return true;
	}	
}

