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

import java.util.List;

/**
 * AST node for a text element within a doc comment.
 * <pre>
 * TextElement:
 *     Sequence of characters not including a close comment delimiter <b>*</b><b>/</b>
 * </pre>
 * 
 * @see Javadoc
 * @since 3.0
 */
public final class TextElement extends ASTNode implements IDocElement {

	/**
	 * The "test" structural property of this node type.
	 * 
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor TEXT_PROPERTY = 
		new SimplePropertyDescriptor(TextElement.class, "text", String.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 * @since 3.0
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		createPropertyList(TextElement.class);
		addProperty(TEXT_PROPERTY);
		PROPERTY_DESCRIPTORS = reapPropertyList();
	}
	
	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants
	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
	
	/**
	 * Canonical empty string.
	 */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	
	/**
	 * The text element; defaults to the empty string.
	 */
	private String text = EMPTY_STRING;
	
	/**
	 * Creates a new AST node for a text element owned by the given AST.
	 * The new node has an empty text string.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TextElement(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == TEXT_PROPERTY) {
			if (get) {
				return getText();
			} else {
				setText((String) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return TEXT_ELEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone0(AST target) {
		TextElement result = new TextElement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setText(getText());
		return result;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
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
	 * Returns this node's text.
	 * 
	 * @return the text of this node
	 */ 
	public String getText() {
		return this.text;
	}
	
	/**
	 * Sets the text of this node to the given value.
	 * <p>
	 * The text element typically includes leading and trailing
	 * whitespace that separates it from the immediately preceding
	 * or following elements. The text element must not include
	 * a block comment closing delimiter "*"+"/".
	 * </p>
	 * 
	 * @param text the text of this node
	 * @exception IllegalArgumentException if the text is null
	 * or contains a block comment closing delimiter
	 */ 
	public void setText(String text) {
		if (text == null) {
			throw new IllegalArgumentException();
		}
		if (text.indexOf("*/") > 0) { //$NON-NLS-1$
			throw new IllegalArgumentException();
		}
		preValueChange(TEXT_PROPERTY);
		this.text = text;
		postValueChange(TEXT_PROPERTY);
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 1 * 4;
		if (this.text != EMPTY_STRING) {
			// everything but our empty string costs
			size += stringSize(this.text);
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

