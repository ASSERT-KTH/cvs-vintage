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

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * String literal nodes.
 * 
 * @since 2.0
 */
public class StringLiteral extends Expression {

	/**
	 * The literal string, including quotes and escapes; defaults to the 
	 * literal for the empty string.
	 */
	private String escapedValue = "\"\"";//$NON-NLS-1$

	/**
	 * Creates a new unparented string literal node owned by the given AST.
	 * By default, the string literal denotes the empty string.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	StringLiteral(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return STRING_LITERAL;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		StringLiteral result = new StringLiteral(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setEscapedValue(getEscapedValue());
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
	
	/**
	 * Returns the string value of this literal node to the given string
	 * literal token. The token is the sequence of characters that would appear
	 * in the source program, including enclosing double quotes and embedded
	 * escapes.
	 * 
	 * @return the string literal token, including enclosing double
	 *    quotes and embedded escapes
	 */ 
	public String getEscapedValue() {
		return escapedValue;
	}
		
	/**
	 * Sets the string value of this literal node to the given string literal
	 * token. The token is the sequence of characters that would appear in the
	 * source program, including enclosing double quotes and embedded escapes.
	 * For example,
	 * <ul>
	 * <li><code>""</code> <code>setLiteral("\"\"")</code></li>
	 * <li><code>"hello world"</code> <code>setLiteral("\"hello world\"")</code></li>
	 * <li><code>"boo\nhoo"</code> <code>setLiteral("\"boo\\nhoo\"")</code></li>
	 * </ul>
	 * 
	 * @param token the string literal token, including enclosing double
	 *    quotes and embedded escapes
	 * @exception IllegalArgumentException if the argument is incorrect
	 */ 
	public void setEscapedValue(String token) {
		if (token == null) {
			throw new IllegalArgumentException("Token cannot be null"); //$NON-NLS-1$
		}
		Scanner scanner = getAST().scanner;
		char[] source = token.toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case TerminalTokens.TokenNameStringLiteral:
					break;
				default:
					throw new IllegalArgumentException("Invalid string literal : >" + token + "<"); //$NON-NLS-1$//$NON-NLS-2$
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException("Invalid string literal : >" + token + "<");//$NON-NLS-1$//$NON-NLS-2$
		}
		modifying();
		this.escapedValue = token;
	}

	/**
	 * Returns the value of this literal node. 
	 * <p>
	 * For example,
	 * <code>
	 * <pre>
	 * StringLiteral s;
	 * s.setEscapedValue("\"hello\\nworld\"");
	 * assert s.getLiteralValue().equals("hello\nworld");
	 * </pre>
	 * </p>
	 * <p>
	 * Note that this is a convenience method that converts from the stored 
	 * string literal token returned by <code>getEscapedLiteral</code>.
	 * </p>
	 * 
	 * @return the string value without enclosing double quotes and embedded
	 *    escapes
	 * @exception IllegalArgumentException if the literal value cannot be converted
	 */ 
	public String getLiteralValue() {
		String s = getEscapedValue();
		int len = s.length();
		if (len < 2 || s.charAt(0) != '\"' || s.charAt(len-1) != '\"' ) {
			throw new IllegalArgumentException();
		}
		
		Scanner scanner = getAST().scanner;
		char[] source = s.toCharArray();
		scanner.setSource(source);
		scanner.resetTo(0, source.length);
		try {
			int tokenType = scanner.getNextToken();
			switch(tokenType) {
				case TerminalTokens.TokenNameStringLiteral:
					return new String(scanner.getCurrentTokenSourceString());
				default:
					throw new IllegalArgumentException();
			}
		} catch(InvalidInputException e) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Sets the value of this literal node. 
	 * <p>
	 * For example,
	 * <code>
	 * <pre>
	 * StringLiteral s;
	 * s.setLiteralValue("hello\nworld");
	 * assert s.getEscapedValue("\"hello\\nworld\"");
	 * assert s.getLiteralValue().equals("hello\nworld");
	 * </pre>
	 * </p>
	 * <p>
	 * Note that this is a convenience method that converts to the stored 
	 * string literal token acceptable to <code>setEscapedLiteral</code>.
	 * </p>
	 * 
	 * @param literal the string value without enclosing double quotes and 
	 *    embedded escapes
	 * @exception IllegalArgumentException if the argument is incorrect
	 */
	public void setLiteralValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException();
		}
		int len = value.length();
		StringBuffer b = new StringBuffer(len + 2);
		
		b.append("\""); // opening delimiter //$NON-NLS-1$
		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);
			switch(c) {
				case '\b' :
					b.append("\\b"); //$NON-NLS-1$
					break;
				case '\t' :
					b.append("\\t"); //$NON-NLS-1$
					break;
				case '\n' :
					b.append("\\n"); //$NON-NLS-1$
					break;
				case '\f' :
					b.append("\\f"); //$NON-NLS-1$
					break;
				case '\r' :
					b.append("\\r"); //$NON-NLS-1$
					break;
				case '\"':
					b.append("\\\""); //$NON-NLS-1$
					break;
				case '\'':
					b.append("\\\'"); //$NON-NLS-1$
					break;
				case '\\':
					b.append("\\\\"); //$NON-NLS-1$
					break;
				case '\0' :
					b.append("\\0"); //$NON-NLS-1$
					break;
				case '\1' :
					b.append("\\1"); //$NON-NLS-1$
					break;
				case '\2' :
					b.append("\\2"); //$NON-NLS-1$
					break;
				case '\3' :
					b.append("\\3"); //$NON-NLS-1$
					break;
				case '\4' :
					b.append("\\4"); //$NON-NLS-1$
					break;
				case '\5' :
					b.append("\\5"); //$NON-NLS-1$
					break;
				case '\6' :
					b.append("\\6"); //$NON-NLS-1$
					break;
				case '\7' :
					b.append("\\7"); //$NON-NLS-1$
					break;			
				default:
					b.append(c);
			}
		}
		b.append("\""); // closing delimiter //$NON-NLS-1$
		setEscapedValue(b.toString());
	}
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4 + stringSize(escapedValue);
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}
