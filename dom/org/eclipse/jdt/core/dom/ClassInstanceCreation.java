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

import java.util.List;

/**
 * Class instance creation expression AST node type.
 *
 * <pre>
 * ClassInstanceCreation:
 *        [ Expression <b>.</b> ] <b>new</b> TypeName
 *            <b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 *            [ <b>{</b> ClassBodyDeclaration <b>}</b> ]
 * </pre>
 * 
 * @since 2.0
 */
public class ClassInstanceCreation extends Expression {

	/**
	 * The optional expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The type name; lazily initialized; defaults to a unspecified,
	 * legal type name.
	 */
	private Name typeName = null;
	
	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(true, Expression.class);
		
	/**
	 * Indicates whether there are body declarations.
	 */
	private boolean hasBody = false;

	/**
	 * The body declarations (element type: <code>BodyDeclaration</code>).
	 * Defaults to none.
	 */
	private ASTNode.NodeList bodyDeclarations = 
		new ASTNode.NodeList(true, BodyDeclaration.class);

	/**
	 * Creates a new AST node for a class instance creation expression owned 
	 * by the given AST. By default, there is no qualifying expression,
	 * an unspecified (but legal) type name, an empty list of arguments,
	 * and does not declare an anonymous class (body declarations are empty).
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ClassInstanceCreation (AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ClassInstanceCreation result = new ClassInstanceCreation(target);
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		result.setName((Name) getName().clone(target));
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
		result.setAnonymousClassDeclaration(isAnonymousClassDeclaration());
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
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
			acceptChild(visitor, getExpression());
			acceptChild(visitor, getName());
			acceptChildren(visitor, arguments);
			acceptChildren(visitor, bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this class instance creation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this class instance creation expression.
	 * 
	 * @param expression the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception $precondition-violation:different-ast$
	 * @exception $precondition-violation:not-unparented$
	 * @exception $postcondition-violation:ast-cycle$
	 */ 
	public void setExpression(Expression expression) {
		// a ClassInstanceCreation may occur inside an Expression
		// must check cycles
		replaceChild(this.optionalExpression, expression, true);
		this.optionalExpression = expression;
	}

	/**
	 * Returns the name of the type instantiated in this class instance 
	 * creation expression.
	 * 
	 * @return the type name node
	 */ 
	public Name getName() {
		if (typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return typeName;
	}
	
	/**
	 * Sets the name of the type instantiated in this class instance 
	 * creation expression.
	 * 
	 * @param name the new type name
	 * @exception $precondition-violation:different-ast$
	 * @exception $precondition-violation:not-unparented$
	 */ 
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.typeName, name, false);
		this.typeName = name;
	}

	/**
	 * Returns the live ordered list of argument expressions in this class
	 * instance creation expression.
	 * 
	 * @return the live list of argument expressions (possibly empty)
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return arguments;
	}
	
	/**
	 * Returns whether this class instance creation expression declares
	 * an anonymous class (that is, has class body declarations).
	 * 
	 * @return <code>true</code> if this declares an anonymous class,
	 *    and <code>false</code> otherwise
	 */ 
	public boolean isAnonymousClassDeclaration() {
		return hasBody;
	}
	
	/**
	 * Sets whether this class instance creation expression declares
	 * an anonymous class (that is, has class body declarations).
	 * 
	 * @param hasBody <code>true</code> if this declares an anonymous class,
	 *    and <code>false</code> otherwise
	 */ 
	public void setAnonymousClassDeclaration(boolean hasBody) {
		modifying();
		this.hasBody = hasBody;
	}

	/**
	 * Returns the live ordered list of body declarations of this
	 * class instance creation expression. The body declarations are only
	 * relevant when <code>isAnonymousClassDeclaration</code> returns
	 * <code>true</code>. Adding declarations to the resulting list is
	 * not sufficient; you must also call 
	 * <code>setAnonymousClassDeclaration</code> to mark the node as declaring
	 * an anonymous class.
	 * 
	 * @return the live list of body declarations
	 *    (element type: <code>BodyDeclaration</code>)
	 */ 
	public List bodyDeclarations() {
		return bodyDeclarations;
	}

	/**
	 * Resolves and returns the binding for the constructor invoked by this
	 * expression. For anonymous classes, the binding is that of the anonymous
	 * constructor.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the constructor binding, or <code>null</code> if the binding
	 *    cannot be resolved
	 */	
	public IMethodBinding resolveConstructorBinding() {
		return getAST().getBindingResolver().resolveConstructor(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 5 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (typeName == null ? 0 : getName().treeSize())
			+ (optionalExpression == null ? 0 : getExpression().treeSize())
			+ arguments.listSize()
			+ bodyDeclarations.listSize();
	}
}

