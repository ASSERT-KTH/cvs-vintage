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

package org.eclipse.jdt.core.dom;

/**
 * Labeled statement AST node type.
 *
 * <pre>
 * LabeledStatement:
 *    Identifier <b>:</b> Statement
 * </pre>
 * 
 * @since 2.0
 */
public class LabeledStatement extends Statement {
			
	/**
	 * The label; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName labelName = null;

	/**
	 * The body statement; lazily initialized; defaults to an unspecified, but 
	 * legal, statement.
	 */
	private Statement body = null;

	/**
	 * Creates a new AST node for a labeled statement owned by the given 
	 * AST. By default, the statement has an unspecified (but legal) label
	 * and an unspecified (but legal) statement.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	LabeledStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return LABELED_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		LabeledStatement result = new LabeledStatement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setLabel(
			(SimpleName) ASTNode.copySubtree(target, getLabel()));
		result.setBody(
			(Statement) ASTNode.copySubtree(target, getBody()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getLabel());
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the label of this labeled statement.
	 * 
	 * @return the variable name node
	 */ 
	public SimpleName getLabel() {
		if (labelName == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setLabel(new SimpleName(getAST()));
			getAST().setModificationCount(count);
		}
		return labelName;
	}
		
	/**
	 * Sets the label of this labeled statement.
	 * 
	 * @param label the new label
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setLabel(SimpleName label) {
		if (label == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.labelName, label, false);
		this.labelName = label;
	}
	
	/**
	 * Returns the body of this labeled statement.
	 * 
	 * @return the body statement node
	 */ 
	public Statement getBody() {
		if (body == null) {
			// lazy initialize - use setter to ensure parent link set too
			long count = getAST().modificationCount();
			setBody(new EmptyStatement(getAST()));
			getAST().setModificationCount(count);
		}
		return body;
	}
	
	/**
	 * Sets the body of this labeled statement.
	 * <p>
	 * Special note: The Java language does not allow a local variable declaration
	 * to appear as the body of a labeled statement (they may only appear within a
	 * block). However, the AST will allow a <code>VariableDeclarationStatement</code>
	 * as the body of a <code>LabeledStatement</code>. To get something that will
	 * compile, be sure to embed the <code>VariableDeclarationStatement</code>
	 * inside a <code>Block</code>.
	 * </p>
	 * 
	 * @param statement the body statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Statement statement) {
		if (statement == null) {
			throw new IllegalArgumentException();
		}
		// a LabeledStatement may occur inside a Statement - must check cycles
		replaceChild(this.body, statement, true);
		this.body = statement;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (labelName == null ? 0 : getLabel().treeSize())
			+ (body == null ? 0 : getBody().treeSize());
	}
}

