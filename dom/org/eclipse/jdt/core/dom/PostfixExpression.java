/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.HashMap;
import java.util.Map;

/**
 * Postfix expression AST node type.
 *
 * <pre>
 * PostfixExpression:
 *    Expression PostfixOperator
 * </pre>
 * 
 * @since 2.0
 */
public class PostfixExpression extends Expression {

	/**
 	 * Postfix operators (typesafe enumeration).
	 * <pre>
	 * PostfixOperator:
	 *    <b><code>++</code></p>  <code>INCREMENT</code>
	 *    <b><code>--</code></p>  <code>DECREMENT</code>
	 * </pre>
	 */
	public static class Operator {
	
		/**
		 * The token for the operator.
		 */
		private String token;
		
		/**
		 * Creates a new postfix operator with the given token.
		 * <p>
		 * Note: this constructor is private. The only instances
		 * ever created are the ones for the standard operators.
		 * </p>
		 * 
		 * @param token the character sequence for the operator
		 */
		private Operator(String token) {
			this.token = token;
		}
		
		/**
		 * Returns the character sequence for the operator.
		 * 
		 * @return the character sequence for the operator
		 */
		public String toString() {
			return token;
		}
		
		/** Postfix increment "++" operator. */
		public static final Operator INCREMENT = new Operator("++");//$NON-NLS-1$
		/** Postfix decrement "--" operator. */
		public static final Operator DECREMENT = new Operator("--");//$NON-NLS-1$
		
		/**
		 * Map from token to operator (key type: <code>String</code>;
		 * value type: <code>Operator</code>).
		 */
		private static final Map CODES;
		static {
			CODES = new HashMap(20);
			Operator[] ops = {
					INCREMENT,
					DECREMENT,
				};
			for (int i = 0; i < ops.length; i++) {
				CODES.put(ops[i].toString(), ops[i]);
			}
		}

		/**
		 * Returns the postfix operator corresponding to the given string,
		 * or <code>null</code> if none.
		 * <p>
		 * <code>toOperator</code> is the converse of <code>toString</code>:
		 * that is, <code>Operator.toOperator(op.toString()) == op</code> for 
		 * all operators <code>op</code>.
		 * </p>
		 * 
		 * @param token the character sequence for the operator
		 * @return the postfix operator, or <code>null</code> if none
		 */
		public static Operator toOperator(String token) {
			return (Operator) CODES.get(token);
		}
	}
	
	/**
	 * The operator; defaults to an unspecified postfix operator.
	 */
	private PostfixExpression.Operator operator = 
		PostfixExpression.Operator.INCREMENT;

	/**
	 * The operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression operand = null;

	/**
	 * Creates a new AST node for an postfix expression owned by the given 
	 * AST. By default, the node has unspecified (but legal) operator and 
	 * operand.
	 * 
	 * @param ast the AST that is to own this node
	 */
	PostfixExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		PostfixExpression result = new PostfixExpression(target);
		result.setOperator(getOperator());
		result.setOperand((Expression) getOperand().clone(target));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	boolean equalSubtrees(Object other) {
		if (!(other instanceof PostfixExpression)) {
			return false;
		}
		PostfixExpression o = (PostfixExpression) other;
		return 
			(getOperator().equals(o.getOperator())
			&& ASTNode.equalNodes(getOperand(), o.getOperand()));
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getOperand());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the operator of this postfix expression.
	 * 
	 * @return the operator
	 */ 
	public PostfixExpression.Operator getOperator() {
		return operator;
	}

	/**
	 * Sets the operator of this postfix expression.
	 * 
	 * @param operator the operator
	 * @exception $precondition-violation:invalid-argument$
	 */ 
	public void setOperator(PostfixExpression.Operator operator) {
		if (operator == null) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.operator = operator;
	}

	/**
	 * Returns the operand of this postfix expression.
	 * 
	 * @return the operand expression node
	 */ 
	public Expression getOperand() {
		if (operand  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setOperand(new SimpleName(getAST()));
		}
		return operand;
	}
		
	/**
	 * Sets the operand of this postfix expression.
	 * 
	 * @param expression the operand expression node
	 * @exception $precondition-violation:different-ast$
	 * @exception $precondition-violation:not-unparented$
	 * @exception $postcondition-violation:ast-cycle$
	 */ 
	public void setOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// a PostfixExpression may occur inside a Expression - must check cycles
		replaceChild(this.operand, expression, true);
		this.operand = expression;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (operand == null ? 0 : getOperand().treeSize());
	}
}
