/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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
 * Block comment AST node type.
 * <p>
 * Block comments (also called "traditional" comments in JLS 3.7)
 * begin with "/&#42;", may contain line breaks, and must end
 * with "&#42;/". Following the definition in the JLS (first edition
 * but not second edition), block comment normally exclude comments
 * that begin with "/&#42;#42;", which are instead classified as doc
 * comments ({@link Javadoc Javadoc}).
 * </p>
 * <p>
 * Note that this node type is a comment placeholder, and is
 * only useful for recording the source range where a comment
 * was found in a source string. It is not useful for creating
 * comments.
 * </p>
 * 
 * @since 3.0
 */
public final class BlockComment extends Comment {
	/**
	 * Creates a new block comment node owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	BlockComment(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return BLOCK_COMMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		BlockComment result = new BlockComment(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
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
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize();
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}
